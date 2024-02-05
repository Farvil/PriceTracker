package fr.villot.pricetracker;

import android.app.Application;
import android.content.Context;

import fr.villot.pricetracker.utils.DatabaseHelper;

// On crée un singleton pour la gestion de la base de donnée partout dans l'application.
public class MyApplication extends Application {
    private static DatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = DatabaseHelper.getInstance(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    public static DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

}
