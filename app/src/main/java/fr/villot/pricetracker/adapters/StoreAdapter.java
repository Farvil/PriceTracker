package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.fragments.StoresFragment;
import fr.villot.pricetracker.model.Store;

public class StoreAdapter extends RecyclerView.Adapter<StoreViewHolder> {

    private List<Store> storeList;
    private Context context;
    private SelectionTracker<Long> selectionTracker;

    public StoreAdapter(Context context, List<Store> storeList) {
        this.context = context;
        this.storeList = storeList;
        setHasStableIds(true); // Indique que les IDs sont stables.
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @Override
    public StoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StoreViewHolder holder, int position) {
        Store store = storeList.get(position);
        holder.bind(store, selectionTracker.isSelected((long) position));
    }

    @Override
    public int getItemCount() {
        return storeList.size();
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
