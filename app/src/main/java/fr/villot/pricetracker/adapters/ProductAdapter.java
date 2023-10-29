package fr.villot.pricetracker.adapters;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;

public class ProductAdapter extends SelectableAdapter<Product, ProductViewHolder> {

    private Context context;
    private SelectionTracker<Long> selectionTracker;

    public ProductAdapter(Context context, List<Product> productList) {
        super(productList);
        this.context = context;
    }

    @NonNull
    @Override
    protected int getItemLayoutId() {
        return R.layout.item_product;
    }

    @NonNull
    @Override
    protected ProductViewHolder createViewHolder(View view) {
        ProductViewHolder viewHolder = new ProductViewHolder(view);
        viewHolder.setCheckBoxListener(new ProductViewHolder.CheckBoxListener() {
            @Override
            public void onCheckBoxClick(int position, boolean isChecked) {
                if (selectionTracker != null) {
                    long itemId = getItemId(position);
                    if (isChecked) {
                        selectionTracker.select(itemId);
                    } else {
                        selectionTracker.deselect(itemId);
                    }
                }
            }
        });
        return viewHolder;
    }

    @Override
    protected void bindViewHolder(ProductViewHolder holder, Product product) {
        holder.bind(product);
    }

    // Implémentez la méthode getItemId() qui renverra un identifiant unique pour chaque élément dans la liste
    @Override
    public long getItemId(int position) {
        // Retournez l'identifiant unique pour l'élément à la position donnée
        // Ici, vous pouvez utiliser l'identifiant de l'objet Product, par exemple : return productList.get(position).getId();
        return position; // Pour l'exemple, nous utilisons simplement la position comme identifiant
    }

    // Ajoutez cette méthode pour définir le SelectionTracker
    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }
}
