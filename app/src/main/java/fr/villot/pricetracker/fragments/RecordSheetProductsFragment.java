package fr.villot.pricetracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fr.villot.pricetracker.model.Product;

public class RecordSheetProductsFragment extends ProductsFragment {

    private Long recordSheetId;

    public static RecordSheetProductsFragment newInstance(long recordSheetId) {
        RecordSheetProductsFragment fragment = new RecordSheetProductsFragment();
        Bundle args = new Bundle();
        args.putLong("record_sheet_id", recordSheetId);
        fragment.setArguments(args);
        return fragment;
    }

    public static RecordSheetProductsFragment newInstance() {
        return new RecordSheetProductsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // TODO: Récupérer les données du Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            recordSheetId = bundle.getLong("record_sheet_id");
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Effectuez ici des opérations spécifiques au fragment de relevé de prix
    }

    protected List<Product> getProducts() {
        if (recordSheetId != null)
            return databaseHelper.getProductsOnRecordSheet(recordSheetId);
        else
            return super.getProducts();
    }

}
