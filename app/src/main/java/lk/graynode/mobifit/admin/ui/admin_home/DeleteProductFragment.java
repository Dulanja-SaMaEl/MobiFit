package lk.graynode.mobifit.admin.ui.admin_home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.graynode.mobifit.databinding.FragmentDeleteProductBinding;

public class DeleteProductFragment extends Fragment {
    private FragmentDeleteProductBinding binding;

    private FirebaseFirestore firestore;
    EditText dId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDeleteProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firestore=FirebaseFirestore.getInstance();

        Button deleteProduct = binding.btnDeleteProduct;
        deleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dId = binding.dId;

                String did = dId.getText().toString();

                if (did.isEmpty()) {
                    Toast.makeText(getContext(), "Product Id Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else {
                    deleteProduct(did);
                }
            }
        });

        return root;
    }

    private void deleteProduct(String did) {
        Log.i("did", did);
        firestore.collection("product")
                .whereEqualTo("pid", did) // Find document by pid
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            document.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), "Product Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    dId.setText("");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Product Deleting Failed", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), "Product Not Found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error Finding Product", Toast.LENGTH_SHORT).show()
                );
    }
}