package fr.villot.pricetracker.adapters;//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CheckBox;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//import fr.villot.pricetracker.R;
//import fr.villot.pricetracker.model.Product;
//
//public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder> {
//
//    private Context context;
//    private List<Product> productList;
//
//    public ProductSelectionAdapter(Context context, List<Product> productList) {
//        this.context = context;
//        this.productList = productList;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Product product = productList.get(position);
//        holder.bind(product);
//    }
//
//    @Override
//    public int getItemCount() {
//        return productList.size();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        private TextView productNameTextView;
//        private CheckBox productCheckBox;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            productNameTextView = itemView.findViewById(R.id.productNameTextView);
//            productCheckBox = itemView.findViewById(R.id.productCheckBox);
//        }
//
//        public void bind(Product product) {
//            productNameTextView.setText(product.getName());
//            productCheckBox.setVisibility(View.VISIBLE);
//            productCheckBox.setChecked(product.isSelected());
//
//            // Gérer les changements de sélection de la case à cocher
//            productCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                product.setSelected(isChecked);
//            });
//        }
//    }
//
//    public List<Product> getSelectedProducts() {
//        return productList;
//    }
//}

//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CheckBox;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//import fr.villot.pricetracker.R;
//import fr.villot.pricetracker.model.Product;
//
//public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder> {
//
//    private Context context;
//    private List<Product> productList;
//
//    public ProductSelectionAdapter(Context context, List<Product> productList) {
//        this.context = context;
//        this.productList = productList;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Product product = productList.get(position);
//        holder.bind(product);
//    }
//
//    @Override
//    public int getItemCount() {
//        return productList.size();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        private TextView productNameTextView;
//        private CheckBox productCheckBox;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            productNameTextView = itemView.findViewById(R.id.productNameTextView);
//            productCheckBox = itemView.findViewById(R.id.productCheckBox);
//        }
//
//        public void bind(Product product) {
//            productNameTextView.setText(product.getName());
//            productCheckBox.setVisibility(View.VISIBLE);
//            productCheckBox.setChecked(product.isSelected());
//
//            // Gérer les changements de sélection de la case à cocher
//            productCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                product.setSelected(isChecked);
//            });
//        }
//    }
//
//    public List<Product> getSelectedProducts() {
//        return productList;
//    }
//}
