package fr.villot.pricetracker.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.adapters.PageAdapter;
import fr.villot.pricetracker.fragments.ProductsFragment;
import fr.villot.pricetracker.fragments.RecordSheetsFragment;
import fr.villot.pricetracker.fragments.StoresFragment;
import fr.villot.pricetracker.interfaces.OnSelectionChangedListener;
import fr.villot.pricetracker.interfaces.OnStoreChangedListener;

public class MainActivity extends AppCompatActivity implements OnStoreChangedListener, OnSelectionChangedListener {
    //    DrawerLayout drawerLayout;
    private boolean isSelectionModeActive = false;
    private boolean showEditIcon = false;
    private Fragment currentFragment;
    private PageAdapter pageAdapter;

//    private static final Logger logger = Logger.getLogger(MainActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recuperation des vues
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.viewPager);
        Toolbar toolbar = findViewById(R.id.toolbar);
//        drawerLayout = findViewById(R.id.drawerLayout);

        // Toolbar
        setSupportActionBar(toolbar);

//        // Ajout du bouton hamburger
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
//        }

        // Gestion de la navigation par onglets
        pageAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pageAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        // Annulation du mode de selection sur changement d'onglet
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                clearSelection();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
        if (isSelectionModeActive) {
            inflater.inflate(R.menu.toolbar_selection_menu, menu);

            // Icone de partage dans le fragment des Recordsheets
            if (currentFragment instanceof RecordSheetsFragment) {
                MenuItem itemShare = menu.findItem(R.id.action_share);
                itemShare.setVisible(true);
            }

            // Icone d'edition pour modifier un magasin ou un relevé de prix
            if (showEditIcon) {
                if (currentFragment instanceof StoresFragment
                        || currentFragment instanceof RecordSheetsFragment) {
                    MenuItem itemEdit = menu.findItem(R.id.action_edit);
                    itemEdit.setVisible(true);
                }
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
            }
//          else {
//                // Ouverture du menu déroulant hamburger
//                drawerLayout.openDrawer(GravityCompat.START);
//            }
            return true;
        } else if (itemId == R.id.action_share) {

            if (currentFragment instanceof RecordSheetsFragment) {
                RecordSheetsFragment recordSheetsFragment = (RecordSheetsFragment) currentFragment;
                recordSheetsFragment.shareRecordSheet();
            }
            return true;
        } else if (itemId == R.id.action_edit) {

            if (currentFragment instanceof StoresFragment) {
                StoresFragment storesFragment = (StoresFragment) currentFragment;
                storesFragment.editStore();
            }
            return true;
        }
        else if (itemId == R.id.action_delete) {
            if (currentFragment instanceof ProductsFragment) {
                ProductsFragment productsFragment = (ProductsFragment) currentFragment;
                productsFragment.deleteSelectedItems();
            } else if (currentFragment instanceof StoresFragment) {
                StoresFragment storesFragment = (StoresFragment) currentFragment;
                storesFragment.deleteSelectedItems();
            } else if (currentFragment instanceof RecordSheetsFragment) {
                RecordSheetsFragment recordSheetsFragment = (RecordSheetsFragment) currentFragment;
                recordSheetsFragment.deleteSelectedItems();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Gestion du mode de sélection
    public void setSelectionMode(boolean isSelectionModeActive) {

        // Rafraichissement de la toolbar si nécessaire
        if (this.isSelectionModeActive != isSelectionModeActive) {
            this.isSelectionModeActive = isSelectionModeActive;
            if (!isSelectionModeActive) {
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
//                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
                getSupportActionBar().setTitle(R.string.app_name);

            } else {
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);
            }
            invalidateOptionsMenu(); // Rafraichissement du menu de la toolbar
        }
    }

    public void clearSelection() {
        if (currentFragment instanceof ProductsFragment) {
            ProductsFragment productsFragment = (ProductsFragment) currentFragment;
            productsFragment.clearSelection();
        }
        else if (currentFragment instanceof StoresFragment) {
            StoresFragment storesFragment = (StoresFragment) currentFragment;
            storesFragment.clearSelection();
        }
        else if (currentFragment instanceof RecordSheetsFragment) {
            RecordSheetsFragment recordSheetsFragment = (RecordSheetsFragment) currentFragment;
            recordSheetsFragment.clearSelection();
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

        // Récupération du fragment des relevés de prix
        int recordSheetTabIndex = pageAdapter.getItemPositionFromTitle("Relevés de prix");
        Fragment fragment = pageAdapter.getItem(recordSheetTabIndex);

        if (fragment instanceof RecordSheetsFragment) {
            RecordSheetsFragment recordSheetsFragment = (RecordSheetsFragment) fragment;

            // Par simplicité on recharge toute la liste. Si problème de performances, recharger uniquement le nécessaire
            recordSheetsFragment.updateRecordSheetListViewFromDatabase(false);
        }
    }

    @Override
    public void onSelectionChanged(Fragment fragment, int numSelectedItems) {

        // Sauvegarde le fragment en cours pour les actions de la toolbar
        currentFragment = fragment;

        if (numSelectedItems == 0) {
            hideEditIcon();
            setSelectionMode(false);
        }
        else {

            // Icone d'édition (pour modifier un magasin ou un relevé de prix) si uniquement un seul element sélectionné
            if (numSelectedItems == 1)
                showEditIcon();
            else
                hideEditIcon();

            setSelectionMode(true);

            // Modification du titre de la toolbar pour indiquer le nombre d'éléments sélectionnés.
            String selectionCount = String.valueOf(numSelectedItems) ;
            if (currentFragment instanceof ProductsFragment) {
                selectionCount += " produit";
            }
            else if (currentFragment instanceof StoresFragment) {
                selectionCount += " magasin";
            }
            else if (currentFragment instanceof RecordSheetsFragment) {
                selectionCount += " relevé";
            }
            else {
                selectionCount += " élément";
            }

            // Mise au pluriel si plusieurs elements sélectionnés
            if (numSelectedItems > 1)
                selectionCount += "s";

            getSupportActionBar().setTitle(selectionCount);
        }
    }

}


