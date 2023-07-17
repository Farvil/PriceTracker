package fr.villot.pricetracker.fragments;

import static android.app.ProgressDialog.show;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
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
    private RecyclerView productRecyclerView;
    protected ProductAdapter productAdapter;
    protected List<Product> productList;

    private FloatingActionButton fabAdd;

    public static ProductsFragment newInstance() {
        Log.w("ProductsFragment", "newInstance()");

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
        Log.w("ProductsFragment", "onCreateView()");

        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.w("ProductsFragment", "onViewCreated()");

        super.onViewCreated(view, savedInstanceState);

        // TODO: Gerer une progressbar

        // Recupération des vues
        productRecyclerView = view.findViewById(R.id.productRecyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        productRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Récupération des produits dans la base de données.
        productList = getProducts();

        // Adapter entre RecyclerView et Produit.
        productAdapter = new ProductAdapter(getActivity(), productList);
        productRecyclerView.setAdapter(productAdapter);

        // Gestion du click sur un produit
        productAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object item) {
                if (item instanceof Product) {
                    Product product = (Product) item;
                Snackbar.make(productRecyclerView, "TODO : Afficher les listes des relevés de prix associées au produit : "
                        + product.getBarcode(),
                        Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onItemLongClick(Object item) {
                if (item instanceof Product) {
                    Product product = (Product) item;
                Snackbar.make(productRecyclerView, "TODO : Gestion du long press pour : "
                                + product.getBarcode(),
                        Snackbar.LENGTH_LONG).show();
                }
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

    }

    protected List<Product> getProducts() {
        Log.w("ProductsFragment", "getProducts()");
        return databaseHelper.getAllProducts();
    }

    protected void updateProductListViewFromDatabase(boolean lastItemDisplayed) {
        Log.w("ProductsFragment", "updateProductListViewFromDatabase()");
        // Obtenir la liste des produits à partir de la base de données
        productList = getProducts();

        // Mise à jour de la liste
        productAdapter.setItemList(productList);

//        if (lastItemDisplayed) {
//            // Positionnement de la ListView en dernier item pour voir le produit ajouté.
//            int dernierIndice = productAdapter.getItemCount() - 1;
//            productRecyclerView.smoothScrollToPosition(dernierIndice);
//        }
    }

    private void handleBarcodeScanResult(String barcode) {
        // Appeler l'API OpenFoodFacts via OpenFoodFactsAPIManager dans un thread
        // progressBar.setVisibility(View.VISIBLE);
        OpenFoodFactsAPIManager.getAsyncProductData(barcode, new OpenFoodFactsAPIManager.OnProductDataReceivedListener() {
            @Override
            public void onProductDataReceived(Product product) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Handle the received product data
                        if (product != null) {
//                                progressBar.setVisibility(View.GONE);
                            showUserQueryDialogBox(product);
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
                        addOrUpdateProduct(product);

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

    protected void addOrUpdateProduct(Product product) {
        databaseHelper.addOrUpdateProduct(product);
    }

}