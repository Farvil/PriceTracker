package fr.villot.pricetracker.activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.ProductAdapter;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class SelectProductsActivity extends AppCompatActivity {

    private ProductAdapter productAdapter;
    private List<Product> productList;
    SelectionTracker<Long> selectionTracker;
    private static final String PRODUCT_ACTIVITY_SELECTION_KEY = "product_activity_selection";
    boolean isSelectionModeActive = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_products);

        // Récupération des vues
        RecyclerView productRecyclerView = findViewById(R.id.productRecyclerView);
        Button confirmButton = findViewById(R.id.confirmButton);
        Toolbar toolbar = findViewById(R.id.toolbar);


        // Initialisation du DatabaseHelper
        DatabaseHelper databaseHelper = MyApplication.getDatabaseHelper();

        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Recupération du recordsheetId
        int recordSheetId = getIntent().getIntExtra("record_sheet_id", -1);

        // Récupération des produits dans la base de données.
        productList = databaseHelper.getProductsNotInRecordSheet(recordSheetId);

        if(productList.isEmpty()) {
            // Retourne la liste des produits à l'activité appelante
            setResult(RESULT_CANCELED);
            finish();
        }

        // Ajout Toolbar
        setSupportActionBar(toolbar);

        // Bouton de retour à l'activité principale
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        // Adapter entre RecyclerView et Produit.
        productAdapter = new ProductAdapter(this, productList);
        productRecyclerView.setAdapter(productAdapter);

        // Gestion de la selection d'items
        selectionTracker = new SelectionTracker.Builder<>(
                PRODUCT_ACTIVITY_SELECTION_KEY,
                productRecyclerView,
                new StableIdKeyProvider(productRecyclerView),
                new MyDetailsLookup(productRecyclerView),
                StorageStrategy.createLongStorage()
        ).build();
        productAdapter.setSelectionTracker(selectionTracker);

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();

                int numSelectedItems = selectionTracker.getSelection().size();


                if (numSelectedItems == 0) {
                    setSelectionMode(false);                }
                else {
                    setSelectionMode(true);

                    // Modification du titre de la toolbar pour indiquer le nombre d'éléments sélectionnés.
                    String selectionCount = numSelectedItems + " produit";

                    // Mise au pluriel de "relevé" si plusieurs elements sélectionnés
                    if (numSelectedItems > 1)
                        selectionCount += "s";

                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        getSupportActionBar().setTitle(selectionCount);
                    }
                }

            }

        });

        // Gestion du click sur un produit
        productAdapter.setOnItemClickListener(product -> {
            if (!selectionTracker.hasSelection()) {
                Long position = productAdapter.getPosition(product);
                if (position.intValue() != RecyclerView.NO_POSITION) {
                    selectionTracker.select(position);
                }
            }
        });

        confirmButton.setOnClickListener(view -> {
            if (selectionTracker != null) {
                // Récupérer les éléments sélectionnés
                List<Product> selectedProducts = getSelectedProducts();

                // Retourner la liste des produits à l'activité appelante
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_products", (Serializable) selectedProducts);
                setResult(RESULT_OK, resultIntent);

                // Fermer l'activité
                finish();
            }
        });


    }

    private @NonNull List<Product> getSelectedProducts() {
        Selection<Long> selection = selectionTracker.getSelection();

        // Créer une liste pour stocker les produits sélectionnés
        List<Product> selectedProducts = new ArrayList<>();

        // Parcourir la sélection et ajouter les produits correspondants à la liste
        for (Long selectedItem : selection) {
            Product selectedProduct = productList.get(selectedItem.intValue());
            selectedProducts.add(0, selectedProduct);
        }
        return selectedProducts;
    }

    public void setSelectionMode(boolean isSelectionModeActive) {

        // Rafraîchissement de la toolbar si nécessaire
        if (this.isSelectionModeActive != isSelectionModeActive) {
            this.isSelectionModeActive = isSelectionModeActive;
            if (!isSelectionModeActive) {
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
                getSupportActionBar().setTitle(R.string.select_products_activity_title);
            } else {
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_cancel);
            }
            invalidateOptionsMenu(); // Rafraîchissement du menu de la toolbar
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (selectionTracker != null) {
            selectionTracker.clearSelection();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        if (!isSelectionModeActive)
            inflater.inflate(R.menu.menu_select_products_activity, menu);
        else
            inflater.inflate(R.menu.toolbar_selection_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            if (!isSelectionModeActive) {
                onBackPressed(); // Retour à l'activité parente
            }
            else {
                selectionTracker.clearSelection();
            }
            return true;
        } else if (itemId == R.id.menu_select_all) {

            List<Long> selectedItems = new ArrayList<>();
            for (int i = 0 ; i < productAdapter.getItemCount() ; i++) {
                selectedItems.add((long) i);
            }
            selectionTracker.setItemsSelected(selectedItems, true);

            return true;
        }


        return super.onOptionsItemSelected(item);
    }


}
