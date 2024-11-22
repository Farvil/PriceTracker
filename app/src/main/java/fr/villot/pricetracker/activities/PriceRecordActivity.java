package fr.villot.pricetracker.activities;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.fragments.ProductsOnRecordSheetFragment;
import fr.villot.pricetracker.interfaces.OnSelectionChangedListener;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;
import fr.villot.pricetracker.utils.DatabaseHelper;


public class PriceRecordActivity extends AppCompatActivity implements OnSelectionChangedListener {

    private static final int SELECT_PRODUCTS_REQUEST_CODE = 1;
//    private List<Product> productList;
    private int recordSheetId;
    RecordSheet recordSheet;

    private TextView storeNameTextView;
    Toolbar toolbar;
    private boolean isSelectionModeActive = false;

//    private OnProductDeletedFromRecordSheetListener onProductDeletedFromRecordSheetListener = null;

    private final ActivityResultLauncher<Intent> createDocumentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            // Référence du fragment
                            ProductsOnRecordSheetFragment fragment = (ProductsOnRecordSheetFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                            if (fragment != null) {
                                View fragmentView = fragment.getView();
                                if (fragmentView != null) {
                                    if (fragment.exportRecordSheet(uri)) {
                                        Snackbar.make(fragmentView, "Le fichier CSV est enregistré.", Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Snackbar.make(fragmentView, "Erreur d'enregistrement du fichier CSV !", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                }
            });

    /**
     * Callback pour le resultat correspondant aux produits sélectionnés depuis la bibliothèque de produits
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PRODUCTS_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;

            List<Product> selectedProducts = new ArrayList<>();

            // On récupère la liste des produits sérialisés
            Serializable serializableData = data.getSerializableExtra("selected_products");
            if (serializableData instanceof List<?>) {
                List<?> rawList = (List<?>) serializableData;

                // On vérifie individuellement que chaque élément soit de type Product
                for (Object item : rawList) {
                    if (item instanceof Product) {
                        selectedProducts.add((Product) item);
                    }
                }
            }

            // Ajout ou mise à jour du produit depuis depuis le fragment
            ProductsOnRecordSheetFragment fragment = (ProductsOnRecordSheetFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (fragment != null) {
                for (Product selectedProduct : selectedProducts) {
                    fragment.addOrUpdateProduct(selectedProduct);
                }
            }

        } else if (requestCode == SELECT_PRODUCTS_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            Snackbar.make(storeNameTextView, "Aucun produit supplémentaire disponible dans la bibliothèque", Snackbar.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_record);

        // Récuperation des vues
        storeNameTextView = findViewById(R.id.storeNameTextView);
        TextView storeLocationTextView = findViewById(R.id.storeLocationTextView);
        toolbar = findViewById(R.id.toolbar);
        CardView storeCardView = findViewById(R.id.storeCardView);
        storeCardView.setRadius(0);
        ImageView storeImageView = findViewById(R.id.storeImageView);

        // Initialisation du DatabaseHelper
        DatabaseHelper databaseHelper = MyApplication.getDatabaseHelper();

        // Récupération du nom de la fiche de relevé de prix en paramètre et mise à jour du titre.
        recordSheetId = getIntent().getIntExtra("record_sheet_id", -1);
        recordSheet = databaseHelper.getRecordSheetById(recordSheetId);

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
            int imageResource = storeImageView.getContext().getResources().getIdentifier(recordSheet.getStore().getLogo(), "drawable", storeImageView.getContext().getPackageName());
            storeImageView.setImageResource(imageResource);
        }

        // Fragment qui gere la liste des produits de la recordsheet
        ProductsOnRecordSheetFragment productsOnRecordSheetFragment = ProductsOnRecordSheetFragment.newInstance(recordSheetId);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, productsOnRecordSheetFragment)
                .commit();


        // Récupération de la référence de l'interface depuis les extras de l'intent
//        onProductDeletedFromRecordSheetListener = (OnProductDeletedFromRecordSheetListener) getIntent().getSerializableExtra("listener");
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
                clearSelection();
            }
            return true;
        }
        else if (itemId == R.id.action_add_from_database) {
            addProductsFromDatabase();
            return true;
        }
        else if (itemId == R.id.action_delete) {
            // Obtenez une référence au fragment
            ProductsOnRecordSheetFragment productsOnRecordSheetFragment = (ProductsOnRecordSheetFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (productsOnRecordSheetFragment != null) {
                productsOnRecordSheetFragment.deleteSelectedItems();
            }
            return true;
        }
        else if (itemId == R.id.action_share) {
            ProductsOnRecordSheetFragment productsOnRecordSheetFragment = (ProductsOnRecordSheetFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (productsOnRecordSheetFragment != null) {
                productsOnRecordSheetFragment.shareRecordSheet();
            }
            return true;
        }
        else if (itemId == R.id.action_export) {
            ProductsOnRecordSheetFragment productsOnRecordSheetFragment = (ProductsOnRecordSheetFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (productsOnRecordSheetFragment != null) {

                // Demande le nom du fichier à l'utilisateur
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "export_releve_de_prix.csv");

                createDocumentLauncher.launch(intent);
            }
            return true;
        } else if (itemId == R.id.menu_select_all) {
            ProductsOnRecordSheetFragment productsOnRecordSheetFragment = (ProductsOnRecordSheetFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (productsOnRecordSheetFragment != null) {
                productsOnRecordSheetFragment.selectAllItems();
            }

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void addProductsFromDatabase() {
        Intent intent = new Intent(this, SelectProductsActivity.class);
        intent.putExtra("record_sheet_id", recordSheetId);
        startActivityForResult(intent, SELECT_PRODUCTS_REQUEST_CODE);
    }

    public void setSelectionMode(boolean isSelectionModeActive) {

        // Rafraichissement de la toolbar si nécessaire
        if (this.isSelectionModeActive != isSelectionModeActive) {
            this.isSelectionModeActive = isSelectionModeActive;
            if (!isSelectionModeActive) {
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
                getSupportActionBar().setTitle(recordSheet.getName());
            } else {
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_cancel);
            }
            invalidateOptionsMenu(); // Rafraichissement du menu de la toolbar
        }
    }

    public void clearSelection() {

        // Appel à la méthode clearSelection() du fragment
        ProductsOnRecordSheetFragment productsOnRecordSheetFragment = (ProductsOnRecordSheetFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (productsOnRecordSheetFragment != null) {
            productsOnRecordSheetFragment.clearSelection();
        }
    }

    @Override
    public void onSelectionChanged(Fragment fragment, int numSelectedItems) {

        // Sauvegarde le fragment en cours pour les actions de la toolbar
        // currentFragment = fragment;

        if (numSelectedItems == 0) {
            setSelectionMode(false);
        }
        else {
            setSelectionMode(true);

            // Modification du titre de la toolbar pour indiquer le nombre d'éléments sélectionnés.
            String selectionCount = numSelectedItems + " relevé";

            // Mise au pluriel de "relevé" si plusieurs elements sélectionnés
            if (numSelectedItems > 1)
                selectionCount += "s";

            // Titre de ma toolbar
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(selectionCount);
            }
        }
    }

    public void notifyProductModifiedFromRecordSheet(String barcode) {
        // On informe l'activité appelante de la suppression du produit
        Intent resultIntent = new Intent();
        resultIntent.putExtra("update_required", true);
        resultIntent.putExtra("barcode", barcode);
        setResult(RESULT_OK, resultIntent);
    }

}
