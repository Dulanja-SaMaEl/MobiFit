package lk.graynode.mobifit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import lk.graynode.mobifit.entity.User;
import lk.graynode.mobifit.model.Validations;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView signUpBackBtn = findViewById(R.id.signUpBackBtn);
        signUpBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignUp.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        TextView gotoSingIn = findViewById(R.id.gotoSingIn);
        gotoSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignUp.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        Button signUpBtn = findViewById(R.id.SignUpBtn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText firstName = findViewById(R.id.firstName);
                EditText lastName = findViewById(R.id.lastName);
                EditText email = findViewById(R.id.email);
                EditText password = findViewById(R.id.password);
                EditText cPassword = findViewById(R.id.cPassword);

                String fname = firstName.getText().toString();
                String lname = lastName.getText().toString();
                String e = email.getText().toString();
                String pass = password.getText().toString();
                String cpass = cPassword.getText().toString();

                if (fname.isEmpty()) {
                    Toast.makeText(SignUp.this, "First Name Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (lname.isEmpty()) {
                    Toast.makeText(SignUp.this, "Last Name Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (e.isEmpty()) {
                    Toast.makeText(SignUp.this, "Email Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (!Validations.isPasswordValid(pass)) {
                    Toast.makeText(SignUp.this, "Password must includes at least one uppercase letter, number, special character.", Toast.LENGTH_SHORT).show();
                } else if (pass.isEmpty()) {
                    Toast.makeText(SignUp.this, "Password Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (cpass.isEmpty()) {
                    Toast.makeText(SignUp.this, "Confirm Password Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (!pass.equals(cpass)) {
                    Toast.makeText(SignUp.this, "Password And Confirm Password Is Not Matching", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    User user = new User();
                    user.setFname(fname);
                    user.setLname(lname);
                    user.setEmail(e);
                    user.setPassword(pass);

                    firestore.collection("user").orderBy("uid", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot latestDoc = queryDocumentSnapshots.getDocuments().get(0);
                                User user1=latestDoc.toObject(User.class);

                                int newUid=Integer.parseInt(user1.getUid())+1;
                                user.setUid(String.valueOf(newUid));

                                firestore.collection("user")
                                        .whereEqualTo("email", e)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                                                if (!documentSnapshotList.isEmpty()) {
                                                    Toast.makeText(SignUp.this, "User Already Exists", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    firestore.collection("user").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Toast.makeText(SignUp.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                                            firstName.setText("");
                                                            lastName.setText("");
                                                            email.setText("");
                                                            password.setText("");
                                                            cPassword.setText("");

                                                            Intent i = new Intent(SignUp.this, MainActivity.class);
                                                            startActivity(i);
                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(SignUp.this, "Task Failed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SignUp.this, "Task Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Log.d("Firestore", "No documents found");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


                }
            }
        });
    }
}