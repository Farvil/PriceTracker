package fr.villot.pricetracker.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.SimpleRecordSheetAdapter;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;
import fr.villot.pricetracker.utils.CsvHelper;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class RecordSheetOnStoreActivity extends AppCompatActivity {

    private SimpleRecordSheetAdapter simpleRecordSheetAdapter;
    private List<RecordSheet> recordSheetList;
    private static final String RECORDSHEET_ON_STORE_ACTIVITY_SELECTION_KEY = "recordsheet_on_store_activity_selection";

    private boolean isSelectionModeActive = false;

    RecyclerView recordSheetRecyclerView;

    List<RecordSheet> recordSheetsToExport;

    private final ActivityResultLauncher<Intent> createDocumentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            if (exportRecordSheets(uri)) {
                                Snackbar.make(recordSheetRecyclerView, "Le fichier CSV est enregistré.", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(recordSheetRecyclerView, "Erreur d'enregistrement du fichier CSV !", Snackbar.LENGTH_SHORT).show();
                            }


                        }
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sheet_on_store);

        // Récuperation des vues
        Toolbar toolbar = findViewById(R.id.toolbar);
        CardView storeCardView = findViewById(R.id.storeCardView);
        storeCardView.setRadius(0);
        TextView storeNameTextView = findViewById(R.id.storeNameTextView);
        TextView storeLocationTextView = findViewById(R.id.storeLocationTextView);
        ImageView storeImageView = findViewById(R.id.storeImageView);
        recordSheetRecyclerView = findViewById(R.id.recordSheetRecyclerView);

        // Initialisation du DatabaseHelper
        DatabaseHelper databaseHelper = MyApplication.getDatabaseHelper();

        // Bouton de retour à l'activité principale
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        // Récupération du magasin en fonction de l'identifiant passé en paramètre de l'activité
        int storeId = getIntent().getIntExtra("store_id", -1);
        Store store = databaseHelper.getStoreById(storeId);
        if (store != null) {
            storeNameTextView.setText(store.getName());
            storeLocationTextView.setText(store.getLocation());
            int imageResource = storeImageView.getContext().getResources().getIdentifier(store.getLogo(), "drawable", storeImageView.getContext().getPackageName());
            storeImageView.setImageResource(imageResource);
        }

        // Recyclerview
        recordSheetRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Récupération des feuilles de relevés de prix dans la base de données.
        recordSheetList = databaseHelper.getRecordSheetsOnStore(storeId);

        // Adapter entre RecyclerView et Produit.
        simpleRecordSheetAdapter = new SimpleRecordSheetAdapter(this, recordSheetList);
        recordSheetRecyclerView.setAdapter(simpleRecordSheetAdapter);

        // Gestion de la selection d'items
        SelectionTracker<Long> selectionTracker = new SelectionTracker.Builder<>(
                RECORDSHEET_ON_STORE_ACTIVITY_SELECTION_KEY,
                recordSheetRecyclerView,
                new StableIdKeyProvider(recordSheetRecyclerView),
                new MyDetailsLookup(recordSheetRecyclerView),
                StorageStrategy.createLongStorage()
        ).build();
        simpleRecordSheetAdapter.setSelectionTracker(selectionTracker);

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                int numSelectedItems = selectionTracker.getSelection().size();

                if (numSelectedItems == 0) {
                    setSelectionMode(false);
                }
                else {
                    setSelectionMode(true);

                    // Modification du titre de la toolbar pour indiquer le nombre d'éléments sélectionnés.
                    String selectionCount = numSelectedItems + " relevé";

                    // Mise au pluriel si plusieurs elements sélectionnés
                    if (numSelectedItems > 1)
                        selectionCount += "s";

                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setTitle(selectionCount);
                    }
                }
            }
        });


        //Lancement de l'activité PriceRecordActivity sur click d'un relevé de prix
        simpleRecordSheetAdapter.setOnItemClickListener(recordSheet -> {
            Intent intent = new Intent(RecordSheetOnStoreActivity.this,  PriceRecordActivity.class);
            intent.putExtra("record_sheet_name", recordSheet.getName());
            intent.putExtra("record_sheet_id", recordSheet.getId());
            intent.putExtra("read_only",true);
            startActivity(intent);
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if (!isSelectionModeActive) {
            inflater.inflate(R.menu.main_menu, menu);
            menu.findItem(R.id.menu_about).setVisible(false);
        } else {
            inflater.inflate(R.menu.toolbar_simple_selection_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (isSelectionModeActive) {
                clearSelection();
            } else {
                getOnBackPressedDispatcher().onBackPressed(); // Retour à l'activité principale
            }
            return true;
        } else if (itemId == R.id.action_share) {
            shareRecordSheets();
            return true;
        } else if (itemId == R.id.action_export) {
            exportRecordSheets();
            return true;
        } else if (itemId == R.id.menu_select_all) {
            selectAllItems();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSelectionMode(boolean isSelectionModeActive) {

        ActionBar actionBar = getSupportActionBar();
        // Rafraîchissement de la toolbar en cas de changement de mode
        if (this.isSelectionModeActive != isSelectionModeActive) {
            this.isSelectionModeActive = isSelectionModeActive;
            if (!isSelectionModeActive) {
                if (actionBar != null) {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
                    actionBar.setTitle(R.string.activity_record_sheet_on_store_title);
                }

            } else {
                if (actionBar != null) {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel);
                }
            }
            invalidateOptionsMenu(); // Rafraîchissement du menu de la toolbar
        }
    }

    public void clearSelection() {

        if (simpleRecordSheetAdapter != null && simpleRecordSheetAdapter.getSelectionTracker() != null) {
            simpleRecordSheetAdapter.getSelectionTracker().clearSelection();
        }

    }


    public void shareRecordSheets() {
        if (simpleRecordSheetAdapter != null && simpleRecordSheetAdapter.getSelectionTracker() != null) {
            Selection<Long> selection = simpleRecordSheetAdapter.getSelectionTracker().getSelection();

            // Vérifier s'il y a des éléments sélectionnés
            if (!selection.isEmpty()) {
                // Création de la liste des recordsheets à partager
                List<RecordSheet> recordSheetsToShare = new ArrayList<>();
                for (Long selectedItem : selection) {
                    recordSheetsToShare.add(0, recordSheetList.get(selectedItem.intValue())); // Ajout en première position pour inverser l'ordre
                }

                CsvHelper csvHelper = new CsvHelper(this, "record_sheet_export.csv");
                csvHelper.fillCsvFileWithRecordSheets(recordSheetsToShare);
                csvHelper.shareCsvFile();
            }
        }
    }

    public void exportRecordSheets() {
        if (simpleRecordSheetAdapter != null && simpleRecordSheetAdapter.getSelectionTracker() != null) {
            Selection<Long> selection = simpleRecordSheetAdapter.getSelectionTracker().getSelection();

            // Vérifier s'il y a des éléments sélectionnés
            if (!selection.isEmpty()) {
                // Création de la liste des recordsheets à partager
                recordSheetsToExport = new ArrayList<>();
                for (Long selectedItem : selection) {
                    recordSheetsToExport.add(0, recordSheetList.get(selectedItem.intValue())); // Ajout en première position pour inverser l'ordre
                }

                // Demande le nom du fichier à l'utilisateur
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "export_releves_de_prix.csv");

                createDocumentLauncher.launch(intent);
            }
        }
    }

    public boolean exportRecordSheets(Uri uri) {
        if (recordSheetsToExport != null) {
            // Initialisation du csvHelper
            CsvHelper csvHelper = new CsvHelper(this, "export_releves_de_prix.csv");
            csvHelper.fillCsvFileWithRecordSheets(recordSheetsToExport);
            return csvHelper.writeCsvFileToUri(uri);
        }

        return false;
    }

    public void selectAllItems() {
        if (simpleRecordSheetAdapter != null && simpleRecordSheetAdapter.getSelectionTracker() != null) {
            List<Long> selectedItems = new ArrayList<>();
            for (int i = 0 ; i < simpleRecordSheetAdapter.getItemCount() ; i++) {
                selectedItems.add((long) i);
            }
            simpleRecordSheetAdapter.getSelectionTracker().setItemsSelected(selectedItems, true);
        }
    }

}