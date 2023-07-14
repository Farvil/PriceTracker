package fr.villot.pricetracker.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.villot.pricetracker.MyApplication;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.activities.PriceRecordActivity;
import fr.villot.pricetracker.adapters.RecordSheetAdapter;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.utils.DatabaseHelper;

public class RecordSheetsFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private ListView recordSheetListView;
    private FloatingActionButton fabAdd;
    private RecordSheetAdapter recordSheetAdapter;
    private List<RecordSheet> recordSheetList;

    public static RecordSheetsFragment newInstance() {
        return new RecordSheetsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_sheet, container, false);

        // Recuperation des vues
        recordSheetListView = view.findViewById(R.id.recordSheetListView);
        fabAdd = view.findViewById(R.id.fabAdd);

        // Initialisation du DatabaseHelper
        databaseHelper = MyApplication.getDatabaseHelper();

        // Action du bouton flottant
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddRecordSheetDialog();
            }
        });


//        // Listener pour le click sur un item
//        recordSheetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // Lancer l'activité PriceRecordActivity avec le nom du relevé de prix en paramètre.
//                String recordSheetName = "Mon Relevé de prix";
//                Intent intent = new Intent(getActivity(), PriceRecordActivity.class);
//                intent.putExtra("record_sheet_name", recordSheetName);
//                startActivity(intent);
//            }
//        });

        // Initialisation de la liste et de son adapter
        recordSheetList = new ArrayList<>();
        recordSheetAdapter = new RecordSheetAdapter(getContext(), recordSheetList);
        recordSheetListView.setAdapter(recordSheetAdapter);

        // Listener sur le click d'un relevé de prix.
        recordSheetAdapter.setOnItemClickListener(new RecordSheetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecordSheet recordSheet) {
                Intent intent = new Intent(getActivity(), PriceRecordActivity.class);
                intent.putExtra("record_sheet_name", recordSheet.getName());
                intent.putExtra("record_sheet_id", recordSheet.getId());
                startActivity(intent);
            }
        });

        // Mise à jour de la liste des fiches d'enregistrement depuis la base de donnees
        updateRecordSheetListViewFromDatabase(false);

        return view;
    }

    private void updateRecordSheetListViewFromDatabase(boolean lastItemDisplayed) {
        // Obtenir la liste des produits à partir de la base de données
        recordSheetList = databaseHelper.getAllRecordSheets();

        // Ajouter les produits à l'adaptateur
        recordSheetAdapter.clear();
        recordSheetAdapter.addAll(recordSheetList);

        if (lastItemDisplayed) {
            // Positionnement de la ListView en dernier item pour voir le produit ajouté.
            int dernierIndice = recordSheetAdapter.getCount() - 1;
            recordSheetListView.setSelection(dernierIndice);
        }
    }

    public void showAddRecordSheetDialog() {
        // Création d'une boîte de dialogue pour demander le nom du relevé de prix
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Nom du relevé de prix");

        // Ajout d'un champ de saisie
        final EditText input = new EditText(getActivity());
        builder.setView(input);

        // Ajout des boutons "Annuler" et "Valider"
        builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String recordScheetName = input.getText().toString();
                if (!recordScheetName.isEmpty()) {

//                    // Recuperation de la date actuelle
//                    Date now = new Date();
//                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY HH:mm");
//                    String date = formatter.format(now);

                    // Création d'un RecordSheet
                    RecordSheet newRecordSheet = new RecordSheet(recordScheetName, new Date(),1);

                    // Ajoute la fiche d'enregistrements à la base de données
                    databaseHelper.addRecordSheet(newRecordSheet);

                    // Update the store list and refresh the spinner
                    updateRecordSheetListViewFromDatabase(true);

//
//                    // Création d'un nouvel objet PriceRecord
//                    PriceRecordList priceRecordList = new PriceRecordList(recordName, null);
//
//                    // Ajout du nouvel objet à la liste
//                    priceRecordList.add(priceRecord);
//                    priceRecordListAdapter.notifyDataSetChanged();
                }
            }
        });

        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Affichage de la boîte de dialogue
        builder.show();
    }

}
