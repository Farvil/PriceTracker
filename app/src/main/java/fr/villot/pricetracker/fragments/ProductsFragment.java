package fr.villot.pricetracker.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.activities.BarCodeScannerActivity;
import fr.villot.pricetracker.activities.RecordSheetOnProductActivity;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.ProductAdapter;
import fr.villot.pricetracker.interfaces.OnSelectionChangedListener;
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

    private OnSelectionChangedListener mOnSelectionChangedListener;

    public void selectAllItems() {
        if (productAdapter != null && productAdapter.getSelectionTracker() != null) {
            List<Long> selectedItems = new ArrayList<>();
            for (int i = 0 ; i < productAdapter.getItemCount() ; i++) {
                selectedItems.add((long) i);
            }
            productAdapter.getSelectionTracker().setItemsSelected(selectedItems, true);
        }
    }


    // Type de boite de dialogue
    public enum ProductsFragmentDialogType {
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

    // Gestion du résultat de l'activité de scan
    private ActivityResultLauncher<ScanOptions> barcodeScannerLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Ajout du listener OnSelectionChangedListener
        if (context instanceof OnSelectionChangedListener) {
            mOnSelectionChangedListener = (OnSelectionChangedListener) context;
        } else {
            throw new RuntimeException(context + " doit implémenter OnSelectionChangedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enregistrement du launcher
        barcodeScannerLauncher = registerForActivityResult(new ScanContract(),
                result -> {
                    if(result.getContents() == null) {
                        Snackbar.make(requireView(),"Scan annulé", Snackbar.LENGTH_SHORT).show();

                    } else {
                        handleBarcodeScanResult(result.getContents());
                    }
                });
    }

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

                int numSelected = selectionTracker.getSelection().size();

                // On informe l'activité parente (MainActivity ou PriceRecordActivity) du changement de sélection
                if (mOnSelectionChangedListener != null) {
                    mOnSelectionChangedListener.onSelectionChanged(getInstance(), numSelected);
                }

                // On masque l'icône flottant si une selection est en cours.
                if (numSelected == 0)
                    fabAdd.setVisibility(View.VISIBLE);
                else
                    fabAdd.setVisibility(View.INVISIBLE);
            }

        });

        // Gestion du click sur un produit
        productAdapter.setOnItemClickListener(this::handleClickOnProduct);

        // Click bouton flottant
        fabAdd.setOnClickListener(v -> launchScanActivity());

        // Long Click bouton flottant
        fabAdd.setOnLongClickListener(view1 -> {
            showFabAddContextMenu(view1);
            return true;
        });

    }

    private void launchScanActivity() {
            ScanOptions options = new ScanOptions();
            options.setCaptureActivity(BarCodeScannerActivity.class);
            options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES);
            options.setPrompt("Scannez un code barre");
            options.setOrientationLocked(false);
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(false);

            // Vérification et lancement
            if (barcodeScannerLauncher != null) {
                barcodeScannerLauncher.launch(options);
            } else {
                Log.e("ProductsFragment", "barcodeScannerLauncher non enregistré");
            }
    }

    protected void handleClickOnProduct(Product product) {
        Intent intent = new Intent(getActivity(), RecordSheetOnProductActivity.class);
        intent.putExtra("barcode", product.getBarcode());
        startActivity(intent);
//        showUserQueryDialogBox(product, ProductsFragmentDialogType.DIALOG_TYPE_INFO);
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
            productRecyclerView.smoothScrollToPosition(productAdapter.getLastItemPosition());
        }
    }

    protected void handleBarcodeScanResult(String barcode) {

        // Si le produit existe on propose une mise à jour des données
        // Sinon on propose à l'utilisateur de créer le produit.
        Product product = databaseHelper.getProductFromBarCode(barcode);
        if (product != null) {
            showUserQueryDialogBox(product, ProductsFragmentDialogType.DIALOG_TYPE_ALREADY_EXIST);
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

                getActivity().runOnUiThread(() -> {
                    // Handle the received product data
                    if (product != null) {
                        progressDialog.dismiss();
                        if (productAlreadyExist)
                            showUserQueryDialogBox(product, ProductsFragmentDialogType.DIALOG_TYPE_UPDATE);
                        else
                            showUserQueryDialogBox(product, ProductsFragmentDialogType.DIALOG_TYPE_ADD);

                    }
                });
            }

            @Override
            public void onProductDataError(String error) {
                final String finalError = error;

                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();

                    // Handle the received product data
                    if (finalError != null) {
                        Toast.makeText(getActivity(), finalError, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    View getProductViewForDialog(Product product, final int layoutResource) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(layoutResource, null);

        // Références des vues dans le layout de la boîte de dialogue
        TextView productBarcodeTextView = dialogView.findViewById(R.id.productBarcodeTextView);
        ImageView productImageView = dialogView.findViewById(R.id.productImageView);
        TextView productNameTextView = dialogView.findViewById(R.id.productNameTextView);
        LinearLayout productBrandZone = dialogView.findViewById(R.id.productBrandZone);
        TextView productBrandTextView = dialogView.findViewById(R.id.productBrandTextView);
        LinearLayout productQuantityZone = dialogView.findViewById(R.id.productQuantityZone);
        TextView productQuantityTextView = dialogView.findViewById(R.id.productQuantityTextView);
        LinearLayout productOriginZone = dialogView.findViewById(R.id.productOriginZone);
        TextView productOriginTextView = dialogView.findViewById(R.id.productOriginTextView);
        LinearLayout productPriceZone = dialogView.findViewById(R.id.productPriceZone);


        // Affichage du barcode
        productBarcodeTextView.setText(product.getBarcode());

        // Affichage du nom du produit s'il existe
        String productName = product.getName();
        if (productName != null && !(productName.isEmpty())) {
            productNameTextView.setText(product.getName());
        }

        // Affichage de la marque du produit si elle existe
        String productBrand = product.getBrand();
        if (productBrand != null && !(productBrand.isEmpty())) {
            productBrandTextView.setText(product.getBrand());
        }
        else {
            productBrandZone.setVisibility(View.GONE);
        }

        // Affichage de la quantité du produit si elle existe
        String productQuantity = product.getQuantity();
        if (productQuantity != null && !(productQuantity.isEmpty())) {
            productQuantityTextView.setText(product.getQuantity());
        }
        else {
            productQuantityZone.setVisibility(View.GONE);
        }

        // Affichage de l'origine du produit si elle existe
        String productOrigin = product.getOrigin();
        if (productOrigin != null && !(productOrigin.isEmpty())) {
            productOriginTextView.setText(product.getOrigin());
        } else {
            productOriginZone.setVisibility(View.GONE);
        }

        // Masquage du prix
        productPriceZone.setVisibility(View.GONE);

        // Afficher l'image
        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !(imageUrl.isEmpty())) {
            Picasso.get().load(imageUrl).into(productImageView);
        }

        return dialogView;
    }

    protected void showUserQueryDialogBox(Product product, ProductsFragmentDialogType productsFragmentDialogType) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(getProductViewForDialog(product, R.layout.item_product));

        String title = null;
        String message = null;
        switch (productsFragmentDialogType) {
            case DIALOG_TYPE_ADD:
                title = "Ajouter le produit ?";
                break;
            case DIALOG_TYPE_UPDATE:
                title = "Mettre à jour les données du produit ?";
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
        builder.setCancelable(false);


        builder.setPositiveButton("Oui", (dialog, which) -> {

            switch (productsFragmentDialogType) {
                case DIALOG_TYPE_ADD:
                    // Ajouter le produit à la base de données et à la ListView
                    addOrUpdateProduct(product);

                    // Afficher la nouvelle liste avec ce produit en fin de liste.
                    updateProductListViewFromDatabase(true);
                    break;
                case DIALOG_TYPE_UPDATE:
                    // Ajouter le produit à la base de données et à la ListView
                    databaseHelper.updateProduct(product);

                    // Afficher la nouvelle liste avec ce produit en fin de liste.
                    updateProductListViewFromDatabase(false);
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
            Queue<Product> productsToDelete = new LinkedList<>();

            // Ajout des magasins à supprimer à la file d'attente
            for (Long selectedItem : selection) {
                Product product = productList.get(selectedItem.intValue());
                productsToDelete.offer(product);
            }

            // Suppression des magasins de la file d'attente
            deleteProductsInQueue(productsToDelete);

            // Annule le mode de selection
            clearSelection();
        }
    }

    private void deleteProductsInQueue(Queue<Product> productsToDelete) {
        if (!productsToDelete.isEmpty()) {
            Product product = productsToDelete.poll();

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            assert product != null;
            builder.setMessage("Voulez-vous vraiment supprimer le produit " + product.getName() + " ?");
            builder.setCancelable(false);

            builder.setPositiveButton("Oui", (dialog, which) -> {
                // Suppression uniquement si pas de dépendances avec des priceRecords.
                if (!databaseHelper.hasPriceRecordsOnProduct(product.getBarcode())) {
                    databaseHelper.deleteProduct(product.getBarcode());
                    updateProductListViewFromDatabase(false);
                } else {
                    Snackbar.make(requireView(), "Suppression impossible car au moins un relevé de prix associée au produit", Snackbar.LENGTH_SHORT).show();
                }

                // Appeler la suppression des magasins restants dans la file d'attente
                deleteProductsInQueue(productsToDelete);
            });

            builder.setNegativeButton("Non", (dialog, which) -> {
                // Si l'utilisateur clique sur "Non", passage au magasin suivant dans la file d'attente
                deleteProductsInQueue(productsToDelete);
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    private void showFabAddContextMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.fab_add_context_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_scan_barcode:
                    launchScanActivity();
                    return true;
                case R.id.menu_manual_entry:
                    showManualBarcodeInputDialog();
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void showManualBarcodeInputDialog() {
        // Création boite de dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_product, null);
        builder.setView(dialogView);

        builder.setTitle("Ajouter un produit");
        EditText barcodeEditText = dialogView.findViewById(R.id.productBarcodeEditText);

        // Configuration des boutons de la boîte de dialogue
        builder.setPositiveButton("Valider", (dialog, which) -> {
            String barcode = barcodeEditText.getText().toString().trim();

            // Vérification de la validité du code barre et ajout du produit
            if (isValidBarcode(barcode)) {
                handleBarcodeScanResult(barcode);
            } else {
                // Affiche un message d'erreur si le code-barres n'est pas valide
                Toast.makeText(getActivity(), "Code-barres non valide", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        // Affiche la boîte de dialogue
        builder.show();
    }

    private boolean isValidBarcode(String barcode) {
        return barcode.matches("^[0-9]{12,}$");
    }

}