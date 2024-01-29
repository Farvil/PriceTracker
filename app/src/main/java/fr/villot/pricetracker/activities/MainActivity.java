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
import fr.villot.pricetracker.model.Store;

public class MainActivity extends AppCompatActivity {
    //    DrawerLayout drawerLayout;
    private boolean isSelectionModeActive = false;
    private boolean showEditIcon = false;
    private Fragment currentFragment;

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
        PageAdapter pageAdapter = new PageAdapter(getSupportFragmentManager());
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
                setSelectionMode(currentFragment, false); // Quitter le mode de sélection
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
            if (currentFragment instanceof RecordSheetsFragment) {
                MenuItem itemShare = menu.getItem(0);
                itemShare.setVisible(true);
            }

            // Icone d'edition du magasin
            if (showEditIcon) {
                if (currentFragment instanceof StoresFragment) {
                    MenuItem itemEdit = menu.getItem(1);
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
                setSelectionMode(currentFragment, false);
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
    public void setSelectionMode(Fragment fragment, boolean isSelectionModeActive) {

        //Sauvegarde le fragment en cours pour les actions de la toolbar
        currentFragment = fragment;

        // Rafraichissement de la toolbar si nécessaire
        if (this.isSelectionModeActive != isSelectionModeActive) {
            this.isSelectionModeActive = isSelectionModeActive;
            if (!isSelectionModeActive) {
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
//                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
                getSupportActionBar().setTitle(R.string.app_name);

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

            } else {
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);
            }
            invalidateOptionsMenu(); // Rafraichissement du menu de la toolbar
        }
    }

    @SuppressLint("RestrictedApi")
    public void showEditIcon() {
        showEditIcon = true;
        //TODO: verifier si correct
        getSupportActionBar().invalidateOptionsMenu();
    }

    @SuppressLint("RestrictedApi")
    public void hideEditIcon() {
        showEditIcon = false;
        //TODO: verifier si correct
        getSupportActionBar().invalidateOptionsMenu();
    }

}


