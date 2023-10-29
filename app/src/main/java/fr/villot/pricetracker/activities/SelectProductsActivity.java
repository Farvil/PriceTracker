package fr.villot.pricetracker.activities;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.adapters.ProductAdapter;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class SelectProductsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_products);

        productRecyclerView = findViewById(R.id.productRecyclerView);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Récupération des produits dans la base de données.
        productList = databaseHelper.getAllProducts();

        // Adapter entre RecyclerView et Produit.
        productAdapter = new ProductAdapter(this, productList);
        productRecyclerView.setAdapter(productAdapter);

//        // Créez et définissez le SelectionTracker pour gérer la sélection
//        SelectionTracker<Long> selectionTracker = new SelectionTracker.Builder<>(
//                "productSelection",
//                productRecyclerView,
//                new ProductAdapter.ProductItemKeyProvider(productRecyclerView), // Utilisez notre ItemKeyProvider personnalisé
//                new ProductAdapter.DetailsLookup(productRecyclerView),
//                StorageStrategy.createLongStorage()
//        ).build();

//        // Écoutez les changements de sélection et mettez à jour l'interface utilisateur en conséquence
//        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
//            @Override
//            public void onSelectionChanged() {
//                super.onSelectionChanged();
//                if (actionMode == null) {
//                    actionMode = startActionMode(actionModeCallback);
//                }
//                int selectedItemsCount = selectionTracker.getSelection().size();
//                if (selectedItemsCount == 0) {
//                    actionMode.finish();
//                } else {
//                    actionMode.setTitle(getString(R.string.selected_items, selectedItemsCount));
//                }
//            }
//        });
//
//        // Définissez le SelectionTracker dans l'adaptateur pour gérer la sélection
//        productAdapter.setSelectionTracker(selectionTracker);
    }

    // Le reste de votre code pour le mode d'action (ActionMode) et les actions de menu reste inchangé
}
