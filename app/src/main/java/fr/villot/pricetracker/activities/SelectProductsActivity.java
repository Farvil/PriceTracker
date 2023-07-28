package fr.villot.pricetracker.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

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

        // Activer le mode de sélection
        productAdapter.setSelectionMode(true);
        productAdapter.setRecyclerView(productRecyclerView);
    }
}