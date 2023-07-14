package fr.villot.pricetracker.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.utils.DatabaseHelper;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.adapters.StoreAdapter;
import fr.villot.pricetracker.model.Store;

public class StoresFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private ListView storeListView;
    private FloatingActionButton fabAdd;

    private StoreAdapter storeAdapter;
    private List<Store> storeList;

    private static final Logger logger = Logger.getLogger(StoresFragment.class.getName());

    public static StoresFragment newInstance() {
        return new StoresFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stores, container, false);

        storeListView = view.findViewById(R.id.storeListView);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddStoreDialog(requireActivity());
            }
        });


        // Listener pour le click sur un item
        storeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO : Afficher la liste des relevés de prix pour ce magasin.
                Snackbar.make(view, "TODO : Afficher la liste des relevés de prix pour ce magasin.", Snackbar.LENGTH_SHORT).show();
            }
        });


        // Initialisation de la liste et de son adapter
        storeList = new ArrayList<>();
        storeAdapter = new StoreAdapter(getActivity(), storeList);
        storeListView.setAdapter(storeAdapter);

        // Mise à jour de la liste des magasins depuis la base de données
        updateStoreListViewFromDatabase(false);

        return view;
    }

    private void showAddStoreDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ajouter un nouveau magasin");

        // Create the layout for the dialog
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 0);

        // Create EditText fields for store name and location
        EditText nameEditText = new EditText(context);
        nameEditText.setHint("Nom du magasin");
        layout.addView(nameEditText);

        EditText locationEditText = new EditText(context);
        locationEditText.setHint("Localisation");
        layout.addView(locationEditText);

        builder.setView(layout);

        // Set up the buttons for Save and Cancel
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameEditText.getText().toString().trim();
                String location = locationEditText.getText().toString().trim();

                if (!name.isEmpty() && !location.isEmpty()) {
                    // Create a new Store object
                    Store newStore = new Store();
                    newStore.setName(name);
                    newStore.setLocation(location);

                    // Add the store to the database
                    databaseHelper.addStore(newStore);

                    updateStoreListViewFromDatabase(true);

                } else {
                    Snackbar.make(getView(),"Veuillez entrer un nom de magasin et un lieu", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void updateStoreListViewFromDatabase(boolean lastItemDisplayed) {

        // Obtenir la liste des magasins à partir de la base de données
        storeList = databaseHelper.getAllStores();

        // Ajouter les produits à l'adaptateur
        storeAdapter.clear();
        storeAdapter.addAll(storeList);

        if (lastItemDisplayed) {
            // Positionnement de la ListView en dernier item pour voir le magasin ajouté.
            int dernierIndice = storeAdapter.getCount() - 1;
            storeListView.setSelection(dernierIndice);
        }
    }
}
