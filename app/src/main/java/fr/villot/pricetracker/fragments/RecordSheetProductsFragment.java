package fr.villot.pricetracker.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import fr.villot.pricetracker.model.PriceRecord;
import fr.villot.pricetracker.model.Product;

public class RecordSheetProductsFragment extends ProductsFragment {

    private Long recordSheetId;

    public static RecordSheetProductsFragment newInstance(long recordSheetId) {
        Log.w("RecordSheetProductsFragment", "newInstance()");

        RecordSheetProductsFragment fragment = new RecordSheetProductsFragment();
        Bundle args = new Bundle();
        args.putLong("record_sheet_id", recordSheetId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.w("RecordSheetProductsFragment", "onCreateView()");

        View view = super.onCreateView(inflater, container, savedInstanceState);

        // TODO: Récupérer les données du Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            recordSheetId = bundle.getLong("record_sheet_id",-1);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.w("RecordSheetProductsFragment", "onViewCreated()");

        super.onViewCreated(view, savedInstanceState);
    }

    protected List<Product> getProducts() {
        Log.w("RecordSheetProductsFragment", "getProducts(), recordSheetId : " + recordSheetId);
        if (recordSheetId != null && recordSheetId != -1)
            return databaseHelper.getProductsOnRecordSheet(recordSheetId);
        else
            return super.getProducts();
    }

    protected void addOrUpdateProduct(Product product) {
        super.addOrUpdateProduct(product);

        // Demander le prix à l'utilisateur et l'enregistrer dans la base de données
        showPriceInputDialogAndUpdateDatabase(product);
    }

    protected void handleBarcodeScanResult(String barcode) {

        // Si le produit existe on l'ajoute à la recordsheet
        // Sinon on propose à l'utilisateur de créer le produit.
        Product product = databaseHelper.getProductFromBarCode(barcode);
        if (product != null) {
            showPriceInputDialogAndUpdateDatabase(product);
        }
        else {
            super.getProductDataFromOpenFoodFacts(barcode, false);
        }
    }

    protected void updateProductListViewFromDatabase(boolean lastItemDisplayed) {
        super.updateProductListViewFromDatabase(lastItemDisplayed);
    }

    private void showPriceInputDialogAndUpdateDatabase(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Saisir le prix");

        // Ajoutez un champ de texte pour la saisie du prix
        final EditText priceEditText = new EditText(getActivity());
        priceEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(priceEditText);

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

        builder.show();
    }


}
