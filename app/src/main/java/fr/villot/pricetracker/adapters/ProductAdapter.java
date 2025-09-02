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
    private final Context context;
    private SelectionTracker<Long> selectionTracker;

    private ProductAdapter.OnItemClickListener<Product> onItemClickListener;

    public Long getPosition(Product product) {
        int index = productList.indexOf(product);
        return (long) index;
    }

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


    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        boolean isSelected = false;
        if (selectionTracker != null) {
            isSelected = selectionTracker.isSelected((long) position);
        }

        holder.bind(product,isSelected);

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
                onItemClickListener.onItemClick(productList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public int getLastItemPosition() {
        int itemCount = getItemCount();
        int lastPosition = 0;

        if (itemCount > 0) {
            lastPosition = itemCount - 1;
        }

        return lastPosition;
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
