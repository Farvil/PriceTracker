package fr.villot.pricetracker.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.logging.Logger;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.adapters.PageAdapter;
import fr.villot.pricetracker.fragments.StoresFragment;
import fr.villot.pricetracker.utils.DatabaseHelper;
import fr.villot.pricetracker.R;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PageAdapter pageAdapter;
    private Toolbar toolbar;
    DrawerLayout drawerLayout;
    private boolean isSelectionModeActive = false;
    private StoresFragment storesFragment;


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

        pageAdapter = new PageAdapter(getSupportFragmentManager());
        //Set Adapter PageAdapter and glue it together
        viewPager.setAdapter(pageAdapter);
        //Glue TabLayout and ViewPager together
        tabLayout.setupWithViewPager(viewPager);
        //Design purpose. Tabs have the same width
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        // Récupération du fragment storesFragment
        storesFragment = (StoresFragment) pageAdapter.getItem(pageAdapter.getItemPositionFromTitle("Magasins"));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // L'utilisateur a changé d'onglet
                setSelectionMode(false); // Quitter le mode de sélection
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu resource
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
                    setSelectionMode(false);
                } else {
                    // Ouverture du menu déroulant hamburger
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            case R.id.action_settings:
                // Do something when the settings menu item is clicked
                // Replace this with the action you want to perform
                return true;
            case R.id.action_delete:
                storesFragment.deleteSelectedItems();
                return true;
            // Add more cases if you have more menu items
            // For example, if you have another menu item with ID "action_item2":
            // case R.id.action_item2:
            //     // Do something for action_item2
            //     return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setSelectionMode(boolean isSelectionModeActive) {
        // Rafraichissement de la toolbar si nécessaire
        if (this.isSelectionModeActive != isSelectionModeActive) {
            this.isSelectionModeActive = isSelectionModeActive;
            if (!isSelectionModeActive) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
                getSupportActionBar().setTitle(R.string.app_name);

                storesFragment.clearSelection();

//                // Demande au fragment StoreFragment d'annuler la sélection
//                if (viewPager != null && pageAdapter != null) {
////TODO: revoir
//// viewPager.getCurrentItem()
//                    Fragment fragment = pageAdapter.getItem(pageAdapter.getItemPositionFromTitle("Magasins"));
//
//                    if (fragment instanceof StoresFragment) {
//                        StoresFragment storesFragment = (StoresFragment) fragment;
//                        storesFragment.clearSelection();
//                    }
//                }

            } else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);
            }
            invalidateOptionsMenu(); // Rafraichissement du menu de la toolbar
        }
    }
}


