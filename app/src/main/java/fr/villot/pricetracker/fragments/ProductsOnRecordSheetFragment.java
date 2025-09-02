package fr.villot.pricetracker.fragments;

import static fr.villot.pricetracker.fragments.ProductsFragment.ProductsFragmentDialogType.DIALOG_TYPE_ALREADY_EXIST;
import static fr.villot.pricetracker.fragments.ProductsFragment.ProductsFragmentDialogType.DIALOG_TYPE_INFO;
import static fr.villot.pricetracker.fragments.ProductsFragment.ProductsFragmentDialogType.DIALOG_TYPE_ORIGIN;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.selection.Selection;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.activities.PriceRecordActivity;
import fr.villot.pricetracker.model.PriceRecord;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.utils.CsvHelper;

public class ProductsOnRecordSheetFragment extends ProductsFragment {

    private int recordSheetId;

    private CsvHelper csvHelper;

    public static ProductsOnRecordSheetFragment newInstance(int recordSheetId, boolean readOnly) {
        Log.w("RecordSheetProductsFragment", "newInstance()");

        ProductsOnRecordSheetFragment fragment = new ProductsOnRecordSheetFragment();
        Bundle args = new Bundle();
        args.putInt("record_sheet_id", recordSheetId);
        args.putBoolean("read_only", readOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.w("RecordSheetProductsFragment", "onCreateView()");

        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Initialisation du csvHelper
        csvHelper = new CsvHelper(requireActivity(), "record_sheet_export.csv");

        // Récupération des données du Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            recordSheetId = bundle.getInt("record_sheet_id",-1);
            readOnlyMode = bundle.getBoolean("read_only", false);
        }
        return view;
    }

    protected List<Product> getProducts() {
        Log.w("RecordSheetProductsFragment", "getProducts(), recordSheetId : " + recordSheetId);
        if (recordSheetId != -1)
            return databaseHelper.getProductsOnRecordSheet(recordSheetId);
        else
            return super.getProducts();
    }

    @Override
    protected void handleClickOnProduct(Product product) {

        // Si l'origine du produit n'est pas vérifiée alors on demande une vérification
        if(!product.getOriginVerified()) {
            super.showUserQueryDialogBox(product, DIALOG_TYPE_ORIGIN);
        }

        showPriceInputDialogAndUpdateDatabase(product);
    }

    public void addOrUpdateProduct(Product product) {
        super.addOrUpdateProduct(product);

        // Demander le prix à l'utilisateur et l'enregistrer dans la base de données
        showPriceInputDialogAndUpdateDatabase(product);
    }

    protected void showUserQueryDialogBox(Product product, ProductsFragmentDialogType productsFragmentDialogType) {

        if (productsFragmentDialogType != DIALOG_TYPE_ALREADY_EXIST)
            super.showUserQueryDialogBox(product, productsFragmentDialogType);
        else {
            // Si l'origine du produit n'est pas vérifiée alors on demande une vérification
            if(!product.getOriginVerified()) {
                super.showUserQueryDialogBox(product, DIALOG_TYPE_ORIGIN);
            }

            // On demande le prix
            showPriceInputDialogAndUpdateDatabase(product);
        }

    }

    private void showPriceInputDialogAndUpdateDatabase(Product product) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View dialogView = getProductViewForDialog(product, R.layout.dialog_price_record, DIALOG_TYPE_INFO);
        builder.setView(dialogView);

        EditText priceEditText = dialogView.findViewById(R.id.priceEditText);
        builder.setTitle("Saisir le prix du produit");
        builder.setCancelable(false);
        priceEditText.requestFocus();

        builder.setPositiveButton("OK", (dialog, which) -> {
            String priceStr = priceEditText.getText().toString();
            if (!TextUtils.isEmpty(priceStr)) {
                try {
                    double price = Double.parseDouble(priceStr);

                    // Mettez à jour l'objet PriceRecord avec le prix saisi
                    PriceRecord priceRecord = new PriceRecord();
                    priceRecord.setRecordSheetId(recordSheetId);
                    priceRecord.setProductBarcode(product.getBarcode());
                    priceRecord.setPrice(price);

                    // Ajoutez ou mettez à jour le PriceRecord dans la base de données
                    databaseHelper.addOrUpdatePriceRecord(priceRecord);

                    // Rafraîchissement de la recyclerview aves les nouvelles données
                    updateProductListViewFromDatabase(true);

                    // Mise à jour des prix Min, Max et Moy dans l'activité RecordSheetOnProductActivity
                    if (requireActivity() instanceof PriceRecordActivity) {
                        ((PriceRecordActivity) requireActivity()).notifyProductModifiedFromRecordSheet(product.getBarcode());
                    }

                    // Mettez à jour la liste des produits ou effectuez d'autres actions si nécessaires
                    // par exemple, rafraîchir l'interface utilisateur
                } catch (NumberFormatException e) {
                    // La saisie n'est pas un nombre valide, vous pouvez afficher un message d'erreur
                    // ou prendre d'autres mesures appropriées.
                }
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> {
            // L'utilisateur a annulé la saisie, rien à faire.
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deleteSelectedItems() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage("Voulez-vous vraiment retirer les produits sélectionnés de ce relevé de prix ?");
        builder.setCancelable(false);

        builder.setPositiveButton("Oui", (dialog, which) -> {

            if (productAdapter != null && productAdapter.getSelectionTracker() != null) {

                Selection<Long> selection = productAdapter.getSelectionTracker().getSelection();

                for (Long selectedItem : selection) {
                    Product product = productList.get(selectedItem.intValue());
                    try {
                        databaseHelper.deleteProductOnRecordSheet(product.getBarcode(), recordSheetId);

                        // On notifie l'activité parente (uniquement PriceRecordActivity) de la suppression du produit du relevé
                        if (requireActivity() instanceof PriceRecordActivity) {
                            ((PriceRecordActivity) requireActivity()).notifyProductModifiedFromRecordSheet(product.getBarcode());
                        }
                    } catch (Exception e) {
                        Snackbar.make(requireView(),"Erreur de suppression du produits dans le relevé " + recordSheetId + " !", Snackbar.LENGTH_SHORT).show();
                        break; // Ne tente pas d'autres suppressions en cas d'erreur
                    }
                }

                // Mettre à jour la liste après la suppression
                updateProductListViewFromDatabase(false);
                clearSelection();
            }

        });

        builder.setNegativeButton("Non", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    public void shareRecordSheet() {
        RecordSheet recordSheetToShare = databaseHelper.getRecordSheetById(recordSheetId);

        csvHelper.fillCsvFileWithRecordSheet(recordSheetToShare);
        csvHelper.shareCsvFile();
    }

    public boolean exportRecordSheet(Uri uri) {

        RecordSheet recordSheetToExport = databaseHelper.getRecordSheetById(recordSheetId);

        // Initialisation du csvHelper
        CsvHelper csvHelper = new CsvHelper(requireActivity(), "export_releve_de_prix.csv");
        csvHelper.fillCsvFileWithRecordSheet(recordSheetToExport);
        return csvHelper.writeCsvFileToUri(uri);

    }

}
