package fr.villot.pricetracker.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Date;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.activities.PriceRecordActivity;
import fr.villot.pricetracker.adapters.RecordSheetAdapter;
import fr.villot.pricetracker.adapters.SpinnerStoreAdapter;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class RecordSheetsFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private RecyclerView recordSheetRecyclerView;
    private RecordSheetAdapter recordSheetAdapter;
    private List<RecordSheet> recordSheetList;
    private FloatingActionButton fabAdd;

    public static RecordSheetsFragment newInstance() {
        return new RecordSheetsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recuperation des vues
        recordSheetRecyclerView = view.findViewById(R.id.recordSheetRecyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        recordSheetRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Récupération des feuilles de relevés de prix dans la base de données.
        recordSheetList = getRecordSheets();

        // Adapter entre RecyclerView et Produit.
        recordSheetAdapter = new RecordSheetAdapter(getActivity(), recordSheetList);
        recordSheetRecyclerView.setAdapter(recordSheetAdapter);

        recordSheetAdapter.setOnItemClickListener(new RecordSheetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object item) {
                if (item instanceof RecordSheet) {
                    RecordSheet recordSheet = (RecordSheet) item;
                    Intent intent = new Intent(getActivity(), PriceRecordActivity.class);
                    intent.putExtra("record_sheet_name", recordSheet.getName());
                    intent.putExtra("record_sheet_id", recordSheet.getId());
                    startActivity(intent);

                }
            }

            @Override
            public void onItemLongClick(Object item) {
                if (item instanceof RecordSheet) {
                    RecordSheet recordSheet = (RecordSheet) item;
                    Snackbar.make(recordSheetRecyclerView, "TODO : Afficher les options du relevé de prix : "
                                    + recordSheet.getName(),
                            Snackbar.LENGTH_LONG).show();

                }
            }

        });

        // Action du bouton flottant
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateRecordSheetDialog();
            }
        });

    }


    protected List<RecordSheet> getRecordSheets() {
        return databaseHelper.getAllRecordSheets();
    }

    private void updateRecordSheetListViewFromDatabase(boolean lastItemDisplayed) {
        // Obtenir la liste des produits à partir de la base de données
        recordSheetList = databaseHelper.getAllRecordSheets();

        // Ajouter les produits à l'adaptateur
        recordSheetAdapter.setItemList(recordSheetList);

        if (lastItemDisplayed) {
            // Positionnement de la ListView en dernier item pour voir le produit ajouté.
            int dernierIndice = recordSheetAdapter.getItemCount() - 1;
            recordSheetRecyclerView.smoothScrollToPosition(dernierIndice);
        }
    }


    public void showCreateRecordSheetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_recordsheet, null);
        builder.setView(dialogView);
        builder.setTitle("Créer un nouveau relevé de prix");

        Spinner storeSpinner = dialogView.findViewById(R.id.storeSpinner);
        EditText recordsheetNameEditText = dialogView.findViewById(R.id.recordsheetNameEditText);

        // Obtenez la liste des magasins à partir de votre base de données (par exemple, dans une liste storesList)

        // Créez un ArrayAdapter pour afficher la liste des magasins dans le Spinner
        List<Store> storeList = databaseHelper.getAllStores();
        SpinnerStoreAdapter spinnerStoreAdapter = new SpinnerStoreAdapter(getContext(), storeList);
        storeSpinner.setAdapter(spinnerStoreAdapter);

        builder.setPositiveButton("Créer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Récupérez le nom de la recordsheet saisi par l'utilisateur
                String recordSheetName = recordsheetNameEditText.getText().toString();

                if (!recordSheetName.isEmpty()) {

                    // Recuperation du magasin selectionné
                    Store selectedStore = (Store) storeSpinner.getSelectedItem();
                    if (selectedStore != null) {
                        // Création d'un RecordSheet
                        RecordSheet newRecordSheet = new RecordSheet(recordSheetName, new Date(),selectedStore.getId(), selectedStore.getLogo());

                        // Ajoute la fiche d'enregistrements à la base de données
                        databaseHelper.addRecordSheet(newRecordSheet);

                        // Update the store list and refresh the spinner
                        updateRecordSheetListViewFromDatabase(true);
                    }

                }

            }
        });

        builder.setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


//    public void showAddRecordSheetDialog() {
//        // Création d'une boîte de dialogue pour demander le nom du relevé de prix
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Nom du relevé de prix");
//
//        // Ajout d'un champ de saisie
//        final EditText input = new EditText(getActivity());
//        builder.setView(input);
//
//        // Ajout des boutons "Annuler" et "Valider"
//        builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String recordScheetName = input.getText().toString();
//                if (!recordScheetName.isEmpty()) {
//
////                    // Recuperation de la date actuelle
////                    Date now = new Date();
////                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY HH:mm");
////                    String date = formatter.format(now);
//
//                    // Création d'un RecordSheet
//                    RecordSheet newRecordSheet = new RecordSheet(recordScheetName, new Date(),1);
//
//                    // Ajoute la fiche d'enregistrements à la base de données
//                    databaseHelper.addRecordSheet(newRecordSheet);
//
//                    // Update the store list and refresh the spinner
//                    updateRecordSheetListViewFromDatabase(true);
//
////
////                    // Création d'un nouvel objet PriceRecord
////                    PriceRecordList priceRecordList = new PriceRecordList(recordName, null);
////
////                    // Ajout du nouvel objet à la liste
////                    priceRecordList.add(priceRecord);
////                    priceRecordListAdapter.notifyDataSetChanged();
//                }
//            }
//        });
//
//        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        // Affichage de la boîte de dialogue
//        builder.show();
//    }

}
