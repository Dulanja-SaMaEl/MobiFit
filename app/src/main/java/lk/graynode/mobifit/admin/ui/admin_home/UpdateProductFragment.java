package lk.graynode.mobifit.admin.ui.admin_home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import lk.graynode.mobifit.databinding.FragmentUpdateProductBinding;
import lk.graynode.mobifit.entity.ProductItem;


public class UpdateProductFragment extends Fragment {

    private FragmentUpdateProductBinding binding;

    FirebaseFirestore firestore;

    EditText uId, uTitle, uDescription, uPrice, uQty, uUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUpdateProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        Button updateProduct = binding.btnUpdateProduct;
        updateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uId = binding.uId;
                uTitle = binding.uTitle;
                uDescription = binding.uDescription;
                uPrice = binding.uPrice;
                uQty = binding.uQty;
                uUrl = binding.uUrl;

                String title = uTitle.getText().toString();
                String uid = uId.getText().toString();
                String description = uDescription.getText().toString();
                String price = uPrice.getText().toString();
                String qty = uQty.getText().toString();
                String url = uUrl.getText().toString();

                if (uid.isEmpty()) {
                    Toast.makeText(getContext(), "Product Id Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (title.isEmpty()) {
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
                    product.setPid(uid);
                    product.setPtitle(title);
                    product.setPdescription(description);
                    product.setPrice(price);
                    product.setQty(qty);
                    product.setUrl(url);

                    updateProduct(product);


                }
            }
        });


        // Inflate the layout for this fragment
        return root;

    }

    private void updateProduct(ProductItem product) {

        firestore.collection("product").whereEqualTo("pid",product.getPid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if(!queryDocumentSnapshots.isEmpty()){
                    DocumentSnapshot documentSnapshot=queryDocumentSnapshots.getDocuments().get(0);
                    if(documentSnapshot.exists()){
                        // Create a map with updated data
                        HashMap<String, Object> productUpdatedData = new HashMap<>();
                        productUpdatedData.put("ptitle", product.getPtitle());
                        productUpdatedData.put("pdescription", product.getPdescription());
                        productUpdatedData.put("price", product.getPrice());
                        productUpdatedData.put("qty", product.getQty());
                        productUpdatedData.put("url", product.getUrl());


                        firestore.collection("product").document(documentSnapshot.getId()).update(productUpdatedData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getContext(), "Product Updated Successfully", Toast.LENGTH_SHORT).show();
                                uId.setText("");
                                uTitle.setText("");
                                uDescription.setText("");
                                uPrice.setText("");
                                uQty.setText("");
                                uUrl.setText("");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Product Updating Failed", Toast.LENGTH_SHORT).show();

                            }
                        });

                    }else{
                        Toast.makeText(getContext(), "Product Not Available", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), "Product Not Available", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}