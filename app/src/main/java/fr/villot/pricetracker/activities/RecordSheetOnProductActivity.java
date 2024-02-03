package fr.villot.pricetracker.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.RecordSheetAdapter;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class RecordSheetOnProductActivity extends AppCompatActivity {

    private String barcode;
    private DatabaseHelper databaseHelper;
    private RecyclerView recordSheetRecyclerView;
    private RecordSheetAdapter recordSheetAdapter;
    private List<RecordSheet> recordSheetList;
    private static final String RECORDSHEET_ON_PRODUCT_ACTIVITY_SELECTION_KEY = "recordsheet_on_product_activity_selection";

    private boolean isSelectionModeActive = false;


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
        TextView productBrandTextView = findViewById(R.id.productBrandTextView);
        TextView productQuantityTextView = findViewById(R.id.productQuantityTextView);
        ImageView productImageView = findViewById(R.id.productImageView);
        recordSheetRecyclerView = findViewById(R.id.recordSheetRecyclerView);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        // Bouton de retour à l'activité principale
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        // Récupération du magasin en fonction de l'identifiant passé en paramètre de l'activité
        barcode = getIntent().getStringExtra("barcode");
        Product product = databaseHelper.getProductFromBarCode(barcode);

        if (product != null) {
            productBarcodeTextView.setText(product.getBarcode());
            productNameTextView.setText(product.getName());
            productBrandTextView.setText(product.getBrand());
            productQuantityTextView.setText(product.getQuantity());

            // Utiliser Picasso pour charger l'image à partir de l'URL et l'afficher dans ImageView
            Picasso.get().load(product.getImageUrl()).into(productImageView);
        }

        // Recyclerview
        recordSheetRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Récupération des feuilles de relevés de prix dans la base de données.
        recordSheetList = databaseHelper.getRecordSheetsOnProduct(barcode);

        // Adapter entre RecyclerView et Produit.
        recordSheetAdapter = new RecordSheetAdapter(this, recordSheetList);
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
                    String selectionCount = String.valueOf(numSelectedItems) + " relevé";

                    // Mise au pluriel si plusieurs elements sélectionnés
                    if (numSelectedItems > 1)
                        selectionCount += "s";

                    getSupportActionBar().setTitle(selectionCount);
                }
            }
        });


        //Lancement de l'activité PriceRecordActivity sur click d'un relevé de prix
        recordSheetAdapter.setOnItemClickListener(new RecordSheetAdapter.OnItemClickListener<RecordSheet>() {
            @Override
            public void onItemClick(RecordSheet recordSheet) {
                Intent intent = new Intent(RecordSheetOnProductActivity.this,  PriceRecordActivity.class);
                intent.putExtra("record_sheet_name", recordSheet.getName());
                intent.putExtra("record_sheet_id", recordSheet.getId());
                startActivity(intent);
            }
        });

        productCardView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   showUserQueryDialogBox(product);
               }
           }
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

        // Rafraichissement de la toolbar en cas de changement de mode
        if (this.isSelectionModeActive != isSelectionModeActive) {
            this.isSelectionModeActive = isSelectionModeActive;
            if (!isSelectionModeActive) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
                getSupportActionBar().setTitle(R.string.activity_record_sheet_on_product_title);

            } else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);
            }
            invalidateOptionsMenu(); // Rafraichissement du menu de la toolbar
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

        File privateRootDir = getFilesDir();
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
        Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", csvFile);

        // Intent de partage en ajoutant les droits temporaires d'accès au fichier
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        startActivity(Intent.createChooser(shareIntent, "Partager via"));
    }


    View getProductViewForDialog(Product product, final int layoutResource) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(layoutResource, null);

        // Références des vues dans le layout de la boîte de dialogue
        TextView productBarcodeTextView = dialogView.findViewById(R.id.productBarcodeTextView);
        ImageView productImageView = dialogView.findViewById(R.id.productImageView);
        TextView productNameTextView = dialogView.findViewById(R.id.productNameTextView);
        TextView productBrandTextView = dialogView.findViewById(R.id.productBrandTextView);
        TextView productQuantityTextView = dialogView.findViewById(R.id.productQuantityTextView);

        // Afficher les détails du produit dans les vues
        productBarcodeTextView.setText(product.getBarcode());
        productNameTextView.setText(product.getName());
        productBrandTextView.setText(product.getBrand());
        productQuantityTextView.setText(product.getQuantity());

        // Afficher l'image
        Picasso.get().load(product.getImageUrl()).into(productImageView);

        return dialogView;
    }

    protected void showUserQueryDialogBox(Product product) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getProductViewForDialog(product, R.layout.item_product));
        builder.setTitle( "Lancer un navigateur ?");
        builder.setMessage("Souhaitez vous visualiser les détails de ce produit sur le site OpenFoodFacts ?");
        builder.setCancelable(false);


        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String barcode = product.getBarcode();
                String url = "https://world.openfoodfacts.org/product/" + barcode;

                // Lancer le navigateur
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Non", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }


}