package lk.graynode.mobifit.ui.buyitems;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.graynode.mobifit.ProductItemView;
import lk.graynode.mobifit.R;
import lk.graynode.mobifit.databinding.FragmentBuyitemsBinding;
import lk.graynode.mobifit.entity.ProductItem;

public class BuyItemsFragment extends Fragment {

    private FragmentBuyitemsBinding binding;
    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private ProductItemAdapter adapter;
    private List<ProductItem> productItems;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBuyitemsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.scheduleTypesRecylclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productItems = new ArrayList<>();
        adapter = new ProductItemAdapter(productItems);
        recyclerView.setAdapter(adapter);

        TextView searchText=binding.searchText;
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                loadProducts(searchText.getText().toString());
            }
        });

        firestore = FirebaseFirestore.getInstance();
        loadProducts("");

        return root;
    }

    private void loadProducts(String search) {

        if(search.isEmpty()){
            firestore.collection("product").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        productItems.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            ProductItem item = document.toObject(ProductItem.class);
                            if (item != null) productItems.add(item);
                        }
                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Products Searching Failed", Toast.LENGTH_SHORT).show());
        }else{
            String text=search.toLowerCase();

            firestore.collection("product").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            productItems.clear();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String title = document.getString("ptitle");
                                if (title != null && title.toLowerCase().contains(text)) {
                                    ProductItem product = document.toObject(ProductItem.class);
                                    productItems.add(product);
                                }

                            }
                            requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Products Searching Failed", Toast.LENGTH_SHORT).show());
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

class ProductItemAdapter extends RecyclerView.Adapter<ProductItemAdapter.ProductItemViewHolder> {
    private final List<ProductItem> productItems;

    static class ProductItemViewHolder extends RecyclerView.ViewHolder {
        TextView pItemTitle, pItemDescription, pItemPrice;
        ImageView pItemImage;

        public ProductItemViewHolder(@NonNull View itemView) {
            super(itemView);
            pItemTitle = itemView.findViewById(R.id.pItem_title);
            pItemDescription = itemView.findViewById(R.id.pItem_description);
            pItemPrice = itemView.findViewById(R.id.pItem_price);
            pItemImage = itemView.findViewById(R.id.pItemImage);
        }
    }

    public ProductItemAdapter(List<ProductItem> productItems) {
        this.productItems = productItems;
    }

    @NonNull
    @Override
    public ProductItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductItemViewHolder holder, int position) {
        ProductItem productItem = productItems.get(position);

        holder.pItemTitle.setText(productItem.getPtitle());
        holder.pItemDescription.setText(formatDescription(productItem.getPdescription()));
        holder.pItemPrice.setText("Rs." + productItem.getPrice());

        Glide.with(holder.itemView.getContext())
                .load(productItem.getUrl())
                .apply(new RequestOptions().timeout(60000))
                .into(holder.pItemImage);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ProductItemView.class);
            intent.putExtra("pid", productItem.getPid());
            intent.putExtra("ptitle", productItem.getPtitle());
            intent.putExtra("pdescription", productItem.getPdescription());
            intent.putExtra("price", productItem.getPrice());
            intent.putExtra("qty", productItem.getQty());
            intent.putExtra("url", productItem.getUrl());
            view.getContext().startActivity(intent);
        });
    }

    private String formatDescription(String description) {
        int maxLength = 50;
        return (description.length() > maxLength ? description.substring(0, maxLength) + "..." : description);
    }

    @Override
    public int getItemCount() {
        return productItems.size();
    }
}
