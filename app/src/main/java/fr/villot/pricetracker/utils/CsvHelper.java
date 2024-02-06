package fr.villot.pricetracker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.recyclerview.selection.Selection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;

public class CsvHelper {

    private String fileName;
    private Context context;
    private File csvFile;
    private DatabaseHelper databaseHelper;

//    private static final int EXPORT_CSV_REQUEST_CODE = 123;
//    private final Handler handler = new Handler(Looper.getMainLooper());
//    private final Executor executor = Executors.newSingleThreadExecutor();
//
//    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    Intent data = result.getData();
//                    if (data != null) {
//                        Uri uri = data.getData();
//
//                        // Écrire les données dans le fichier à l'emplacement choisi
//                        executor.execute(() -> writeCsvFile(uri));
//                    }
//                }
//            });

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

//    public void exportCsvFile() {
//        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("text/csv");
//        intent.putExtra(Intent.EXTRA_TITLE, fileName);
//
//        launcher.launch(intent);
//    }

}
