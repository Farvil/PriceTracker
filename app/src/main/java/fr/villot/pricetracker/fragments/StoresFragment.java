package fr.villot.pricetracker.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.activities.MainActivity;
import fr.villot.pricetracker.activities.PriceRecordActivity;
import fr.villot.pricetracker.adapters.LogoAdapter;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.RecordSheetAdapter;
import fr.villot.pricetracker.model.LogoItem;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.utils.DatabaseHelper;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.adapters.StoreAdapter;
import fr.villot.pricetracker.model.Store;

public class StoresFragment extends Fragment {

    private static StoresFragment instance;
    private DatabaseHelper databaseHelper;
    private RecyclerView storeRecyclerView;
    public StoreAdapter storeAdapter;
    private List<Store> storeList;
    private FloatingActionButton fabAdd;



//    private static final Logger logger = Logger.getLogger(StoresFragment.class.getName());

    public static StoresFragment getInstance() {
        if (instance == null) {
            instance = new StoresFragment();
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stores, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        storeRecyclerView = view.findViewById(R.id.storeRecyclerView);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        storeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Récupération des magasins dans la base de données
        storeList = getStores();

        // Adapter entre ListView et Produit.
        storeAdapter = new StoreAdapter(getActivity(), storeList);
        storeRecyclerView.setAdapter(storeAdapter);

        SelectionTracker<Long> selectionTracker = new SelectionTracker.Builder<>(
                "store_selection",
                storeRecyclerView,
                new StableIdKeyProvider(storeRecyclerView),
                new MyDetailsLookup(storeRecyclerView),
                StorageStrategy.createLongStorage()
        ).build();
        storeAdapter.setSelectionTracker(selectionTracker);

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                // Réagir aux changements de sélection ici
                int numSelected = selectionTracker.getSelection().size();
                if (numSelected == 0) {
                    ((MainActivity) requireActivity()).setSelectionMode(false);
                    fabAdd.setVisibility(View.VISIBLE);
                }
                else if (numSelected == 1) {
                    ((MainActivity) requireActivity()).setSelectionMode(true);
                    String selectionCount = String.valueOf(numSelected) + " magasin";
                    ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(selectionCount);
                    fabAdd.setVisibility(View.INVISIBLE);
                }
                else {
                    String selectionCount = String.valueOf(numSelected) + " magasins";
                    ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(selectionCount);
                }
            }
        });


//        storeAdapter.setOnItemClickListener(new StoreAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(Object item) {
//                if (item instanceof Store) {
//                    Store store = (Store) item;
//                    // TODO : Afficher la liste des relevés de prix pour ce magasin.
//                    Snackbar.make(storeRecyclerView, "TODO : Afficher la liste des relevés de prix pour ce magasin : "
//                                    + store.getName(),
//                            Snackbar.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onItemLongClick(Object item) {
//                if (item instanceof Store) {
//                    Store store = (Store) item;
//                    // TODO : Gérer le long click
//                    Snackbar.make(storeRecyclerView, "TODO : Gérer le click long pour ce magasin : "
//                                    + store.getName(),
//                            Snackbar.LENGTH_LONG).show();
//                }
//            }
//
//        });

        fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddStoreDialog(requireActivity());
            }
        });

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

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_store, null);
        builder.setView(dialogView);
        builder.setTitle("Ajouter un nouveau magasin");

        Spinner storeLogoSpinner = dialogView.findViewById(R.id.storeLogoSpinner);
        EditText storeNameEditText = dialogView.findViewById(R.id.storeNameEditText);
        EditText storeLocationEditText = dialogView.findViewById(R.id.storeLocationEditText);

        // Create a Spinner for choosing a logo
        List<LogoItem> logoItems = new ArrayList<>();

        // Ajout des logos des magasins
        logoItems.add(new LogoItem("aldi"));
        logoItems.add(new LogoItem("auchan"));
        logoItems.add(new LogoItem("carrefour"));
        logoItems.add(new LogoItem("casino"));
        logoItems.add(new LogoItem("g20"));
        logoItems.add(new LogoItem("leclerc"));
        logoItems.add(new LogoItem("lidl"));
        logoItems.add(new LogoItem("mousquetaires"));
        logoItems.add(new LogoItem("systeme_u"));

        // Gestion de l'adapter
        LogoAdapter logoAdapter = new LogoAdapter(context, logoItems);
        storeLogoSpinner.setAdapter(logoAdapter);

        // Set up the buttons for Save and Cancel
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = storeNameEditText.getText().toString().trim();
                String location = storeLocationEditText.getText().toString().trim();
                // Récupérer l'objet LogoItem sélectionné
                LogoItem selectedLogoItem = (LogoItem) storeLogoSpinner.getSelectedItem();
                String selectedLogo = selectedLogoItem.getImageName();

                if (!name.isEmpty() && !location.isEmpty()) {
                    // Create a new Store object
                    Store newStore = new Store();
                    newStore.setName(name);
                    newStore.setLocation(location);
                    newStore.setLogo(selectedLogo);

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

    public void clearSelection() {
        // Logique pour effacer la sélection
        if (storeAdapter != null && storeAdapter.getSelectionTracker() != null) {
            storeAdapter.getSelectionTracker().clearSelection();
            fabAdd.setVisibility(View.VISIBLE);
        }
    }

    public void deleteSelectedItems() {
        if (storeAdapter != null && storeAdapter.getSelectionTracker() != null) {

            Selection<Long> selection = storeAdapter.getSelectionTracker().getSelection();

//            String toDelete = new String();
            for (Long selectedItem : selection) {
                Store store = storeList.get(selectedItem.intValue());
                databaseHelper.deleteStore(store.getId());
//                toDelete += store.getName() + " ";
            }

//            Snackbar.make(getView(),"Store : " + toDelete, Snackbar.LENGTH_SHORT).show();

            // Mettre à jour la liste après la suppression
            updateStoreListViewFromDatabase(false);
            clearSelection();
        }
    }

}
