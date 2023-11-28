package fr.villot.pricetracker.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.logging.Logger;

import fr.villot.pricetracker.adapters.PageAdapter;
import fr.villot.pricetracker.fragments.ProductsFragment;
import fr.villot.pricetracker.fragments.RecordSheetsFragment;
import fr.villot.pricetracker.fragments.StoresFragment;
import fr.villot.pricetracker.R;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PageAdapter pageAdapter;
    private Toolbar toolbar;
    DrawerLayout drawerLayout;
    private boolean isSelectionModeActive = false;
    private Fragment currentFragment;

    private static final Logger logger = Logger.getLogger(MainActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recuperation des vues
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);

        // Toolbar
        setSupportActionBar(toolbar);

        // Ajout du bouton hamburger
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

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
        if (isSelectionModeActive)
            inflater.inflate(R.menu.toolbar_selection_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item clicks
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isSelectionModeActive) {
                    setSelectionMode(currentFragment, false);
                } else {
                    // Ouverture du menu déroulant hamburger
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_share:
                if (currentFragment instanceof RecordSheetsFragment) {
                    RecordSheetsFragment recordSheetsFragment = (RecordSheetsFragment) currentFragment;
                    recordSheetsFragment.shareRecordSheet();
                }
                return true;
            case R.id.action_delete:
                if (currentFragment instanceof ProductsFragment) {
                    ProductsFragment productsFragment = (ProductsFragment) currentFragment;
                    productsFragment.deleteSelectedItems();
                }
                else if (currentFragment instanceof StoresFragment) {
                    StoresFragment storesFragment = (StoresFragment) currentFragment;
                    storesFragment.deleteSelectedItems();
                }
                else if (currentFragment instanceof RecordSheetsFragment) {
                    RecordSheetsFragment recordSheetsFragment = (RecordSheetsFragment) currentFragment;
                    recordSheetsFragment.deleteSelectedItems();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Gestion du mode de sélection
    public void setSelectionMode(Fragment fragment, boolean isSelectionModeActive) {

        //Sauvegarde le fragment en cours pour les actions de la toolbar
        currentFragment = fragment;

        // Rafraichissement de la toolbar si nécessaire
        if (this.isSelectionModeActive != isSelectionModeActive) {
            this.isSelectionModeActive = isSelectionModeActive;
            if (!isSelectionModeActive) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
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
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);
            }
            invalidateOptionsMenu(); // Rafraichissement du menu de la toolbar
        }
    }
}


