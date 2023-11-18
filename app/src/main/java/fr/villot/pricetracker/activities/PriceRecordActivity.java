package fr.villot.pricetracker.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;


import java.io.Serializable;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.fragments.ProductsFragment;
import fr.villot.pricetracker.fragments.RecordSheetProductsFragment;
import fr.villot.pricetracker.fragments.RecordSheetsFragment;
import fr.villot.pricetracker.fragments.StoresFragment;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;
import fr.villot.pricetracker.utils.DatabaseHelper;


public class PriceRecordActivity extends AppCompatActivity {

    private static final int SELECT_PRODUCTS_REQUEST_CODE = 1;
    private DatabaseHelper databaseHelper;
    private List<Product> productList;
    private long recordSheetId;

    private TextView storeNameTextView;
    private TextView storeLocationTextView;
    Toolbar toolbar;
    private ImageView storeImageView;
    private CardView storeCardView;
    private boolean isSelectionModeActive = false;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PRODUCTS_REQUEST_CODE && resultCode == RESULT_OK) {
            // Récupérer la liste des produits sélectionnés
            List<Product> selectedProducts = (List<Product>) data.getSerializableExtra("selected_products");


            // Obtenez une référence au fragment
            RecordSheetProductsFragment fragment = (RecordSheetProductsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

            // Appelez la méthode addOrUpdateProduct du fragment
            if (fragment != null) {
                for (Product selectedProduct : selectedProducts) {
                    fragment.addOrUpdateProduct(selectedProduct);
                }
            }
        }
    }


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
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
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

        MenuInflater inflater = getMenuInflater();
        if (!isSelectionModeActive)
            inflater.inflate(R.menu.menu_recordsheet, menu);
        else
            inflater.inflate(R.menu.toolbar_selection_menu, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (!isSelectionModeActive) {
                onBackPressed(); // Retour à l'activité principale
            }
            else {
                setSelectionMode(false);
            }
            return true;
        }
        else if (itemId == R.id.action_add_from_database) {
            addProductsFromDatabase();
            return true;
        }
        else if (itemId == R.id.action_delete) {
            // Obtenez une référence au fragment
            RecordSheetProductsFragment recordSheetProductsFragment = (RecordSheetProductsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

            // Appelez la méthode addOrUpdateProduct du fragment
            if (recordSheetProductsFragment != null) {
                recordSheetProductsFragment.deleteSelectedItems();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addProductsFromDatabase() {
        Intent intent = new Intent(this, SelectProductsActivity.class);
        startActivityForResult(intent, SELECT_PRODUCTS_REQUEST_CODE);
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

    public void setSelectionMode(boolean isSelectionModeActive) {

        // Rafraichissement de la toolbar si nécessaire
        if (this.isSelectionModeActive != isSelectionModeActive) {
            this.isSelectionModeActive = isSelectionModeActive;
            if (!isSelectionModeActive) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
                getSupportActionBar().setTitle(R.string.app_name);

                // Obtenez une référence au fragment
                RecordSheetProductsFragment recordSheetProductsFragment = (RecordSheetProductsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

                // Appelez la méthode addOrUpdateProduct du fragment
                if (recordSheetProductsFragment != null) {
                    recordSheetProductsFragment.clearSelection();
                }

            } else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);
            }
            invalidateOptionsMenu(); // Rafraichissement du menu de la toolbar
        }
    }

}
