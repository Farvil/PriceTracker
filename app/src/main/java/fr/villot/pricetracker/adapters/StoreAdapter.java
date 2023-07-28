package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;
import fr.villot.pricetracker.model.Store;

public class StoreAdapter extends SelectableAdapter<Store, StoreViewHolder> {

    private Context context;

    public StoreAdapter(Context context, List<Store> storeList) {
        super(storeList);
        this.context = context;
    }

    @NonNull
    @Override
    protected int getItemLayoutId() {
        return R.layout.item_store;
    }

    @NonNull
    @Override
    protected StoreViewHolder createViewHolder(View view) {
        return new StoreViewHolder(view);
    }

    @Override
    protected void bindViewHolder(StoreViewHolder holder, Store store) {
        holder.bind(store);
    }

//    protected void setItemSelection(StoreViewHolder holder, int position, boolean isSelected) {
//        holder.setSelected(isSelected);
//    }
    protected void setItemSelection(SelectableAdapter<Store, StoreViewHolder> adapter, int position, boolean isSelected) {
        StoreViewHolder holder = adapter.getViewHolderAtPosition(position);
        if (holder != null) {
            holder.setSelected(isSelected);
        }
    }
}
