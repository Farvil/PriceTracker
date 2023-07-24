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
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.logging.Logger;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.adapters.PageAdapter;
import fr.villot.pricetracker.utils.DatabaseHelper;
import fr.villot.pricetracker.R;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PageAdapter pageAdapter;
    private Toolbar toolbar;
    DrawerLayout drawerLayout;

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
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu); // Remplacez ic_menu par votre icône hamburger personnalisée si nécessaire
        }

        pageAdapter = new PageAdapter(getSupportFragmentManager());
        //Set Adapter PageAdapter and glue it together
        viewPager.setAdapter(pageAdapter);
        //Glue TabLayout and ViewPager together
        tabLayout.setupWithViewPager(viewPager);
        //Design purpose. Tabs have the same width
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu resource
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item clicks
        switch (item.getItemId()) {
            case android.R.id.home: // Cela correspond à l'ID par défaut pour le bouton d'accueil (hamburger)
                // Gérer l'action du clic sur le bouton hamburger ici
                // Par exemple, ouvrir le menu déroulant du DrawerLayout
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                // Do something when the settings menu item is clicked
                // Replace this with the action you want to perform
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

}


