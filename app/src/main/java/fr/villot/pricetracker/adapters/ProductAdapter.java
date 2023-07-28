package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;

public class ProductAdapter extends SelectableAdapter<Product, ProductViewHolder> implements ProductViewHolder.CheckBoxListener {

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
        ProductViewHolder viewHolder = new ProductViewHolder(view);
        viewHolder.setCheckBoxListener(this); // Assurez-vous que ProductViewHolder reçoit les événements de clic CheckBox
        return viewHolder;
    }

    @Override
    protected void bindViewHolder(ProductViewHolder holder, Product product) {
        holder.bind(product);
    }


    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.setSelectionMode(isSelectionMode);

        holder.productCheckBox.setOnCheckedChangeListener(null);
        holder.productCheckBox.setChecked(isItemSelected(holder.getAdapterPosition()));
        holder.productCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int adapterPosition = holder.getAdapterPosition();
                if (isChecked) {
                    selectedItems.add(adapterPosition);
                } else {
                    selectedItems.remove((Integer) adapterPosition);
                }
            }
        });
    }



    protected void setItemSelection(SelectableAdapter<Product, ProductViewHolder> adapter, int position, boolean isSelected) {
        ProductViewHolder holder = adapter.getViewHolderAtPosition(position);
        if (holder != null) {
            holder.setSelected(isSelected);
        }
    }

    @Override
    public void onCheckBoxClick(int position, boolean isChecked) {
        // Gestion du clic sur la CheckBox pour la sélection directe
        if (isChecked) {
            selectedItems.add(position);
        } else {
            selectedItems.remove((Integer) position);
        }

        setItemSelection(this, position, isChecked);
    }

}
