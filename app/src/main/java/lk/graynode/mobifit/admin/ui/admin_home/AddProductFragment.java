package lk.graynode.mobifit.admin.ui.admin_home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import lk.graynode.mobifit.databinding.FragmentAddProductBinding;
import lk.graynode.mobifit.entity.ProductItem;

public class AddProductFragment extends Fragment {

    private FragmentAddProductBinding binding;

    private FirebaseFirestore firestore;

    EditText pTitle, pDescription, pPrice, pQty, pUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAddProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button addProduct=binding.btnAddProduct;
        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pTitle = binding.pTitle;
                pDescription = binding.pDescription;
                pPrice = binding.pPrice;
                pQty =binding.pQty;
                pUrl = binding.pUrl;

                String title = pTitle.getText().toString();
                String description = pDescription.getText().toString();
                String price = pPrice.getText().toString();
                String qty = pQty.getText().toString();
                String url = pUrl.getText().toString();

                if (title.isEmpty()) {
                    Toast.makeText(getContext(), "Product Title Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Product Description Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (price.isEmpty()) {
                    Toast.makeText(getContext(), "Product Price Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (qty.isEmpty()) {
                    Toast.makeText(getContext(), "Product Quantity Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (url.isEmpty()) {
                    Toast.makeText(getContext(), "Product URL Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else {

                    firestore = FirebaseFirestore.getInstance();

                    ProductItem product = new ProductItem();
                    product.setPtitle(title);
                    product.setPdescription(description);
                    product.setPrice(price);
                    product.setQty(qty);
                    product.setUrl(url);

                    saveProduct(product);


                }
            }
        });
        // Inflate the layout for this fragment
        return root;
    }
    private void saveProduct(ProductItem product) {
        loadProductId(new ProductIdCallback() {
            @Override
            public void onProductIdReceived(int newPid) {
                product.setPid(String.valueOf(newPid)); // Set new PID before saving

                firestore.collection("product").add(product)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Product Added Successfully", Toast.LENGTH_SHORT).show();
                                pTitle.setText("");
                                pDescription.setText("");
                                pPrice.setText("");
                                pQty.setText("");
                                pUrl.setText("");
                            } else {
                                Toast.makeText(getContext(), "Product Adding Failed", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Product Adding Failed", Toast.LENGTH_SHORT).show()
                        );
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to get new Product ID", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadProductId(ProductIdCallback callback) {
        firestore.collection("product")
                .orderBy("pid", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot latestDoc = queryDocumentSnapshots.getDocuments().get(0);
                        ProductItem productItem = latestDoc.toObject(ProductItem.class);

                        int newPid = Integer.parseInt(productItem.getPid()) + 1;
                        callback.onProductIdReceived(newPid); // Callback function
                    } else {
                        callback.onProductIdReceived(1); // Start from 1 if no products exist
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Product ID Fetch Failed", Toast.LENGTH_SHORT).show();
                    callback.onFailure(e);
                });
    }

    public interface ProductIdCallback {
        void onProductIdReceived(int newPid);
        void onFailure(Exception e);
    }

}