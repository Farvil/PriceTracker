package fr.villot.pricetracker.fragments;

import android.content.Context;
import android.content.DialogInterface;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.activities.PriceRecordActivity;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.RecordSheetAdapter;
import fr.villot.pricetracker.adapters.SpinnerStoreAdapter;
import fr.villot.pricetracker.interfaces.OnSelectionChangedListener;
import fr.villot.pricetracker.model.Product;
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
            throw new RuntimeException(context.toString() + " doit implémenter OnSelectionChangedListener");
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

                // On masque l'icone flottant si une selection est en cours.
                if (numSelected == 0)
                    fabAdd.setVisibility(View.VISIBLE);
                else
                    fabAdd.setVisibility(View.INVISIBLE);
            }
        });

        //Lancement de l'activité PriceRecordActivity sur click d'un relevé de prix
        recordSheetAdapter.setOnItemClickListener(new RecordSheetAdapter.OnItemClickListener<RecordSheet>() {
            @Override
            public void onItemClick(RecordSheet recordSheet) {
                Intent intent = new Intent(getActivity(), PriceRecordActivity.class);
                intent.putExtra("record_sheet_name", recordSheet.getName());
                intent.putExtra("record_sheet_id", recordSheet.getId());
                startActivity(intent);
            }
        });

        // Action du bouton flottant
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateRecordSheetDialog();
            }
        });

    }


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


    public void showCreateRecordSheetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_recordsheet, null);
        builder.setView(dialogView);
        builder.setTitle("Créer un nouveau relevé de prix");
        builder.setCancelable(false);

        Spinner storeSpinner = dialogView.findViewById(R.id.storeSpinner);
        EditText recordsheetNameEditText = dialogView.findViewById(R.id.recordsheetNameEditText);

        // Obtenez la liste des magasins à partir de votre base de données (par exemple, dans une liste storesList)

        // Créez un ArrayAdapter pour afficher la liste des magasins dans le Spinner
        List<Store> storeList = databaseHelper.getAllStores();
        SpinnerStoreAdapter spinnerStoreAdapter = new SpinnerStoreAdapter(getContext(), storeList);
        storeSpinner.setAdapter(spinnerStoreAdapter);

        builder.setPositiveButton("Créer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Récupérez le nom de la recordsheet saisi par l'utilisateur
                String recordSheetName = recordsheetNameEditText.getText().toString();

                if (!recordSheetName.isEmpty()) {

                    // Recuperation du magasin selectionné
                    Store selectedStore = (Store) storeSpinner.getSelectedItem();
                    if (selectedStore != null) {
                        // Création d'un RecordSheet
                        RecordSheet newRecordSheet = new RecordSheet(recordSheetName, new Date(),selectedStore);

                        // Ajoute la fiche d'enregistrements à la base de données
                        databaseHelper.addRecordSheet(newRecordSheet);

                        // Update the store list and refresh the spinner
                        updateRecordSheetListViewFromDatabase(true);
                    }

                }

            }
        });

        builder.setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void clearSelection() {
        // Logique pour effacer la sélection
        if (recordSheetAdapter != null && recordSheetAdapter.getSelectionTracker() != null) {
            recordSheetAdapter.getSelectionTracker().clearSelection();
            fabAdd.setVisibility(View.VISIBLE);
        }
    }

    public void deleteSelectedItems() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Voulez-vous vraiment supprimer les feuilles de relevés de prix sélectionnées ?");
        builder.setCancelable(false);

        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (recordSheetAdapter != null && recordSheetAdapter.getSelectionTracker() != null) {

                    Selection<Long> selection = recordSheetAdapter.getSelectionTracker().getSelection();

                    for (Long selectedItem : selection) {
                        RecordSheet recordSheet = recordSheetList.get(selectedItem.intValue());
                        try {
                            databaseHelper.deleteRecordSheet(recordSheet.getId());
                        } catch (Exception e) {
                            Snackbar.make(getView(),"Erreur lors de la suppression de " + recordSheet.getName() + " !", Snackbar.LENGTH_SHORT).show();
                            break; // Ne tente pas d'autres suppressions en cas d'erreur
                        }
                    }

                    // Mettre à jour la liste après la suppression
                    updateRecordSheetListViewFromDatabase(false);
                    clearSelection();
                }

            }
        });

        builder.setNegativeButton("Non", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void shareRecordSheet() {
        if (recordSheetAdapter != null && recordSheetAdapter.getSelectionTracker() != null) {
            Selection<Long> selection = recordSheetAdapter.getSelectionTracker().getSelection();

            // Vérifier s'il y a des éléments sélectionnés
            if (!selection.isEmpty()) {

                // Création de la liste des recordsheets à partager
                List<RecordSheet> recordSheetsToShare = new ArrayList<>();
                for (Long selectedItem : selection) {
                    recordSheetsToShare.add(recordSheetList.get(selectedItem.intValue()));
                }

                CsvHelper csvHelper = new CsvHelper(requireActivity(), "record_sheet_export.csv");
                csvHelper.fillCsvFileWithRecordSheets(recordSheetsToShare);
                csvHelper.shareCsvFile();
            }
        }
    }

}
