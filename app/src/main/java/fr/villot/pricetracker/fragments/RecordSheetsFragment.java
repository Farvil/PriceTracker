package fr.villot.pricetracker.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Date;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.activities.MainActivity;
import fr.villot.pricetracker.activities.PriceRecordActivity;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.RecordSheetAdapter;
import fr.villot.pricetracker.adapters.SpinnerStoreAdapter;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class RecordSheetsFragment extends Fragment {
    private static RecordSheetsFragment instance;
    private DatabaseHelper databaseHelper;
    private RecyclerView recordSheetRecyclerView;
    private RecordSheetAdapter recordSheetAdapter;
    private List<RecordSheet> recordSheetList;
    private FloatingActionButton fabAdd;
    private static final String RECORDSHEET_SELECTION_KEY = "recordsheet_selection";


    public static RecordSheetsFragment getInstance() {
        if (instance == null) {
            instance = new RecordSheetsFragment();
        }
        return instance;
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
                // Réagir aux changements de sélection ici
                int numSelected = selectionTracker.getSelection().size();
                if (numSelected == 0) {
                    ((MainActivity) requireActivity()).setSelectionMode(getInstance(),false);
                    fabAdd.setVisibility(View.VISIBLE);
                }
                else if (numSelected == 1) {
                    ((MainActivity) requireActivity()).setSelectionMode(getInstance(),true);
                    String selectionCount = String.valueOf(numSelected) + " relevé";
                    ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(selectionCount);
                    fabAdd.setVisibility(View.INVISIBLE);
                }
                else {
                    String selectionCount = String.valueOf(numSelected) + " relevés";
                    ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(selectionCount);
                }
            }
        });

        //Lancement de l'activité PriceRecordActivity sur clic d'un relevé de prix
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

    private void updateRecordSheetListViewFromDatabase(boolean lastItemDisplayed) {
        // Obtenir la liste des produits à partir de la base de données
        recordSheetList = databaseHelper.getAllRecordSheets();

        // Ajouter les produits à l'adaptateur
        recordSheetAdapter.setItemList(recordSheetList);

        if (lastItemDisplayed) {
            // Positionnement de la ListView en dernier item pour voir le produit ajouté.
            int dernierIndice = recordSheetAdapter.getItemCount() - 1;
            recordSheetRecyclerView.smoothScrollToPosition(dernierIndice);
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
                // Créer un fichier CSV
                File csvFile = createCsvFile();

                // Remplir le fichier CSV avec les données
                fillCsvFile(csvFile, selection);

                // Partager le fichier CSV
                 shareCsvFile(csvFile);
            }
        }
    }

    private File createCsvFile() {
        String fileName = "record_sheet_export.csv";

        File privateRootDir = requireActivity().getFilesDir();
        File exportDir = new File(privateRootDir, "export");

        // Créer le répertoire s'il n'existe pas
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        return new File(exportDir, fileName);
    }

    private void fillCsvFile(File csvFile, Selection<Long> selection) {
        try (FileWriter writer = new FileWriter(csvFile)) {
            // En-têtes CSV
            writer.append("Nom du relevé de Prix,Date,Nom du magasin,Localisation du magasin, Code barre,Nom du produit,Marque,Quantité,Image URL,Prix\n");

            for (Long selectedItem : selection) {
                RecordSheet recordSheet = recordSheetList.get(selectedItem.intValue());

                // Récupérer les produits et magasin associés à la RecordSheet
                List<Product> products = databaseHelper.getProductsOnRecordSheet(recordSheet.getId());
                Store store = databaseHelper.getStoreById(recordSheet.getStoreId());

                // Remplir le fichier CSV avec les données de chaque produit
                for (Product product : products) {
                    writer.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            recordSheet.getName(),
                            recordSheet.getDate(),
                            store.getName(),
                            store.getLocation(),
                            product.getBarcode(),
                            product.getName(),
                            product.getBrand(),
                            product.getQuantity(),
                            product.getImageUrl(),
                            product.getPrice()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void shareCsvFile(File csvFile) {

        // Uri du fichier à partager via le FileProvider defini dans le manifest.
        Uri fileUri = FileProvider.getUriForFile(requireActivity(), getContext().getPackageName() + ".provider", csvFile);

        // Intent de partage en ajoutant les droits temporaires d'accès au fichier
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        startActivity(Intent.createChooser(shareIntent, "Partager via"));
    }
}
