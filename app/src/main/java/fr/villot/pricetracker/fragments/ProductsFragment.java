package fr.villot.pricetracker.fragments;

import static android.app.ProgressDialog.show;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
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
import fr.villot.pricetracker.activities.MainActivity;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.ProductAdapter;
import fr.villot.pricetracker.utils.DatabaseHelper;
import fr.villot.pricetracker.utils.OpenFoodFactsAPIManager;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;

public class ProductsFragment extends Fragment {
    private static ProductsFragment instance;
    protected DatabaseHelper databaseHelper;
    private RecyclerView productRecyclerView;
    protected ProductAdapter productAdapter;
    protected List<Product> productList;

    private ProgressDialog progressDialog;

    private FloatingActionButton fabAdd;
    private static final String PRODUCT_SELECTION_KEY = "product_selection";

    // Type de boite de dialogue
    public enum DialogType {
        DIALOG_TYPE_ADD,
        DIALOG_TYPE_UPDATE,
        DIALOG_TYPE_ALREADY_EXIST,
        DIALOG_TYPE_INFO
    }


    public static ProductsFragment getInstance() {
        if (instance == null) {
            instance = new ProductsFragment();
        }
        return instance;
    }

    // Gestion du resultat de l'activité de scan
    private final ActivityResultLauncher<ScanOptions> barcodeScannerLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Snackbar.make(getView(),"Scan annulé", Snackbar.LENGTH_SHORT).show();

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

