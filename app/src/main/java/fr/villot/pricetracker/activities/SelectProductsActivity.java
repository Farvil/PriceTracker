package fr.villot.pricetracker.activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.selection.OnContextClickListener;
import androidx.recyclerview.selection.OnItemActivatedListener;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.adapters.MyDetailsLookup;
import fr.villot.pricetracker.adapters.ProductAdapter;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class SelectProductsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private Button confirmButton;
    SelectionTracker<Long> selectionTracker;
    private static final String PRODUCT_ACTIVITY_SELECTION_KEY = "product_activity_selection";
    private long recordSheetId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_products);

        productRecyclerView = findViewById(R.id.productRecyclerView);
        confirmButton = findViewById(R.id.confirmButton);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Recupération du recordsheetId
        recordSheetId = getIntent().getLongExtra("record_sheet_id", -1);

        // Récupération des produits dans la base de données.
        productList = databaseHelper.getProductsNotInRecordSheet(recordSheetId);

        if(productList.isEmpty()) {
            // Retourne la liste des produits à l'activité appelante
            setResult(RESULT_CANCELED);
            finish();
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
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build();
        productAdapter.setSelectionTracker(selectionTracker);
        
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onItemStateChanged(Long key, boolean selected) {
                // Logique à effectuer lorsqu'un élément est sélectionné ou désélectionné
            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();

                // Logique à effectuer lorsqu'il y a un changement dans la sélection
                int numSelected = selectionTracker.getSelection().size();
                // ...
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectionTracker != null) {
                    // Récupérer les éléments sélectionnés
                    Selection<Long> selection = selectionTracker.getSelection();

                    // Créer une liste pour stocker les produits sélectionnés
                    List<Product> selectedProducts = new ArrayList<>();

                    // Parcourir la sélection et ajouter les produits correspondants à la liste
                    for (Long selectedItem : selection) {
                        Product selectedProduct = productList.get(selectedItem.intValue());
                        selectedProducts.add(selectedProduct);
                    }

                    // Retourner la liste des produits à l'activité appelante
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selected_products", (Serializable) selectedProducts);
                    setResult(RESULT_OK, resultIntent);

                    // Fermer l'activité
                    finish();
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (selectionTracker != null) {
            selectionTracker.clearSelection();
        }
    }


}
