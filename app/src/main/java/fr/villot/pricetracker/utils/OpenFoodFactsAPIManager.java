package fr.villot.pricetracker.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.villot.pricetracker.model.Product;

public class OpenFoodFactsAPIManager {
    private static final String TAG = OpenFoodFactsAPIManager.class.getSimpleName();
    private static final String API_URL = "https://world.openfoodfacts.org/api/v0/product/";

    public interface OnProductDataReceivedListener {
        void onProductDataReceived(Product product);

        void onProductDataError(String errorMessage);
    }

    public static void getAsyncProductData(String barcode, OnProductDataReceivedListener listener) {
        new Thread(() -> {
            String apiUrl = API_URL + barcode + ".json";
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject jsonResponse = getJsonResponse(connection);
                    if (jsonResponse.has("product")) {
                        JSONObject jsonProduct = jsonResponse.getJSONObject("product");

                        // Create a Product object from the JSON data
                        Product product = parseProductData(barcode, jsonProduct);

                        // Call the listener's callback with the product data
                        listener.onProductDataReceived(product);
                    } else {
                        // No product data found
                        listener.onProductDataError("No product data found for barcode: " + barcode);
                    }
                } else {
                    // Error response from the server
                    listener.onProductDataError("Error response from server. Response code: " + responseCode);
                }

                connection.disconnect();
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error retrieving product data", e);
                listener.onProductDataError("Error retrieving product data: " + e.getMessage());
            }
        }).start();
    }

    private static @NonNull JSONObject getJsonResponse(HttpURLConnection connection) throws IOException, JSONException {
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse the JSON response
        return new JSONObject(response.toString());
    }

    private static Product parseProductData(String barcode, JSONObject jsonProduct) throws JSONException {

        // On essaie d'abord de recupérer la version française du nom si elle existe sinon on prend la version officielle.
        String name = jsonProduct.optString("product_name_fr", "");
        if (name.isEmpty()) {
            name = jsonProduct.optString("product_name", "");
        }

        String brand = jsonProduct.optString("brands", "");
        String quantity = jsonProduct.optString("quantity", "");
        String origin = jsonProduct.optString("origins", "");
        String imageUrl = jsonProduct.optString("image_url", "");

        // Create and return the Product object
        return new Product(barcode, name, brand, quantity, origin, imageUrl, false);
    }
}
