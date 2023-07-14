package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;

public class ProductAdapter extends SelectableAdapter<Product, ProductViewHolder> {

    private Context context;

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
        return new ProductViewHolder(view);
    }

    @Override
    protected void bindViewHolder(ProductViewHolder holder, Product product) {
        holder.bind(product);
    }

    @Override
    protected void setItemSelection(ProductViewHolder holder, Product product, boolean isSelected) {
        holder.setSelection(isSelected);
    }
}
