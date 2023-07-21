package fr.villot.pricetracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        // Ajout du produit dans la RecordSheet
        PriceRecord priceRecord = new PriceRecord();
        priceRecord.setRecordSheetId(recordSheetId);
        priceRecord.setProductBarcode(product.getBarcode());
        databaseHelper.addPriceRecord(priceRecord);
    }

    protected void handleBarcodeScanResult(String barcode) {

        // Si le produit existe on l'ajoute à la recordsheet
        // Sinon on propose à l'utilisateur de créer le produit.
        Product product = databaseHelper.getProductFromBarCode(barcode);
        if (product != null) {
            PriceRecord priceRecord = new PriceRecord();
            priceRecord.setRecordSheetId(recordSheetId);
            priceRecord.setProductBarcode(product.getBarcode());
            databaseHelper.addPriceRecord(priceRecord);

            super.updateProductListViewFromDatabase(true);
        }
        else {
            super.getProductDataFromOpenFoodFacts(barcode);
        }
    }

}
