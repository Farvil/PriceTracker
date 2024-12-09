package fr.villot.pricetracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.RecordSheetAdapter;
import fr.villot.pricetracker.interfaces.OnProductDeletedFromRecordSheetListener;
import fr.villot.pricetracker.model.PriceRecord;
import fr.villot.pricetracker.model.PriceStats;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.utils.CsvHelper;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class RecordSheetOnProductActivity extends AppCompatActivity implements OnProductDeletedFromRecordSheetListener {

    private String barcode;
    private DatabaseHelper databaseHelper;
    private RecyclerView recordSheetRecyclerView;
    private RecordSheetAdapter recordSheetAdapter;
    private List<RecordSheet> recordSheetList;
    private static final String RECORDSHEET_ON_PRODUCT_ACTIVITY_SELECTION_KEY = "recordsheet_on_product_activity_selection";

    private boolean isSelectionModeActive = false;

    TextView productMinPrice;
    TextView productMaxPrice;
    TextView productMoyPrice;

    private final ActivityResultLauncher<Intent> priceRecordLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        boolean updateRequired = data.getBooleanExtra("update_required", false);
                        if (updateRequired) {
                            updateRecordSheetListViewFromDatabase(false);
                            updatePriceStats();
                        }
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sheet_on_product);

        // Récuperation des vues
        Toolbar toolbar = findViewById(R.id.toolbar);
        CardView productCardView = findViewById(R.id.productCardView);
        productCardView.setRadius(0);
        TextView productBarcodeTextView = findViewById(R.id.productBarcodeTextView);
        TextView productNameTextView = findViewById(R.id.productNameTextView);
        LinearLayout productBrandZone = findViewById(R.id.productBrandZone);
        TextView productBrandTextView = findViewById(R.id.productBrandTextView);
        LinearLayout productQuantityZone = findViewById(R.id.productQuantityZone);
        TextView productQuantityTextView = findViewById(R.id.productQuantityTextView);
        LinearLayout productOriginZone = findViewById(R.id.productOriginZone);
        TextView productOriginTextView = findViewById(R.id.productOriginTextView);
        ImageView productImageView = findViewById(R.id.productImageView);
        LinearLayout productPriceZone = findViewById(R.id.productPriceZone);
        recordSheetRecyclerView = findViewById(R.id.recordSheetRecyclerView);
        productMinPrice = findViewById(R.id.productMinPrice);
        productMaxPrice = findViewById(R.id.productMaxPrice);
        productMoyPrice = findViewById(R.id.productMoyPrice);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        // Bouton de retour à l'activité principale
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        // Récupération du produit en fonction de l'identifiant passé en paramètre de l'activité
        barcode = getIntent().getStringExtra("barcode");
        Product product = databaseHelper.getProductFromBarCode(barcode);

        if (product != null) {

            // Affichage du barcode
            productBarcodeTextView.setText(product.getBarcode());

            // Affichage du nom du produit s'il existe
            String productName = product.getName();
            if (productName != null && !(productName.isEmpty())) {
                productNameTextView.setText(product.getName());
            }

            // Affichage de la marque du produit si elle existe
            String productBrand = product.getBrand();
            if (productBrand != null && !(productBrand.isEmpty())) {
                productBrandTextView.setText(product.getBrand());
            }
            else {
                productBrandZone.setVisibility(View.GONE);
            }

            // Affichage de la quantité du produit si elle existe
            String productQuantity = product.getQuantity();
            if (productQuantity != null && !(productQuantity.isEmpty())) {
                productQuantityTextView.setText(product.getQuantity());
            }
            else {
                productQuantityZone.setVisibility(View.GONE);
            }

            // Affichage de l'origine du produit si elle existe
            String productOrigin = product.getOrigin();
            if (productOrigin != null && !(productOrigin.isEmpty())) {
                productOriginTextView.setText(product.getOrigin());
            } else {
                productOriginZone.setVisibility(View.GONE);
            }

            // On masque le prix
            productPriceZone.setVisibility(View.GONE);

            // Min, Max, Moy
            updatePriceStats();

            // Afficher l'image
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !(imageUrl.isEmpty())) {
                Picasso.get().load(imageUrl).into(productImageView);
            }

        }

        // Recyclerview
        recordSheetRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Récupération des feuilles de relevés de prix dans la base de données.
        recordSheetList = databaseHelper.getRecordSheetsOnProduct(barcode);
        setPriceForRecordSheets(recordSheetList);

        // Adapter entre RecyclerView et Produit.
        recordSheetAdapter = new RecordSheetAdapter(this, recordSheetList);
        recordSheetAdapter.setProductPrice(true); // Pour l'affichage du prix
        recordSheetRecyclerView.setAdapter(recordSheetAdapter);

        // Gestion de la selection d'items
        SelectionTracker<Long> selectionTracker = new SelectionTracker.Builder<>(
                RECORDSHEET_ON_PRODUCT_ACTIVITY_SELECTION_KEY,
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
        recordSheetAdapter.setOnItemClickListener(recordSheet -> {
            Intent intent = new Intent(RecordSheetOnProductActivity.this,  PriceRecordActivity.class);
            intent.putExtra("record_sheet_name", recordSheet.getName());
            intent.putExtra("record_sheet_id", recordSheet.getId());
            priceRecordLauncher.launch(intent);
        });

        productCardView.setOnClickListener(view -> showUserQueryDialogBox(product)
        );

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
        if (isSelectionModeActive) {
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
                onBackPressed(); // Retour à l'activité principale
            }
            return true;
        } else if (itemId == R.id.action_share) {
            shareRecordSheet();
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
                    actionBar.setTitle(R.string.activity_record_sheet_on_product_title);
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

        if (recordSheetAdapter != null && recordSheetAdapter.getSelectionTracker() != null) {
            recordSheetAdapter.getSelectionTracker().clearSelection();
        }

    }


    public void shareRecordSheet() {
        if (recordSheetAdapter != null && recordSheetAdapter.getSelectionTracker() != null) {
            Selection<Long> selection = recordSheetAdapter.getSelectionTracker().getSelection();

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


    View getProductViewForDialog(Product product) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.item_product, null);

        // Références des vues dans le layout de la boîte de dialogue
        TextView productBarcodeTextView = dialogView.findViewById(R.id.productBarcodeTextView);
        ImageView productImageView = dialogView.findViewById(R.id.productImageView);
        TextView productNameTextView = dialogView.findViewById(R.id.productNameTextView);
        TextView productBrandTextView = dialogView.findViewById(R.id.productBrandTextView);
        TextView productQuantityTextView = dialogView.findViewById(R.id.productQuantityTextView);
        LinearLayout productPriceZone = dialogView.findViewById(R.id.productPriceZone);

        // On affiche les détails du produit dans les vues
        productBarcodeTextView.setText(product.getBarcode());
        productNameTextView.setText(product.getName());
        productBrandTextView.setText(product.getBrand());
        productQuantityTextView.setText(product.getQuantity());

        // On masque le prix
        productPriceZone.setVisibility(View.GONE);

        // On affiche l'image
        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !(imageUrl.isEmpty())) {
            Picasso.get().load(imageUrl).into(productImageView);
        }

        return dialogView;
    }

    protected void showUserQueryDialogBox(Product product) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getProductViewForDialog(product));
        builder.setTitle( "Lancer un navigateur ?");
        builder.setMessage("Souhaitez vous visualiser les détails de ce produit sur le site OpenFoodFacts ?");
        builder.setCancelable(false);


        builder.setPositiveButton("Oui", (dialog, which) -> {

            String barcode = product.getBarcode();
            String url = "https://world.openfoodfacts.org/product/" + barcode;

            // Lancer le navigateur
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        builder.setNegativeButton("Non", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onProductDeletedFromRecordSheet(String barcode, int recordSheetId) {
        updateRecordSheetListViewFromDatabase(false);
    }

    public void updateRecordSheetListViewFromDatabase(boolean lastItemDisplayed) {
        // Liste des recordsheets associées au produit
        recordSheetList = databaseHelper.getRecordSheetsOnProduct(barcode);
        setPriceForRecordSheets(recordSheetList);

        // Ajouter les produits à l'adaptateur
        recordSheetAdapter.setItemList(recordSheetList);

        if (lastItemDisplayed) {
            // Positionnement de la ListView en dernier item pour voir la recordsheet ajoutée.
            recordSheetRecyclerView.smoothScrollToPosition(recordSheetAdapter.getLastItemPosition());
        }

    }

    private void setPriceForRecordSheets(List<RecordSheet> recordSheetList) {
        for (RecordSheet recordSheet : recordSheetList) {
            List<PriceRecord> allPriceRecords = databaseHelper.getPriceRecordsOnRecordSheet(recordSheet.getId());

            // Filtrer les PriceRecords pour ne conserver que ceux correspondant au produit (barcode)
            List<PriceRecord> filteredPriceRecords = new ArrayList<>();
            for (PriceRecord priceRecord : allPriceRecords) {
                if (priceRecord.getProductBarcode().equals(barcode)) {
                    filteredPriceRecords.add(priceRecord);
                }
            }

            // Assigner uniquement les PriceRecords filtrés au RecordSheet
            recordSheet.setPriceRecords(filteredPriceRecords);
        }
    }

    private void updatePriceStats() {
        PriceStats priceStats = databaseHelper.getMinMaxAvgPriceForProduct(barcode);
        if (priceStats != null) {
            productMinPrice.setText(priceStats.getMinPriceFormated());
            productMaxPrice.setText(priceStats.getMaxPriceFormated());
            productMoyPrice.setText(priceStats.getAvgPriceFormated());
        }
    }

}