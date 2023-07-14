package fr.villot.pricetracker.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.activities.PriceRecordActivity;
import fr.villot.pricetracker.adapters.RecordSheetAdapter;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.utils.DatabaseHelper;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.adapters.StoreAdapter;
import fr.villot.pricetracker.model.Store;

public class StoresFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private RecyclerView storeRecyclerView;
    private StoreAdapter storeAdapter;
    private List<Store> storeList;
    private FloatingActionButton fabAdd;


//    private static final Logger logger = Logger.getLogger(StoresFragment.class.getName());

    public static StoresFragment newInstance() {
        return new StoresFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stores, container, false);

        storeRecyclerView = view.findViewById(R.id.storeRecyclerView);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        storeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Récupération des magasins dans la base de données
        storeList = getStores();

        // Adapter entre ListView et Produit.
        storeAdapter = new StoreAdapter(getActivity(), storeList);
        storeRecyclerView.setAdapter(storeAdapter);

        storeAdapter.setOnItemClickListener(new StoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object item) {
                if (item instanceof Store) {
                    Store store = (Store) item;
                    // TODO : Afficher la liste des relevés de prix pour ce magasin.
                    Snackbar.make(storeRecyclerView, "TODO : Afficher la liste des relevés de prix pour ce magasin : "
                                    + store.getName(),
                            Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onItemLongClick(Object item) {
                if (item instanceof Store) {
                    Store store = (Store) item;
                    // TODO : Gérer le long click
                    Snackbar.make(storeRecyclerView, "TODO : Gérer le click long pour ce magasin : "
                                    + store.getName(),
                            Snackbar.LENGTH_LONG).show();
                }
            }

        });

        fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddStoreDialog(requireActivity());
            }
        });


        return view;
    }

    private List<Store> getStores() {
        return databaseHelper.getAllStores();
    }

    private void updateStoreListViewFromDatabase(boolean lastItemDisplayed) {
        // Obtenir la liste des produits à partir de la base de données
        storeList = databaseHelper.getAllStores();

        // Ajouter les produits à l'adaptateur
        storeAdapter.setItemList(storeList);

        if (lastItemDisplayed) {
            // Positionnement de la ListView en dernier item pour voir le produit ajouté.
            int dernierIndice = storeAdapter.getItemCount() - 1;
            storeRecyclerView.smoothScrollToPosition(dernierIndice);
        }
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
}
