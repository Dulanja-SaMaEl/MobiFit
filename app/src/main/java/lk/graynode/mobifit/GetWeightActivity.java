package lk.graynode.mobifit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class GetWeightActivity extends AppCompatActivity {

    private NumberPicker weightPicker;
    private MaterialButton continueButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get_weight);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        weightPicker = findViewById(R.id.weightPicker);
        continueButton = findViewById(R.id.continueButton);

        // Setup NumberPicker
        setupWeightPicker();



        continueButton.setOnClickListener(v -> {
            int selectedWeight = weightPicker.getValue();

            String uid=getIntent().getStringExtra("uid");
            String fname=getIntent().getStringExtra("fname");
            String lname=getIntent().getStringExtra("lname");
            String email=getIntent().getStringExtra("email");
            String height=getIntent().getStringExtra("height");
            String weight=String.valueOf(selectedWeight);

            saveDataToFirestore(height,weight,email);

            SharedPreferences sharedPref = getSharedPreferences("lk.graynode.mobifit.data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("email", email);
            editor.putString("fname", fname);
            editor.putString("lname", lname);
            editor.apply();

            Intent intent=new Intent(GetWeightActivity.this, Home.class);
            startActivity(intent);
            finish();
        });
    }

    private void saveDataToFirestore(String height,String weight,String email) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();


        firestore.collection("user")
                .whereEqualTo("email", email)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        DocumentSnapshot documentSnapshot=queryDocumentSnapshots.getDocuments().get(0);

                        if(!documentSnapshot.exists()){
                            Toast.makeText(GetWeightActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                        }else{
                            // Create a map with updated data
                            HashMap<String, Object> updatedUserData = new HashMap<>();
                            updatedUserData.put("height", height);
                            updatedUserData.put("weight", weight);

                            firestore.collection("user").document(String.valueOf(documentSnapshot.getId())).update(updatedUserData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(GetWeightActivity.this, "Details Updated", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(GetWeightActivity.this, "Details Updating Failed", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GetWeightActivity.this, "User Finding Failed", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void setupWeightPicker() {
        // Set NumberPicker properties
        weightPicker.setMinValue(15);  // Minimum height
        weightPicker.setMaxValue(700); // Maximum height
        weightPicker.setValue(50);     // Default selected height



        // Custom formatter to show only numbers
        weightPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.valueOf(value);
            }
        });
    }
}