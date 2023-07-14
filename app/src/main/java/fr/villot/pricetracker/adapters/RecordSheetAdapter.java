package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.icu.text.AlphabeticIndex;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.RecordSheet;

public class RecordSheetAdapter extends RecyclerView.Adapter<RecordSheetViewHolder> {

    private Context context;
    private List<RecordSheet> recordSheetList;
    private List<RecordSheet> selectedRecordSheets = new ArrayList<>();
    public interface OnItemClickListener {
        void onItemClick(RecordSheet recordSheet);
        void onItemLongClick(RecordSheet recordSheet);
    }


    private RecordSheetAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(RecordSheetAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public RecordSheetAdapter(Context context, List<RecordSheet> recordSheetList) {
        this.context = context;
        this.recordSheetList = recordSheetList;
    }

    @NonNull
    @Override
    public RecordSheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record_sheet, parent, false);
        return new RecordSheetViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull RecordSheetViewHolder holder, int position) {
        RecordSheet recordSheet = recordSheetList.get(position);
        holder.bind(recordSheet);

        // Changement de couleur sur click
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    holder.setColorItemPressed();
//                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//                    holder.setColorItemDefault();
//                }
                return false;
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                RecordSheet recordSheet = recordSheetList.get(position);
                boolean isSelected = selectedRecordSheets.contains(recordSheet);

                if (isSelected) {
                    // Le recordSheet est déjà sélectionné, donc on le supprime de la liste
                    selectedRecordSheets.remove(recordSheet);
                    holder.setSelection(false); // Passe en paramètre false pour indiquer que la sélection est désactivée
                } else {
                    // Le recordSheet n'est pas sélectionné, donc on l'ajoute à la liste
                    selectedRecordSheets.add(recordSheet);
                    holder.setSelection(true); // Passe en paramètre true pour indiquer que la sélection est activée
                }

                if (onItemClickListener != null) {
                    onItemClickListener.onItemLongClick(recordSheet);
                }

                return true;
            }
        });


        // Gestion du click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                // Inverse l'état de sélection lors du clic
//                holder.invertColor();

                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(recordSheet);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return recordSheetList.size();
    }

    public void setRecordSheetList(List<RecordSheet> recordSheetList) {
        this.recordSheetList = recordSheetList;
        notifyDataSetChanged();
    }

    public List<RecordSheet> getSelectedRecordSheets() {
        return selectedRecordSheets;
    }

}
