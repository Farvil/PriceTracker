package fr.villot.pricetracker.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.fragments.ProductsFragment;
import fr.villot.pricetracker.fragments.ProductsOnRecordSheetFragment;
import fr.villot.pricetracker.fragments.RecordSheetsFragment;
import fr.villot.pricetracker.fragments.RecordSheetsOnStoreFragment;
import fr.villot.pricetracker.fragments.StoresFragment;
import fr.villot.pricetracker.interfaces.OnSelectionChangedListener;
import fr.villot.pricetracker.interfaces.OnStoreChangedListener;
import fr.villot.pricetracker.model.Store;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class RecordSheetActivity extends AppCompatActivity implements OnStoreChangedListener, OnSelectionChangedListener {

    private int storeId;
    private boolean isSelectionModeActive = false;
    private boolean showEditIcon = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sheet);

        // Récuperation des vues
        TextView storeNameTextView = findViewById(R.id.storeNameTextView);
        TextView storeLocationTextView = findViewById(R.id.storeLocationTextView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        CardView storeCardView = findViewById(R.id.storeCardView);
        storeCardView.setRadius(0);
        ImageView storeImageView = findViewById(R.id.storeImageView);

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
        storeId = getIntent().getIntExtra("store_id", -1);
        Store store = databaseHelper.getStoreById(storeId);
        if (store != null) {
            storeNameTextView.setText(store.getName());
            storeLocationTextView.setText(store.getLocation());
            int imageResource = storeImageView.getContext().getResources().getIdentifier(store.getLogo(), "drawable", storeImageView.getContext().getPackageName());
            storeImageView.setImageResource(imageResource);
        }

        // Fragment qui gere la liste des recordsheets associées au magasin
        RecordSheetsOnStoreFragment recordSheetsOnStoreFragment = RecordSheetsOnStoreFragment.newInstance(storeId);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, recordSheetsOnStoreFragment)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
        if (isSelectionModeActive) {
            inflater.inflate(R.menu.toolbar_selection_menu, menu);
            MenuItem itemShare = menu.findItem(R.id.action_share);
            itemShare.setVisible(true);

            // Icone d'edition pour modifier un magasin ou un relevé de prix
            if (showEditIcon) {
                MenuItem itemEdit = menu.findItem(R.id.action_edit);
                itemEdit.setVisible(true);
            }
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
            // Appel à la méthode shareRecordSheet() du fragment
            RecordSheetsOnStoreFragment recordSheetsOnStoreFragment = (RecordSheetsOnStoreFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (recordSheetsOnStoreFragment != null) {
                recordSheetsOnStoreFragment.shareRecordSheet();
            }
            return true;
        } else if (itemId == R.id.action_edit) {

                return true;
        }
        else if (itemId == R.id.action_delete) {
            // Appel à la méthode deleteSelectedItems() du fragment
            RecordSheetsOnStoreFragment recordSheetsOnStoreFragment = (RecordSheetsOnStoreFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (recordSheetsOnStoreFragment != null) {
                recordSheetsOnStoreFragment.deleteSelectedItems();
            }
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
                getSupportActionBar().setTitle(R.string.activity_record_sheet_title);

            } else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);
            }
            invalidateOptionsMenu(); // Rafraichissement du menu de la toolbar
        }
    }

    public void clearSelection() {

        // Appel à la méthode clearSelection() du fragment
        RecordSheetsOnStoreFragment recordSheetsOnStoreFragment = (RecordSheetsOnStoreFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (recordSheetsOnStoreFragment != null) {
            recordSheetsOnStoreFragment.clearSelection();
        }

    }

    @Override
    public void onSelectionChanged(Fragment fragment, int numSelectedItems) {

        // Attention fragment n'est pas le bon car celui du TabLayout de la mainActivity (voir getInstance et newInstance)

        if (numSelectedItems == 0) {
            hideEditIcon();
            setSelectionMode(false);
        }
        else {

            // Icone d'édition (pour modifier un relevé de prix) si uniquement un seul element sélectionné
            if (numSelectedItems == 1)
                showEditIcon();
            else
                hideEditIcon();

            setSelectionMode(true);

            // Modification du titre de la toolbar pour indiquer le nombre d'éléments sélectionnés.
            String selectionCount = String.valueOf(numSelectedItems) + " relevé";

            // Mise au pluriel de "relevé" si plusieurs elements sélectionnés
            if (numSelectedItems > 1)
                selectionCount += "s";

            getSupportActionBar().setTitle(selectionCount);
        }
    }

    @SuppressLint("RestrictedApi")
    public void showEditIcon() {
        if (!showEditIcon) {
            showEditIcon = true;
            getSupportActionBar().invalidateOptionsMenu();
        }
    }

    @SuppressLint("RestrictedApi")
    public void hideEditIcon() {
        if (showEditIcon) {
            showEditIcon = false;
            getSupportActionBar().invalidateOptionsMenu();
        }
    }

    @Override
    public void onStoreChanged(int storeId) {

    }
}