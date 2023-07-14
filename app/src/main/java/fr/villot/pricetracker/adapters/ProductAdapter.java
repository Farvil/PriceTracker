package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private List<Product> selectedProducts = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(Product product);
        void onItemLongClick(Product product);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);

        // Changement de couleur sur click
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    holder.setColorItemPressed();
//                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//                    holder.setColorItemDefault();
//                }
                return false;
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Product product = productList.get(position);
                boolean isSelected = selectedProducts.contains(product);

                if (isSelected) {
                    // Le produit est déjà sélectionné, donc on le supprime de la liste
                    selectedProducts.remove(product);
                    holder.setSelection(false); // Passe en paramètre false pour indiquer que la sélection est désactivée
                } else {
                    // Le produit n'est pas sélectionné, donc on l'ajoute à la liste
                    selectedProducts.add(product);
                    holder.setSelection(true); // Passe en paramètre true pour indiquer que la sélection est activée
                }

                if (onItemClickListener != null) {
                    onItemClickListener.onItemLongClick(product);
                }

                return true;
            }
        });


        // Gestion du click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                // Inverse l'état de sélection lors du clic
//                holder.invertColor();

                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(product);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    public List<Product> getSelectedProducts() {
        return selectedProducts;
    }

}
