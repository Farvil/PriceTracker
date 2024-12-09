package fr.villot.pricetracker.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;

public class CsvHelper {

    private final String fileName;
    private final Context context;
    private File csvFile;
    private final DatabaseHelper databaseHelper;

    public CsvHelper(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
        databaseHelper = MyApplication.getDatabaseHelper();

        createCsvFile();

    }

    private void createCsvFile() {
        File privateRootDir = context.getFilesDir();
        File exportDir = new File(privateRootDir, "export");

        // Créer le répertoire s'il n'existe pas
        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                throw new RuntimeException("Échec de la création du répertoire : " + exportDir.getAbsolutePath());
            }
        }
        csvFile = new File(exportDir, fileName);
    }

    public void fillCsvFileWithRecordSheet(RecordSheet recordSheet) {
        // Création de la liste des recordsheets à partager (il n'y en a qu'une)
        List<RecordSheet> recordSheetLst = new ArrayList<>();
        recordSheetLst.add(recordSheet);
        fillCsvFileWithRecordSheets(recordSheetLst);
    }

    public void fillCsvFileWithRecordSheets(List<RecordSheet> recordSheetList) {

        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8);

            // Ajout du BOM pour l'interprétation des accents par excel
            writer.write("\uFEFF");

            // En-têtes CSV
            writer.append("Nom du relevé de prix;Date;Nom du magasin;Localisation du magasin;Code barre;Nom du produit;Marque;Quantité;Image URL;Prix en Euros\n");

            for (RecordSheet recordSheet : recordSheetList) {

                // Récupérer les produits et magasin associés à la RecordSheet
                List<Product> products = databaseHelper.getProductsOnRecordSheet(recordSheet.getId());
                Store store = databaseHelper.getStoreById(recordSheet.getStoreId());

                // Remplir le fichier CSV avec les données de chaque produit
                for (Product product : products) {
                    writer.append(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s\n",
                            recordSheet.getName(),
                            recordSheet.getDate(),
                            store.getName(),
                            store.getLocation(),
                            product.getBarcode(),
                            product.getName(),
                            product.getBrand(),
                            product.getQuantity(),
                            product.getImageUrl(),
                            product.getPrice()));
                }
            }
            writer.close();

        } catch (IOException e) {
            // Gérer l'exception en cas de problème d'écriture
            System.err.println("Erreur lors de l'écriture du fichier CSV: " + e.getMessage());
        }
    }


    public void shareCsvFile() {

        // Uri du fichier à partager via le FileProvider défini dans le manifest.
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", csvFile);

        // Intent de partage en ajoutant les droits temporaires d'accès au fichier
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        context.startActivity(Intent.createChooser(shareIntent, "Partager via"));
    }

    public boolean writeCsvFileToUri(Uri uri) {
        boolean result = false;

        try {
            // Open an OutputStream to the document, overwriting any existing content.
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    FileInputStream fis = new FileInputStream(csvFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    fis.close();
                    outputStream.close();
                    result = true;
                }
            }
        } catch (IOException e) {
            Log.e("CsvHelper", "Erreur lors de l'ouverture de l'uri du csv", e);
        }

        return result;
    }

}
