package fr.villot.pricetracker.adapters;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class MyDetailsLookup extends ItemDetailsLookup<Long> {

    private final RecyclerView recyclerView;

    public MyDetailsLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public ItemDetails<Long> getItemDetails(MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof ProductViewHolder) {
                return ((ProductViewHolder) viewHolder).getItemDetails();
            }
            else if (viewHolder instanceof StoreViewHolder) {
                return ((StoreViewHolder) viewHolder).getItemDetails();
            }
            else if (viewHolder instanceof RecordSheetViewHolder) {
                return ((RecordSheetViewHolder) viewHolder).getItemDetails();
            }
            else if (viewHolder instanceof SimpleRecordSheetViewHolder) {
                return ((SimpleRecordSheetViewHolder) viewHolder).getItemDetails();
            }
        }
        return null;
    }
}

