package fr.villot.pricetracker.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import fr.villot.pricetracker.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Mise à jour de la version de l'appli
        TextView versionNameTextView = findViewById(R.id.app_version);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            versionNameTextView.setText(getString(R.string.app_version, versionName));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AboutActivity", "Erreur de recuperation des infos du package", e);
        }
    }
}