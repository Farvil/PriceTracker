package fr.villot.pricetracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import fr.villot.pricetracker.model.Product;

public abstract class SelectableAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<T> itemList;
    protected List<Integer> selectedItems = new ArrayList<>();
    protected boolean isSelectionMode = false;
    protected OnItemClickListener<T> onItemClickListener;

    private WeakReference<RecyclerView> recyclerViewWeakReference;

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
        bindViewHolder(holder, itemList.get(position));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int adapterPosition = holder.getAdapterPosition();

                boolean isSelected = isItemSelected(adapterPosition);

                if (isSelected) {
                    selectedItems.remove((Integer) adapterPosition);
                } else {
                    selectedItems.add(adapterPosition);
                }

                if (onItemClickListener != null) {
                    onItemClickListener.onItemLongClick(itemList.get(adapterPosition));
                }

                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(itemList.get(position));
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

    public boolean isItemSelected(int position) {
        return selectedItems.contains(position);
    }

    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        notifyDataSetChanged();
    }

    protected abstract int getItemLayoutId();

    protected abstract VH createViewHolder(View view);

    protected abstract void bindViewHolder(VH holder, T item);

    public VH getViewHolderAtPosition(int position) {
        RecyclerView recyclerView = getRecyclerView();
        if (recyclerView != null) {
            return (VH) recyclerView.findViewHolderForAdapterPosition(position);
        }
        return null;
    }

    private RecyclerView getRecyclerView() {
        if (recyclerViewWeakReference != null) {
            return recyclerViewWeakReference.get();
        }
        return null;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        recyclerViewWeakReference = new WeakReference<>(recyclerView);
    }

}
