package fr.villot.pricetracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectableAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<T> itemList;
    protected List<T> selectedItems = new ArrayList<>();
    protected OnItemClickListener<T> onItemClickListener;

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
        void onItemLongClick(T item);
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.onItemClickListener = listener;
    }

    public SelectableAdapter(List<T> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getItemLayoutId(), parent, false);
        return createViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        T item = itemList.get(position);
        bindViewHolder(holder, item);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                boolean isSelected = selectedItems.contains(item);

                if (isSelected) {
                    selectedItems.remove(item);
                    setItemSelection(holder, item, false);
                } else {
                    selectedItems.add(item);
                    setItemSelection(holder, item, true);
                }

                if (onItemClickListener != null) {
                    onItemClickListener.onItemLongClick(item);
                }

                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setItemList(List<T> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    public List<T> getSelectedItems() {
        return selectedItems;
    }

    protected abstract int getItemLayoutId();

    protected abstract VH createViewHolder(View view);

    protected abstract void bindViewHolder(VH holder, T item);

    protected abstract void setItemSelection(VH holder, T item, boolean isSelected);
}
