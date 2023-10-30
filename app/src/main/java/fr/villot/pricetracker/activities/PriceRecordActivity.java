package fr.villot.pricetracker.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.snackbar.Snackbar;


import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.fragments.RecordSheetProductsFragment;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;
import fr.villot.pricetracker.utils.DatabaseHelper;


public class PriceRecordActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private List<Product> productList;
    private long recordSheetId;

    private TextView storeNameTextView;
    private TextView storeLocationTextView;
    Toolbar toolbar;
    private ImageView storeImageView;
    private CardView storeCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_record);

        // Récuperation des vues
        storeNameTextView = findViewById(R.id.storeNameTextView);
        storeLocationTextView = findViewById(R.id.storeLocationTextView);
        toolbar = findViewById(R.id.toolbar);
        storeCardView = findViewById(R.id.storeCardView);
        storeCardView.setRadius(0);
        storeImageView = findViewById(R.id.storeImageView);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        // Récupération du nom de la fiche de relevé de prix en paramètre et mise à jour du titre.
        Long recordSheetId = getIntent().getLongExtra("record_sheet_id", -1);
        RecordSheet recordSheet = databaseHelper.getRecordSheetById(recordSheetId);

        // Titre de la toolbar = nom de la RecordSheet
        toolbar.setTitle(recordSheet.getName());
        setSupportActionBar(toolbar);

        // Bouton de retour à l'activité principale
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Récupération du magasin
        Store store = databaseHelper.getStoreById(recordSheet.getStoreId());
        if (store != null) {
            storeNameTextView.setText(store.getName());
            storeLocationTextView.setText(store.getLocation());
            int imageResource = storeImageView.getContext().getResources().getIdentifier(recordSheet.getLogo(), "drawable", storeImageView.getContext().getPackageName());
            storeImageView.setImageResource(imageResource);
        }

        // Fragment qui gere la liste des produits de la recordsheet
        RecordSheetProductsFragment recordSheetProductsFragment = RecordSheetProductsFragment.newInstance(recordSheetId);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, recordSheetProductsFragment)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recordsheet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed(); // Retour à l'activité principale
            return true;
        }
        else if (itemId == R.id.action_add_from_database) {
            addProductsFromDatabase();
            return true;
        }
        else if (itemId == R.id.action_delete) {
            deleteSelectedProducts();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addProductsFromDatabase() {
        Intent intent = new Intent(this, SelectProductsActivity.class);
        startActivity(intent);
    }

    private void deleteSelectedProducts() {
        Snackbar.make(storeNameTextView, "TODO : Supprimer la fiche avec popup de confirmation", Snackbar.LENGTH_SHORT).show();

//        // Récupérer les produits sélectionnés (par exemple, à partir d'une liste de sélection)
//        List<Product> selectedProducts = productAdapter.getSelectedProducts();
//
//        // Supprimer les produits sélectionnés de votre liste de produits
//        productList.removeAll(selectedProducts);
//
//        // Mettre à jour votre RecyclerView
//        productAdapter.notifyDataSetChanged();
    }

}
