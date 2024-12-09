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
import fr.villot.pricetracker.model.Store;

public class StoreAdapter extends RecyclerView.Adapter<StoreViewHolder> {

    private List<Store> storeList;
    private final Context context;
    private SelectionTracker<Long> selectionTracker;

    private StoreAdapter.OnItemClickListener<Store> onItemClickListener;

    public interface OnItemClickListener<Store> {
        void onItemClick(Store store);
    }

    public void setOnItemClickListener(StoreAdapter.OnItemClickListener<Store> listener) {
        this.onItemClickListener = listener;
    }

    public StoreAdapter(Context context, List<Store> storeList) {
        this.context = context;
        this.storeList = storeList;
        setHasStableIds(true); // Indique que les IDs sont stables.
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StoreViewHolder holder, int position) {
        Store store = storeList.get(position);
        holder.bind(store, selectionTracker.isSelected((long) position));

        // Ajout d'une marge bottom au dernier élément pour éviter la superposition avec le bouton flottant
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
                onItemClickListener.onItemClick(storeList.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public int getLastItemPosition() {
        int itemCount = getItemCount();
        int lastPosition = 0;

        if (itemCount > 0) {
            lastPosition = itemCount - 1;
        }

        return lastPosition;
    }

    public void setItemList(List<Store> itemList) {
        this.storeList = itemList;
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