        // Gestion de la selection d'items
        SelectionTracker<Long> selectionTracker = new SelectionTracker.Builder<>(
                PRODUCT_SELECTION_KEY,
                productRecyclerView,
                new StableIdKeyProvider(productRecyclerView),
                new MyDetailsLookup(productRecyclerView),
                StorageStrategy.createLongStorage()
        ).build();
        productAdapter.setSelectionTracker(selectionTracker);

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                // Réagir aux changements de sélection ici
                int numSelected = selectionTracker.getSelection().size();
                if (numSelected == 0) {
                    ((MainActivity) requireActivity()).setSelectionMode(getInstance(),false);
                    fabAdd.setVisibility(View.VISIBLE);
                }
                else if (numSelected == 1) {
                    ((MainActivity) requireActivity()).setSelectionMode(getInstance(),true);
                    String selectionCount = String.valueOf(numSelected) + " produit";
                    ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(selectionCount);
                    fabAdd.setVisibility(View.INVISIBLE);
                }
                else {
                    String selectionCount = String.valueOf(numSelected) + " produits";
                    ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(selectionCount);
                }
            }
        });

        // Gestion du click sur un produit
        productAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener<Product>() {
            @Override
            public void onItemClick(Product product) {
                showUserQueryDialogBox(product, DialogType.DIALOG_TYPE_INFO);


//                // Créer une boîte de dialogue de confirmation
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle("Informations sur le produit");
//                builder.setMessage("Voulez-vous ouvrir un navigateur pour afficher les détails de ce produit ?");
//
//                builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String barcode = product.getBarcode();
//                        String url = "https://world.openfoodfacts.org/product/" + barcode;
//
//                        // Lancer le navigateur
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                        startActivity(intent);
//                    }
//                });
//
//                builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // L'utilisateur a choisi de ne pas ouvrir le navigateur, vous pouvez ajouter ici d'autres actions si nécessaire.
//                    }
//                });
//
//                AlertDialog dialog = builder.create();
//                dialog.show();
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

        if (lastItemDisplayed) {
            // Positionnement de la ListView en dernier item pour voir le produit ajouté.
            int dernierIndice = productAdapter.getItemCount() - 1;
            productRecyclerView.smoothScrollToPosition(dernierIndice);
        }
    }

    protected void handleBarcodeScanResult(String barcode) {

        // Si le produit existe on propose une mise à jour des données
        // Sinon on propose à l'utilisateur de créer le produit.
        Product product = databaseHelper.getProductFromBarCode(barcode);
        if (product != null) {
            showUserQueryDialogBox(product,DialogType.DIALOG_TYPE_ALREADY_EXIST);
        }
        else {
            getProductDataFromOpenFoodFacts(barcode, false);
        }
    }

    protected void getProductDataFromOpenFoodFacts(String barcode, boolean productAlreadyExist) {

        showProgressDialog("Merci de patienter...");
        OpenFoodFactsAPIManager.getAsyncProductData(barcode, new OpenFoodFactsAPIManager.OnProductDataReceivedListener() {
            @Override
            public void onProductDataReceived(Product product) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Handle the received product data
                        if (product != null) {
                            progressDialog.dismiss();
                            if (productAlreadyExist)
                                showUserQueryDialogBox(product, DialogType.DIALOG_TYPE_UPDATE);
                            else
                                showUserQueryDialogBox(product, DialogType.DIALOG_TYPE_ADD);

                        }
                    }
                });
            }

            @Override
            public void onProductDataError(String error) {
                final String finalError = error;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();

                        // Handle the received product data
                        if (finalError != null) {
                            Toast.makeText(getActivity(), finalError, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    View getProductViewForDialog(Product product) {
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

        return dialogView;
    }

    private void showUserQueryDialogBox(Product product, DialogType dialogType) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(getProductViewForDialog(product));

        String title = null;
        String message = null;
        switch (dialogType) {
            case DIALOG_TYPE_ADD:
                title = "Ajouter le produit ?";
                break;
            case DIALOG_TYPE_UPDATE:
                title = "Mettre à jour le produit ?";
                break;
            case DIALOG_TYPE_INFO:
                title = "Lancer un navigateur ?";
                message = "Souhaitez vous visualiser les détails de ce produit sur le site OpenFoodFacts ?";
                break;
            case DIALOG_TYPE_ALREADY_EXIST:
                title = "Produit déjà existant";
                message = "Souhaitez-vous effectuer une mise à jour des données de ce produit ?";
                break;
        }

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (dialogType) {
                    case DIALOG_TYPE_ADD:
                    case DIALOG_TYPE_UPDATE:
                        // Ajouter le produit à la base de données et à la ListView
                        addOrUpdateProduct(product);

                        // Afficher la nouvelle liste avec ce produit en fin de liste.
                        updateProductListViewFromDatabase(true);
                        break;
                    case DIALOG_TYPE_INFO:
                        String barcode = product.getBarcode();
                        String url = "https://world.openfoodfacts.org/product/" + barcode;

                        // Lancer le navigateur
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        break;
                    case DIALOG_TYPE_ALREADY_EXIST:
                        getProductDataFromOpenFoodFacts(product.getBarcode(), true);
                        break;

                }

            }
        });

        builder.setNegativeButton("Non", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    protected void addOrUpdateProduct(Product product) {
        databaseHelper.addOrUpdateProduct(product);
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false); // Empêche l'utilisateur de fermer la boîte de dialogue en cliquant en dehors
        progressDialog.show();
    }

    public void clearSelection() {
        // Logique pour effacer la sélection
        if (productAdapter != null && productAdapter.getSelectionTracker() != null) {
            productAdapter.getSelectionTracker().clearSelection();
            fabAdd.setVisibility(View.VISIBLE);
        }
    }

    public void deleteSelectedItems() {
        if (productAdapter != null && productAdapter.getSelectionTracker() != null) {

            Selection<Long> selection = productAdapter.getSelectionTracker().getSelection();

            String toDelete = new String();
            for (Long selectedItem : selection) {
                Product product = productList.get(selectedItem.intValue());
//                databaseHelper.deleteProduct(product.getId());
                toDelete += product.getName() + " ";
            }

            Snackbar.make(getView(),"Product : " + toDelete, Snackbar.LENGTH_SHORT).show();

            // Mettre à jour la liste après la suppression
            updateProductListViewFromDatabase(false);
            clearSelection();
        }
    }

}