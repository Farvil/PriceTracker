package fr.villot.pricetracker.fragments;

import static android.app.ProgressDialog.show;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.squareup.picasso.Picasso;

import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.activities.BarCodeScannerActivity;
import fr.villot.pricetracker.adapters.ProductAdapter;
import fr.villot.pricetracker.utils.DatabaseHelper;
import fr.villot.pricetracker.utils.OpenFoodFactsAPIManager;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;

public class ProductsFragment extends Fragment {

    protected DatabaseHelper databaseHelper;
    private RecyclerView productListView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    private FloatingActionButton fabAdd;

    public static ProductsFragment newInstance() {
        return new ProductsFragment();
    }

    // Gestion du resultat de l'activité de scan
    private final ActivityResultLauncher<ScanOptions> barcodeScannerLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(getActivity(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    handleBarcodeScanResult(result.getContents());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        // TODO: Gerer une progressbar

        // Recupération des vues
        productListView = view.findViewById(R.id.productRecyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        productListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Récupération des produits dans la base de données.
        productList = getProducts();

        // Adapter entre ListView et Produit.
        productAdapter = new ProductAdapter(getActivity(), productList);
        productListView.setAdapter(productAdapter);

        // Gestion du click sur un produit
        productAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                Snackbar.make(productListView, "TODO : Afficher les listes des relevés de prix associées au produit : "
                        + product.getBarcode(),
                        Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onItemLongClick(Product product) {
                Snackbar.make(productListView, "TODO : Gestion du long press pour : "
                                + product.getBarcode(),
                        Snackbar.LENGTH_LONG).show();
            }
        });

        // Action du bouton flottant
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanOptions options = new ScanOptions();
                options.setCaptureActivity(BarCodeScannerActivity.class);
                options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES);
                options.setPrompt("Scanner un code barre");
                options.setOrientationLocked(false);
                options.setBeepEnabled(true);
                options.setBarcodeImageEnabled(false);
                barcodeScannerLauncher.launch(options);
            }
        });

        return view;
    }

    protected List<Product> getProducts() {
        return databaseHelper.getAllProducts();
    }

    private void updateProductListViewFromDatabase(boolean lastItemDisplayed) {
        // Obtenir la liste des produits à partir de la base de données
        productList = databaseHelper.getAllProducts();

        // Mise à jour de la liste
        productAdapter.setProductList(productList);

        if (lastItemDisplayed) {
            // Positionnement de la ListView en dernier item pour voir le produit ajouté.
            int dernierIndice = productAdapter.getItemCount() - 1;
            productListView.smoothScrollToPosition(dernierIndice);
        }
    }

    private void handleBarcodeScanResult(String barcode) {
        // Appeler l'API OpenFoodFacts via OpenFoodFactsAPIManager dans un thread
        // progressBar.setVisibility(View.VISIBLE);
        OpenFoodFactsAPIManager.getAsyncProductData(barcode, new OpenFoodFactsAPIManager.OnProductDataReceivedListener() {
            @Override
            public void onProductDataReceived(Product product) {

                final Product finalProduct = product; // Déclarer une variable finale pour capturer la référence du produit

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Handle the received product data
                        if (finalProduct != null) {
//                                progressBar.setVisibility(View.GONE);
                            showUserQueryDialogBox(finalProduct);
                        }
                    }
                });
            }

            private void showUserQueryDialogBox(Product product) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View dialogView = inflater.inflate(R.layout.item_product, null);

                // Références des vues dans le layout de la boîte de dialogue
                TextView productBarcodeTextView = dialogView.findViewById(R.id.productBarcodeTextView);
                ImageView productImageView = dialogView.findViewById(R.id.productImageView);
                TextView productNameTextView = dialogView.findViewById(R.id.productNameTextView);
                TextView productBrandTextView = dialogView.findViewById(R.id.productBrandTextView);
                TextView productQuantityTextView = dialogView.findViewById(R.id.productQuantityTextView);

                // Afficher les détails du produit dans les vues
                productBarcodeTextView.setText(product.getBarcode());
                productNameTextView.setText(product.getName());
                productBrandTextView.setText(product.getBrand());
                productQuantityTextView.setText(product.getQuantity());

                // Afficher l'image
                Picasso.get().load(product.getImageUrl()).into(productImageView);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(dialogView);
                builder.setTitle("Ajouter ou mettre à jour le produit ?");

                builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ajouter le produit à la base de données et à la ListView
                        databaseHelper.addOrUpdateProduct(product);

                        // Afficher la nouvelle liste avec ce produit en fin de liste.
                        updateProductListViewFromDatabase(true);
                    }
                });

                builder.setNegativeButton("Non", null);

                AlertDialog dialog = builder.create();
                dialog.show();

            }

            @Override
            public void onProductDataError(String error) {
                final String finalError = error;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Handle the received product data
                        if (finalError != null) {
//                                progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), finalError, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

}