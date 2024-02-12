package fr.villot.pricetracker.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import fr.villot.pricetracker.BuildConfig;
import fr.villot.pricetracker.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Mise à jour de la version de l'appli
        TextView versionNameTextView = findViewById(R.id.app_version);
        versionNameTextView.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));
    }
}