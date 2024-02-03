package fr.villot.pricetracker.fragments;

import static fr.villot.pricetracker.fragments.StoresFragment.StoresFragmentDialogType.DIALOG_TYPE_ADD;
import static fr.villot.pricetracker.fragments.StoresFragment.StoresFragmentDialogType.DIALOG_TYPE_UPDATE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.activities.RecordSheetActivity;
import fr.villot.pricetracker.adapters.LogoAdapter;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.interfaces.OnSelectionChangedListener;
import fr.villot.pricetracker.interfaces.OnStoreChangedListener;
import fr.villot.pricetracker.model.LogoItem;
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

    private static final String STORE_SELECTION_KEY = "store_selection";

    public enum StoresFragmentDialogType {
        DIALOG_TYPE_ADD,
        DIALOG_TYPE_UPDATE
    }

    private OnStoreChangedListener mOnStoreChangedListener;

    private OnSelectionChangedListener mOnSelectionChangedListener;



//    private static final Logger logger = Logger.getLogger(StoresFragment.class.getName());

    public static StoresFragment getInstance() {
        if (instance == null) {
            instance = new StoresFragment();
        }
        return instance;
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

        // Ajout du listener OnSelectionChangedListener
        if (context instanceof OnSelectionChangedListener) {
            mOnSelectionChangedListener = (OnSelectionChangedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " doit implémenter OnSelectionChangedListener");
        }
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
                STORE_SELECTION_KEY,
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

                int numSelected = selectionTracker.getSelection().size();

                // On informe l'activité parente (MainActivity) du changement de sélection
                if (mOnSelectionChangedListener != null) {
                    mOnSelectionChangedListener.onSelectionChanged(getInstance(), numSelected);
                }

                // On masque l'icone flottant si une selection est en cours.
                if (numSelected == 0)
                    fabAdd.setVisibility(View.VISIBLE);
                else
                    fabAdd.setVisibility(View.INVISIBLE);
            }

        });

        // Lancement de l'activité RecordSheetActivity sur click d'un magasin
        storeAdapter.setOnItemClickListener(new StoreAdapter.OnItemClickListener<Store>() {
            @Override
            public void onItemClick(Store store) {
                Intent intent = new Intent(getActivity(), RecordSheetActivity.class);
                intent.putExtra("store_id", store.getId());
                startActivity(intent);
            }
        });


        fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserQueryDialogBox(new Store(),DIALOG_TYPE_ADD);
