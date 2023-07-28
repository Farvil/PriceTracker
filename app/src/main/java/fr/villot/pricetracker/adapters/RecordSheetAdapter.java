package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.RecordSheet;

public class RecordSheetAdapter extends SelectableAdapter<RecordSheet, RecordSheetViewHolder> {

    private Context context;

    public RecordSheetAdapter(Context context, List<RecordSheet> recordSheetList) {
        super(recordSheetList);
        this.context = context;
    }

    @NonNull
    @Override
    protected int getItemLayoutId() {
        return R.layout.item_record_sheet;
    }

    @NonNull
    @Override
    protected RecordSheetViewHolder createViewHolder(View view) {
        return new RecordSheetViewHolder(view);
    }

    @Override
    protected void bindViewHolder(RecordSheetViewHolder holder, RecordSheet recordSheet) {
        holder.bind(recordSheet);
    }

//    protected void setItemSelection(RecordSheetViewHolder holder, int position, boolean isSelected) {
//        holder.setSelected(isSelected);
//    }
    protected void setItemSelection(SelectableAdapter<RecordSheet, RecordSheetViewHolder> adapter, int position, boolean isSelected) {
        RecordSheetViewHolder holder = adapter.getViewHolderAtPosition(position);
        if (holder != null) {
            holder.setSelected(isSelected);
        }
    }
}
