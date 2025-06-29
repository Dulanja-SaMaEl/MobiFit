package lk.graynode.mobifit.ui.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.graynode.mobifit.Home;
import lk.graynode.mobifit.MainActivity;
import lk.graynode.mobifit.R;
import lk.graynode.mobifit.databinding.FragmentTrackordersBinding;
import lk.graynode.mobifit.databinding.FragmentUserBinding;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPref = getContext().getSharedPreferences("lk.graynode.mobifit.data", Context.MODE_PRIVATE);
        String email = sharedPref.getString("email", "");

        Button btnLogout=root.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear(); // Clears all stored preferences
                editor.apply(); // Apply changes asynchronously

                // Redirect to Login Activity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
                startActivity(intent);
                getActivity().finish();

            }
        });


        if (!email.isEmpty()) {

            EditText userAddress = root.findViewById(R.id.userAddress);
            EditText userMobile = root.findViewById(R.id.userMobile);
            EditText userPassword = root.findViewById(R.id.userPassword);
            EditText userHeight = root.findViewById(R.id.userHeight);
            EditText userWeight = root.findViewById(R.id.userWeight);

            String address = userAddress.getText().toString();
            String mobile = userMobile.getText().toString();
            String password = userPassword.getText().toString();
            String height = userHeight.getText().toString();
            String weight = userWeight.getText().toString();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("user")
                    .whereEqualTo("email", email)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                            if (!documentSnapshotList.isEmpty()) {

                                DocumentSnapshot firstDocument=documentSnapshotList.get(0);

                                userAddress.setText(firstDocument.getString("address"));
                                userMobile.setText(firstDocument.getString("mobile"));
                                userPassword.setText(firstDocument.getString("password"));
                                userHeight.setText(firstDocument.getString("height"));
                                userWeight.setText(firstDocument.getString("weight"));

                                Button updateUserDataBtn = root.findViewById(lk.graynode.mobifit.R.id.updateUserDataBtn);
                                updateUserDataBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        EditText userAddress = root.findViewById(R.id.userAddress);
                                        EditText userMobile = root.findViewById(R.id.userMobile);
                                        EditText userPassword = root.findViewById(R.id.userPassword);
                                        EditText userHeight = root.findViewById(R.id.userHeight);
                                        EditText userWeight = root.findViewById(R.id.userWeight);

                                        String address = userAddress.getText().toString();
                                        String mobile = userMobile.getText().toString();
                                        String password = userPassword.getText().toString();
                                        String height = userHeight.getText().toString();
                                        String weight = userWeight.getText().toString();

                                        if(address.isEmpty()){
                                            Toast.makeText(getContext(), "Address Can Not Be Empty", Toast.LENGTH_SHORT).show();
                                        } else if (mobile.isEmpty()) {
                                            Toast.makeText(getContext(), "Mobile Can Not Be Empty", Toast.LENGTH_SHORT).show();
                                        } else if (password.isEmpty()) {
                                            Toast.makeText(getContext(), "Password Can Not Be Empty", Toast.LENGTH_SHORT).show();
                                        } else if (height.isEmpty()) {
                                            Toast.makeText(getContext(), "Height Can Not Be Empty", Toast.LENGTH_SHORT).show();
                                        } else if (weight.isEmpty()) {
                                            Toast.makeText(getContext(), "Weight Can Not Be Empty", Toast.LENGTH_SHORT).show();
                                        }else{
                                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                                            // Create a map with updated data
                                            HashMap<String, Object> updatedUserData = new HashMap<>();
                                            updatedUserData.put("address", address);
                                            updatedUserData.put("mobile", mobile);
                                            updatedUserData.put("password", password);
                                            updatedUserData.put("height", height);
                                            updatedUserData.put("weight", weight);

                                            firestore.collection("user").document(String.valueOf(firstDocument.getId())).update(updatedUserData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(getContext(), "Details Updated", Toast.LENGTH_SHORT).show();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getContext(), "Details Updating Failed", Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                        }

                                    }
                                });

                            } else {
                                Toast.makeText(getContext(), "User Not Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Task Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}