//                showAddStoreDialog(requireActivity());
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
        builder.setCancelable(false);

        Spinner storeLogoSpinner = dialogView.findViewById(R.id.storeLogoSpinner);
        EditText storeNameEditText = dialogView.findViewById(R.id.storeNameEditText);
        EditText storeLocationEditText = dialogView.findViewById(R.id.storeLocationEditText);

        // Liste des logos des enseignes
        List<LogoItem> logoItems = new ArrayList<>();
        List<String> brandsList = databaseHelper.getBrands();
        for (String brand : brandsList) {
            logoItems.add(new LogoItem(brand));
        }

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


    protected void showUserQueryDialogBox(Store store, StoresFragmentDialogType storesFragmentDialogType) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_store, null);
        builder.setView(dialogView);

        // Références des vues dans le layout de la boîte de dialogue
        Spinner storeLogoSpinner = dialogView.findViewById(R.id.storeLogoSpinner);
        EditText storeNameEditText = dialogView.findViewById(R.id.storeNameEditText);
        EditText storeLocationEditText = dialogView.findViewById(R.id.storeLocationEditText);

        // Liste des logos des enseignes
        List<LogoItem> logoItems = new ArrayList<>();
        List<String> brandsList = databaseHelper.getBrands();
        for (String brand : brandsList) {
            logoItems.add(new LogoItem(brand));
        }

        // Gestion de l'adapter
        LogoAdapter logoAdapter = new LogoAdapter(getActivity(), logoItems);
        storeLogoSpinner.setAdapter(logoAdapter);

        String title = null;
        switch (storesFragmentDialogType) {
            case DIALOG_TYPE_ADD:
                title = "Ajouter un nouveau magasin";
                break;
            case DIALOG_TYPE_UPDATE:
                title = "Modifier le magasin";

                if (store != null) {
                    storeNameEditText.setText(store.getName());
                    storeLocationEditText.setText(store.getLocation());
                    storeLogoSpinner.setSelection(brandsList.indexOf(store.getLogo()));
                }
                break;
        }

        builder.setTitle(title);
        builder.setCancelable(false);

        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Récupération des informations saisies
                String name = storeNameEditText.getText().toString().trim();
                String location = storeLocationEditText.getText().toString().trim();
                LogoItem selectedLogoItem = (LogoItem) storeLogoSpinner.getSelectedItem();
                String selectedLogo = selectedLogoItem.getImageName();

                if (!name.isEmpty() && !location.isEmpty()) {

                    store.setName(name);
                    store.setLocation(location);
                    store.setLogo(selectedLogo);

                    switch (storesFragmentDialogType) {
                        case DIALOG_TYPE_ADD:
                            // Ajout dans la base de données
                            databaseHelper.addStore(store);
                            break;
                        case DIALOG_TYPE_UPDATE:
                            databaseHelper.updateStore(store);
                            notifyStoreChanged(store.getId());
                            break;
                    }

                    updateStoreListViewFromDatabase(true);

                } else {
                    Snackbar.make(getView(),"Veuillez entrer un nom de magasin et un lieu", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Non", null);

        AlertDialog dialog = builder.create();
        dialog.show();

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
            Queue<Store> storesToDelete = new LinkedList<>();

            // Ajout des magasins à supprimer à la file d'attente
            for (Long selectedItem : selection) {
                Store store = storeList.get(selectedItem.intValue());
                storesToDelete.offer(store);
            }

            // Suppression des magasins de la file d'attente
            deleteStoresInQueue(storesToDelete);

            // Annule le mode de selection
            clearSelection();
        }
    }

    private void deleteStoresInQueue(Queue<Store> storesToDelete) {
        if (!storesToDelete.isEmpty()) {
            Store store = storesToDelete.poll();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Voulez-vous vraiment supprimer le magasin " + store.getName() + " ?");
            builder.setCancelable(false);

            builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Suppression uniquement si pas de dépendances avec des recordsheets.
                    if (!databaseHelper.hasRecordSheetsOnStore(store.getId())) {
                        databaseHelper.deleteStore(store.getId());
                        updateStoreListViewFromDatabase(false);
                    } else {
                        Snackbar.make(getView(), "Suppression impossible car au moins une feuille de relevé de prix associée au magasin", Snackbar.LENGTH_SHORT).show();
                    }

                    // Appeler la suppression des magasins restants dans la file d'attente
                    deleteStoresInQueue(storesToDelete);
                }
            });

            builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Si l'utilisateur clique sur "Non", passage au magasin suivant dans la file d'attente
                    deleteStoresInQueue(storesToDelete);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    // Modification du magasin
    public void editStore() {

        if (storeAdapter != null && storeAdapter.getSelectionTracker() != null) {
            Selection<Long> selection = storeAdapter.getSelectionTracker().getSelection();

            // Récupération de l'élément sélectionné
            Store store = null;
            for (Long selectedItem : selection) {
                store = storeList.get(selectedItem.intValue());
                break;
            }

            showUserQueryDialogBox(store,DIALOG_TYPE_UPDATE);
        }
    }

    // Méthode appelée lors du changement sur un magasin
    private void notifyStoreChanged(int storeId) {
        if (mOnStoreChangedListener != null) {
            mOnStoreChangedListener.onStoreChanged(storeId);
        }
    }

}
