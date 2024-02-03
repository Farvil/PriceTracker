package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.RecordSheet;

public class SimpleRecordSheetAdapter extends RecyclerView.Adapter<SimpleRecordSheetViewHolder> {

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

    public SimpleRecordSheetAdapter(Context context, List<RecordSheet> recordSheetList) {
        this.context = context;
        this.recordSheetList = recordSheetList;
        setHasStableIds(true); // Indique que les IDs sont stables.
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @Override
    public SimpleRecordSheetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_simple_record_sheet, parent, false);
        return new SimpleRecordSheetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleRecordSheetViewHolder holder, int position) {
        RecordSheet recordSheet = recordSheetList.get(position);
        holder.bind(recordSheet, selectionTracker.isSelected((long) position));

        // Ajout d'une marge bottom au dernier élément pour eviter la superposition avec le bouton flottant
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (position == getItemCount() - 1) {
            layoutParams.bottomMargin = context.getResources().getDimensionPixelSize(R.dimen.margin_bottom_last_item);
            holder.itemView.setLayoutParams(layoutParams);
        } else {
            // Réinitialisation des marges pour les éléments précédents
            layoutParams.bottomMargin = 0;
            holder.itemView.setLayoutParams(layoutParams);
        }

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
