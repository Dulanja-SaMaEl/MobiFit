package lk.graynode.mobifit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import lk.graynode.mobifit.admin.AdminMainActivity;
import lk.graynode.mobifit.admin.ui.admin_home.AdminHomeFragment;

public class AdminLogIn extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false); // Prevent dismissing it manually
        progressDialog.setMessage("Sign In...");

        TextView gotoUserSignIn = findViewById(R.id.gotoUserSignIn);
        gotoUserSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AdminLogIn.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        Button adminLoginBtn = findViewById(R.id.adminLoginBtn);
        adminLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();

                EditText adminEmail = findViewById(R.id.adminEmail);
                EditText adminPassword = findViewById(R.id.adminPassword);

                String email=adminEmail.getText().toString();
                String password=adminPassword.getText().toString();

                if (email.isEmpty()) {
                    Toast.makeText(AdminLogIn.this, "Email Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(AdminLogIn.this, "Password Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else {


                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    firestore.collection("admin")
                            .where(
                                    Filter.and(
                                            Filter.equalTo("email", email),
                                            Filter.equalTo("password", password)
                                    )
                            )
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    progressDialog.dismiss();
                                    List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                                    if (!documentSnapshotList.isEmpty()) {
                                        DocumentSnapshot documentSnapshot=documentSnapshotList.get(0);

                                        Toast.makeText(AdminLogIn.this, "LogIn Success", Toast.LENGTH_SHORT).show();
                                        SharedPreferences sharedPref = getSharedPreferences("lk.graynode.mobifit.admindata", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString("email", email);
                                        editor.putString("fname", documentSnapshot.getString("fname"));
                                        editor.putString("lname", documentSnapshot.getString("lname"));
                                        editor.apply();

                                        Intent i = new Intent(AdminLogIn.this, AdminMainActivity.class);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Toast.makeText(AdminLogIn.this, "User Not Found", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AdminLogIn.this, "Task Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }


        });
    }
}