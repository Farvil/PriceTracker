package fr.villot.pricetracker.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;

public class CsvHelper {

    private String fileName;
    private Context context;
    private File csvFile;
    private DatabaseHelper databaseHelper;

//    private final ActivityResultLauncher<Intent> createDocumentLauncher;

    public CsvHelper(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
        databaseHelper = MyApplication.getDatabaseHelper();

        createCsvFile();

//        // On verifie si le contexte est une activité pour utiliser registerForActivityResult
//        if (context instanceof AppCompatActivity) {
//            createDocumentLauncher = ((AppCompatActivity) context).registerForActivityResult(
//                    new ActivityResultContracts.StartActivityForResult(),
//                    result -> {
//                        if (result.getResultCode() == Activity.RESULT_OK) {
//                            Intent data = result.getData();
//                            if (data != null) {
//                                Uri uri = data.getData();
//                                if (uri != null) {
//                                    // Écrire les données dans le fichier à l'emplacement choisi
//                                    writeCsvFile(uri);
//                                }
//                            }
//                        }
//                    });
//        } else {
//            // Gérer le cas où le contexte n'est pas une activité
//            createDocumentLauncher = null;
//        }

    }

    private void createCsvFile() {
        File privateRootDir = context.getFilesDir();
        File exportDir = new File(privateRootDir, "export");

        // Créer le répertoire s'il n'existe pas
        if (!exportDir.exists()) {
            exportDir.mkdirs();
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
        try (FileWriter writer = new FileWriter(csvFile)) {
            // En-têtes CSV
            writer.append("Nom du relevé de Prix,Date,Nom du magasin,Localisation du magasin, Code barre,Nom du produit,Marque,Quantité,Image URL,Prix\n");

            for (RecordSheet recordSheet : recordSheetList) {

                // Récupérer les produits et magasin associés à la RecordSheet
                List<Product> products = databaseHelper.getProductsOnRecordSheet(recordSheet.getId());
                Store store = databaseHelper.getStoreById(recordSheet.getStoreId());

                // Remplir le fichier CSV avec les données de chaque produit
                for (Product product : products) {
                    writer.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void shareCsvFile() {

        // Uri du fichier à partager via le FileProvider defini dans le manifest.
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", csvFile);

        // Intent de partage en ajoutant les droits temporaires d'accès au fichier
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        context.startActivity(Intent.createChooser(shareIntent, "Partager via"));
    }

    public void exportCsvFile() {
//        if (createDocumentLauncher != null) {
//            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.setType("text/csv");
//            intent.putExtra(Intent.EXTRA_TITLE, fileName);
//
//            createDocumentLauncher.launch(intent);
//        }
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
            e.printStackTrace();
        }

        return result;
    }

}
