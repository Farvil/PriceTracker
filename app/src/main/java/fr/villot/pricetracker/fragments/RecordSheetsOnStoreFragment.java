package fr.villot.pricetracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fr.villot.pricetracker.model.RecordSheet;

public class RecordSheetsOnStoreFragment extends RecordSheetsFragment {

    private int storeId = -1;

    public static RecordSheetsOnStoreFragment newInstance(int storeId) {
        RecordSheetsOnStoreFragment fragment = new RecordSheetsOnStoreFragment();
        Bundle args = new Bundle();
        args.putInt("store_id", storeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Récupération des données du Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            storeId = bundle.getInt("store_id",-1);
        }
        return view;
    }

    protected List<RecordSheet> getRecordSheets() {
        if (storeId != -1)
            return databaseHelper.getRecordSheetsOnStore(storeId);
        else
            return super.getRecordSheets();
    }

}
