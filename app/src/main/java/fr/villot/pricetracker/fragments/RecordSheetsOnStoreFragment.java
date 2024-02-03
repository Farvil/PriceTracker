package fr.villot.pricetracker.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import fr.villot.pricetracker.interfaces.OnStoreChangedListener;
import fr.villot.pricetracker.model.RecordSheet;

public class RecordSheetsOnStoreFragment extends RecordSheetsFragment {

    private int storeId = -1;

    private OnStoreChangedListener mOnStoreChangedListener;


    public static RecordSheetsOnStoreFragment newInstance(int storeId) {
        RecordSheetsOnStoreFragment fragment = new RecordSheetsOnStoreFragment();
        Bundle args = new Bundle();
        args.putInt("store_id", storeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verification que l'activité hôte implémente l'interface
        if (context instanceof OnStoreChangedListener) {
            mOnStoreChangedListener = (OnStoreChangedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " doit implémenter OnStoreNameChangedListener");
        }

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

    // Méthode appelée lors du changement sur un magasin
    private void notifyStoreChanged(int storeId) {
        if (mOnStoreChangedListener != null) {
            mOnStoreChangedListener.onStoreChanged(storeId);
        }
    }

    @Override
    public void deleteSelectedItems() {
        super.deleteSelectedItems();

        // En cas de suppression d'un relevé de prix sur un magasin, il faut rafraichir le fragment des relevés de prix de l'activité principale
        notifyStoreChanged(storeId);
    }
}
