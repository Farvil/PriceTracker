package fr.villot.pricetracker.fragments;

import static fr.villot.pricetracker.fragments.ProductsFragment.DialogType.DIALOG_TYPE_ALREADY_EXIST;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.selection.Selection;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.PriceRecord;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;

public class ProductsOnRecordSheetFragment extends ProductsFragment {

    private Long recordSheetId;

    public static ProductsOnRecordSheetFragment newInstance(long recordSheetId) {
        Log.w("RecordSheetProductsFragment", "newInstance()");

        ProductsOnRecordSheetFragment fragment = new ProductsOnRecordSheetFragment();
        Bundle args = new Bundle();
        args.putLong("record_sheet_id", recordSheetId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.w("RecordSheetProductsFragment", "onCreateView()");

        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Récupération des données du Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            recordSheetId = bundle.getLong("record_sheet_id",-1);
        }
        return view;
    }

    protected List<Product> getProducts() {
        Log.w("RecordSheetProductsFragment", "getProducts(), recordSheetId : " + recordSheetId);
        if (recordSheetId != null && recordSheetId != -1)
            return databaseHelper.getProductsOnRecordSheet(recordSheetId);
        else
            return super.getProducts();
    }

    @Override
    protected void handleClickOnProduct(Product product) {
        showPriceInputDialogAndUpdateDatabase(product);
    }

    public void addOrUpdateProduct(Product product) {
        super.addOrUpdateProduct(product);

        // Demander le prix à l'utilisateur et l'enregistrer dans la base de données
        showPriceInputDialogAndUpdateDatabase(product);
    }

    protected void showUserQueryDialogBox(Product product, DialogType dialogType) {

        if (dialogType != DIALOG_TYPE_ALREADY_EXIST)
            super.showUserQueryDialogBox(product,dialogType);
        else {
            showPriceInputDialogAndUpdateDatabase(product);
        }

    }

    private void showPriceInputDialogAndUpdateDatabase(Product product) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getProductViewForDialog(product, R.layout.dialog_price_record);
        builder.setView(dialogView);

        EditText priceEditText = dialogView.findViewById(R.id.priceEditText);
        builder.setTitle("Saisir le prix du produit");
        builder.setCancelable(false);
        priceEditText.requestFocus();

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

                        updateProductListViewFromDatabase(true);

                        // Mettez à jour la liste des produits ou effectuez d'autres actions si nécessaires
                        // par exemple, rafraîchir l'interface utilisateur
                    } catch (NumberFormatException e) {
                        // La saisie n'est pas un nombre valide, vous pouvez afficher un message d'erreur
                        // ou prendre d'autres mesures appropriées.
                    }
                }
            }
        });

        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // L'utilisateur a annulé la saisie, rien à faire.
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deleteSelectedItems() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Voulez-vous vraiment retirer les produits selectionnés de ce relevé de prix ?");
        builder.setCancelable(false);

        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (productAdapter != null && productAdapter.getSelectionTracker() != null) {

                    Selection<Long> selection = productAdapter.getSelectionTracker().getSelection();

                    for (Long selectedItem : selection) {
                        Product product = productList.get(selectedItem.intValue());
                        try {
                            databaseHelper.deleteProductOnRecordSheet(product.getBarcode(), recordSheetId);
                        } catch (Exception e) {
                            Snackbar.make(getView(),"Erreur de suppression du produits dans le relevé " + recordSheetId + " !", Snackbar.LENGTH_SHORT).show();
                            break; // Ne tente pas d'autres suppressions en cas d'erreur
                        }
                    }

                    // Mettre à jour la liste après la suppression
                    updateProductListViewFromDatabase(false);
                    clearSelection();
                }

            }
        });

        builder.setNegativeButton("Non", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    public void shareRecordSheet() {

        // Créer un fichier CSV
        File csvFile = createCsvFile();

        // Remplir le fichier CSV avec les données
        fillCsvFile(csvFile);

        // Partager le fichier CSV
        shareCsvFile(csvFile);
    }


    private File createCsvFile() {
        String fileName = "record_sheet_export.csv";

        File privateRootDir = requireActivity().getFilesDir();
        File exportDir = new File(privateRootDir, "export");

        // Créer le répertoire s'il n'existe pas
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        return new File(exportDir, fileName);
    }

    private void fillCsvFile(File csvFile) {
        try (FileWriter writer = new FileWriter(csvFile)) {
            // En-têtes CSV
            writer.append("Nom du relevé de Prix,Date,Nom du magasin,Localisation du magasin, Code barre,Nom du produit,Marque,Quantité,Image URL,Prix\n");

            // Récupérer les produits et magasin associés à la RecordSheet
            List<Product> products = databaseHelper.getProductsOnRecordSheet(recordSheetId);
            RecordSheet recordSheet = databaseHelper.getRecordSheetById(recordSheetId);
            Store store = databaseHelper.getStoreById(recordSheet.getStoreId());

            // Remplir le fichier CSV avec les données de chaque produit
            for (Product product : products) {
                writer.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        recordSheet.getName(),
                        recordSheet.getDate(),
                        store.getName(),
                        store.getLocation(),
                        product.getBarcode(),
                        product.getName(),
                        product.getBrand(),
                        product.getQuantity(),
                        product.getImageUrl(),
                        product.getPrice()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void shareCsvFile(File csvFile) {

        // Uri du fichier à partager via le FileProvider defini dans le manifest.
        Uri fileUri = FileProvider.getUriForFile(requireActivity(), getContext().getPackageName() + ".provider", csvFile);

        // Intent de partage en ajoutant les droits temporaires d'accès au fichier
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        startActivity(Intent.createChooser(shareIntent, "Partager via"));
    }

}