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

public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {

    private List<Product> productList;
    private Context context;
    private SelectionTracker<Long> selectionTracker;

    private ProductAdapter.OnItemClickListener<Product> onItemClickListener;

    public interface OnItemClickListener<Product> {
        void onItemClick(Product product);
    }

    public void setOnItemClickListener(ProductAdapter.OnItemClickListener<Product> listener) {
        this.onItemClickListener = listener;
    }


    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        setHasStableIds(true); // Indique que les IDs sont stables.
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }


    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, selectionTracker.isSelected((long) position));

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(productList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setItemList(List<Product> itemList) {
        this.productList = itemList;
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
