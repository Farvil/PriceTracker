package fr.villot.codebarre.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.logging.Logger;

import fr.villot.codebarre.MyApplication;
import fr.villot.codebarre.adapters.PageAdapter;
import fr.villot.codebarre.utils.DatabaseHelper;
import fr.villot.codebarre.R;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private PageAdapter pageAdapter;

    private static final Logger logger = Logger.getLogger(MainActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recuperation des vues
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        pageAdapter = new PageAdapter(getSupportFragmentManager());
        //Set Adapter PageAdapter and glue it together
        viewPager.setAdapter(pageAdapter);
        //Glue TabLayout and ViewPager together
        tabLayout.setupWithViewPager(viewPager);
        //Design purpose. Tabs have the same width
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

    }

}


