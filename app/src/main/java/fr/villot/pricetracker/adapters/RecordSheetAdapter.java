package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;

public class RecordSheetAdapter extends RecyclerView.Adapter<RecordSheetViewHolder> {

    private List<RecordSheet> recordSheetList;
    private Context context;
    private SelectionTracker<Long> selectionTracker;

    private OnItemClickListener<RecordSheet> onItemClickListener;

    public interface OnItemClickListener<RecordSheet> {
        void onItemClick(RecordSheet recordSheet);
    }

    public void setOnItemClickListener(OnItemClickListener<RecordSheet> listener) {
        this.onItemClickListener = listener;
    }

    public RecordSheetAdapter(Context context, List<RecordSheet> recordSheetList) {
        this.context = context;
        this.recordSheetList = recordSheetList;
        setHasStableIds(true); // Indique que les IDs sont stables.
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @Override
    public RecordSheetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_record_sheet, parent, false);
        return new RecordSheetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecordSheetViewHolder holder, int position) {
        RecordSheet recordSheet = recordSheetList.get(position);
        holder.bind(recordSheet, selectionTracker.isSelected((long) position));

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(recordSheetList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordSheetList.size();
    }

    public void setItemList(List<RecordSheet> itemList) {
        this.recordSheetList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        // Retourne un ID unique pour chaque élément, par exemple, l'ID de l'élément à la position donnée.
        return position;
    }

    public SelectionTracker<Long> getSelectionTracker() {
        return selectionTracker;
    }

}
