package fr.villot.pricetracker.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.activities.PriceRecordActivity;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.RecordSheetAdapter;
import fr.villot.pricetracker.adapters.SpinnerStoreAdapter;
import fr.villot.pricetracker.interfaces.OnSelectionChangedListener;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;
import fr.villot.pricetracker.utils.CsvHelper;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class RecordSheetsFragment extends Fragment {
    private static RecordSheetsFragment instance;
    protected DatabaseHelper databaseHelper;
    private RecyclerView recordSheetRecyclerView;
    private RecordSheetAdapter recordSheetAdapter;
    private List<RecordSheet> recordSheetList;
    private FloatingActionButton fabAdd;
    private static final String RECORDSHEET_SELECTION_KEY = "recordsheet_selection";

    private OnSelectionChangedListener mOnSelectionChangedListener;

    public void selectAllItems() {
        if (recordSheetAdapter != null && recordSheetAdapter.getSelectionTracker() != null) {
            List<Long> selectedItems = new ArrayList<>();
            for (int i = 0 ; i < recordSheetAdapter.getItemCount() ; i++) {
                selectedItems.add((long) i);
            }
            recordSheetAdapter.getSelectionTracker().setItemsSelected(selectedItems, true);
        }
    }

    public enum RecordSheetsFragmentDialogType {
        DIALOG_TYPE_ADD,
        DIALOG_TYPE_UPDATE
    }


    /**
     * Retourne l'instance du singleton
     * @return L'instance du fragment
     */
    public static RecordSheetsFragment getInstance() {
        if (instance == null) {
            instance = new RecordSheetsFragment();
        }
        return instance;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Ajout du listener OnSelectionChangedListener
        if (context instanceof OnSelectionChangedListener) {
            mOnSelectionChangedListener = (OnSelectionChangedListener) context;
        } else {
            throw new RuntimeException(context + " doit implémenter OnSelectionChangedListener");
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record_sheet, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recuperation des vues
        recordSheetRecyclerView = view.findViewById(R.id.recordSheetRecyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        recordSheetRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Récupération des feuilles de relevés de prix dans la base de données.
        recordSheetList = getRecordSheets();

        // Adapter entre RecyclerView et Produit.
        recordSheetAdapter = new RecordSheetAdapter(getActivity(), recordSheetList);
        recordSheetRecyclerView.setAdapter(recordSheetAdapter);

        // Gestion de la selection d'items
        SelectionTracker<Long> selectionTracker = new SelectionTracker.Builder<>(
                RECORDSHEET_SELECTION_KEY,
                recordSheetRecyclerView,
                new StableIdKeyProvider(recordSheetRecyclerView),
                new MyDetailsLookup(recordSheetRecyclerView),
                StorageStrategy.createLongStorage()
        ).build();
        recordSheetAdapter.setSelectionTracker(selectionTracker);

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                int numSelected = selectionTracker.getSelection().size();

                // On informe l'activité parente (MainActivity ou RecordSheetActivity) du changement de sélection
                if (mOnSelectionChangedListener != null) {
                    mOnSelectionChangedListener.onSelectionChanged(getInstance(), numSelected);
                }

                // On masque l'icône flottant si une selection est en cours.
                if (numSelected == 0)
                    fabAdd.setVisibility(View.VISIBLE);
                else
                    fabAdd.setVisibility(View.INVISIBLE);
            }
        });

        //Lancement de l'activité PriceRecordActivity sur click d'un relevé de prix
        recordSheetAdapter.setOnItemClickListener(recordSheet -> {
            Intent intent = new Intent(getActivity(), PriceRecordActivity.class);
            intent.putExtra("record_sheet_name", recordSheet.getName());
            intent.putExtra("record_sheet_id", recordSheet.getId());
            startActivity(intent);
        });

        // Action du bouton flottant
        fabAdd.setOnClickListener(v -> {
            if ((long) databaseHelper.getAllStores().size() != 0) {
                showUserQueryDialogBox(new RecordSheet(), RecordSheetsFragmentDialogType.DIALOG_TYPE_ADD);
            }
            else {
                Snackbar.make(requireView(),"Il faut au minimum un magasin pour y associer un relevé de prix !", Snackbar.LENGTH_SHORT).show();
            }
        });

    }


    /**
     * Récupère les relevés de prix
     *
     * <p>Cette méthode est protected et sera redéfinie par la classe fille RecordSheetsOnStoreFragment </p>
     *
     * @return les relevés de prix
     */
    protected List<RecordSheet> getRecordSheets() {
        return databaseHelper.getAllRecordSheets();
    }

    public void updateRecordSheetListViewFromDatabase(boolean lastItemDisplayed) {
        // Obtenir la liste des produits à partir de la base de données
        recordSheetList = databaseHelper.getAllRecordSheets();

        // Ajouter les produits à l'adaptateur
        recordSheetAdapter.setItemList(recordSheetList);

        if (lastItemDisplayed) {
            // Positionnement de la ListView en dernier item pour voir la recordsheet ajoutée.
            recordSheetRecyclerView.smoothScrollToPosition(recordSheetAdapter.getLastItemPosition());
        }

    }

    /**
     * Popup pour créer ou modifier un relevé de prix
     *
     * @param recordSheet le relevé de prix à modifier
     * @param recordSheetsFragmentDialogType Le type de popup (ajout ou modification)
     */
    protected void showUserQueryDialogBox(RecordSheet recordSheet, RecordSheetsFragmentDialogType recordSheetsFragmentDialogType) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_recordsheet, null);
        builder.setView(dialogView);

        // Références des vues dans le layout de la boîte de dialogue
        Spinner storeSpinner = dialogView.findViewById(R.id.storeSpinner);
        EditText recordsheetNameEditText = dialogView.findViewById(R.id.recordsheetNameEditText);

        //  Affichage de la liste des magasins dans le Spinner via l'adapter
        List<Store> storeList = databaseHelper.getAllStores();
        SpinnerStoreAdapter spinnerStoreAdapter = new SpinnerStoreAdapter(getContext(), storeList);
        storeSpinner.setAdapter(spinnerStoreAdapter);

        String title = null;
        switch (recordSheetsFragmentDialogType) {
            case DIALOG_TYPE_ADD:
                title = "Créer un nouveau relevé de prix";
                break;
            case DIALOG_TYPE_UPDATE:
                title = "Modifier le relevé de prix";

                if (recordSheet != null) {
                    storeSpinner.setSelection(storeList.indexOf(recordSheet.getStore()));
                    recordsheetNameEditText.setText(recordSheet.getName());
                }
                break;
        }

        builder.setTitle(title);
        builder.setCancelable(false);

        builder.setPositiveButton("Oui", (dialogInterface, i) -> {

            // Récupération des informations saisies
            String recordSheetName = recordsheetNameEditText.getText().toString().trim();
            Store selectedStore = (Store) storeSpinner.getSelectedItem();

            if (recordSheet != null && !recordSheetName.isEmpty() && selectedStore != null) {

                recordSheet.setName(recordSheetName);
                recordSheet.setStore(selectedStore);

                switch (recordSheetsFragmentDialogType) {
                    case DIALOG_TYPE_ADD:
                        databaseHelper.addRecordSheet(recordSheet);
                        break;
                    case DIALOG_TYPE_UPDATE:
                        databaseHelper.updateRecordSheet(recordSheet);
                        break;
                }

                // Rafraîchissement de la liste de recordsheets
                updateRecordSheetListViewFromDatabase(true);
            }

        });

        builder.setNegativeButton("Non", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    /**
     * Annule le mode de sélection
     *
     * <p>Cette méthode est publique est peut être appelée depuis l'activité parente </p>
     */
    public void clearSelection() {
        // Logique pour effacer la sélection
        if (recordSheetAdapter != null && recordSheetAdapter.getSelectionTracker() != null) {
            recordSheetAdapter.getSelectionTracker().clearSelection();
            fabAdd.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Supprime les relevés de prix sélectionnés
     */
    public void deleteSelectedItems() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage("Voulez-vous vraiment supprimer les feuilles de relevés de prix sélectionnées ?");
        builder.setCancelable(false);

        builder.setPositiveButton("Oui", (dialog, which) -> {

            if (recordSheetAdapter != null && recordSheetAdapter.getSelectionTracker() != null) {

                Selection<Long> selection = recordSheetAdapter.getSelectionTracker().getSelection();

                for (Long selectedItem : selection) {
                    RecordSheet recordSheet = recordSheetList.get(selectedItem.intValue());
                    try {
                        databaseHelper.deleteRecordSheet(recordSheet.getId());
                    } catch (Exception e) {
                        Snackbar.make(requireView(),"Erreur lors de la suppression de " + recordSheet.getName() + " !", Snackbar.LENGTH_SHORT).show();
                        break; // Ne tente pas d'autres suppressions en cas d'erreur
                    }
                }

                // Mettre à jour la liste après la suppression
                updateRecordSheetListViewFromDatabase(false);
                clearSelection();
            }

        });

        builder.setNegativeButton("Non", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    /**
     * Récupère la liste des relevés de prix sélectionnés
     *
     * @return La liste des relevés sélectionnés
     */
    private List<RecordSheet> getSelectedRecordSheets() {

        List<RecordSheet> selectedRecordSheets = null;

        if (recordSheetAdapter != null && recordSheetAdapter.getSelectionTracker() != null) {
            Selection<Long> selection = recordSheetAdapter.getSelectionTracker().getSelection();

            // Vérifier s'il y a des éléments sélectionnés
            if (!selection.isEmpty()) {

                // Création de la liste des recordsheets à partager
                selectedRecordSheets = new ArrayList<>();
                for (Long selectedItem : selection) {
                    selectedRecordSheets.add( recordSheetList.get(selectedItem.intValue()));
                }
            }
        }

        return selectedRecordSheets;
    }

    /**
     * Partage d'un relevé de prix
     */
    public void shareRecordSheet() {

        List<RecordSheet> selectedRecordSheets = getSelectedRecordSheets();
        if (selectedRecordSheets != null) {
            // Initialisation du csvHelper
            CsvHelper csvHelper = new CsvHelper(requireActivity(), "export_releve_de_prix.csv");
            csvHelper.fillCsvFileWithRecordSheets(selectedRecordSheets);
            csvHelper.shareCsvFile();
        }

    }

    /**
     * Export d'un relevé de prix
     */
    public boolean exportRecordSheet(Uri uri) {

        List<RecordSheet> selectedRecordSheets = getSelectedRecordSheets();
        if (selectedRecordSheets != null) {
            // Initialisation du csvHelper
            CsvHelper csvHelper = new CsvHelper(requireActivity(), "export_releve_de_prix.csv");
            csvHelper.fillCsvFileWithRecordSheets(selectedRecordSheets);
            return csvHelper.writeCsvFileToUri(uri);
        }

        return false;
    }


    /**
     * Modification d'un relevé de prix
     */
    public void editRecordSheet() {

        // L'activité appelle cette méthode uniquement si une seule recordsheet sélectionnée.
        List<RecordSheet> selectedRecordSheets = getSelectedRecordSheets();
        if (selectedRecordSheets != null) {
            showUserQueryDialogBox(selectedRecordSheets.get(0),RecordSheetsFragmentDialogType.DIALOG_TYPE_UPDATE);
            clearSelection();
        }

    }
}
