package lk.graynode.mobifit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import android.Manifest;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.graynode.mobifit.admin.AdminMainActivity;
import lk.graynode.mobifit.model.StepTrackerService;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;

    EditText globalEmail;

    private RequestQueue requestQueue;
    private static final String SERVLET_URL = "https://943c-2402-4000-2100-b9d1-40ad-54f8-4eae-3b28.ngrok-free.app/MobiFit/ForgetPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);


        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 🛑 Moved requestPermissions() before checking if permissions are granted
        requestPermissions();

        if (checkPermissionsGranted()) {
            Intent serviceIntent = new Intent(this, StepTrackerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }

        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false); // Prevent dismissing it manually
        progressDialog.setMessage("Sign In...");

        // Get SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("lk.graynode.mobifit.data", MODE_PRIVATE);
        String email = sharedPref.getString("email", null);

        // Get SharedPreferences
        SharedPreferences sharedPref1 = getSharedPreferences("lk.graynode.mobifit.admindata", MODE_PRIVATE);
        String adminEmail = sharedPref1.getString("email", null);

        firestore=FirebaseFirestore.getInstance();

        // Check if user is already logged in
        if (email != null) {
            // User is logged in, go to HomeActivity
            Intent intent = new Intent(MainActivity.this, Home.class);
            startActivity(intent);
            finish(); // Prevent returning to MainActivity
        }

        // Check if user is already logged in
        if (adminEmail != null) {
            // User is logged in, go to HomeActivity
            Intent intent = new Intent(MainActivity.this, AdminMainActivity.class);
            startActivity(intent);
            finish(); // Prevent returning to MainActivity
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView gotoSignUp = findViewById(R.id.gotoSignUp);
        gotoSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SignUp.class);
                startActivity(i);
            }
        });

        TextView gotoAdminLogin = findViewById(R.id.gotoAdminLogin);
        gotoAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AdminLogIn.class);
                startActivity(i);
            }
        });

        requestQueue = Volley.newRequestQueue(this);

        TextView forgetPassword = findViewById(R.id.forgetPassword);
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globalEmail = findViewById(R.id.lEmail);

                String globalemail = globalEmail.getText().toString();

                if (globalemail.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Email Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else {
                    fetchUniqueValue(globalemail);
                }
            }
        });

        Button userLoginBtn = findViewById(R.id.userLoginBtn);
        userLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();

                EditText lEmail = findViewById(R.id.lEmail);
                EditText lPassword = findViewById(R.id.lPassword);

                String email = lEmail.getText().toString();
                String password = lPassword.getText().toString();


                if (email.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Email Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Password Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else {

                    firestore = FirebaseFirestore.getInstance();

                    firestore.collection("user")
                            .where(
                                    Filter.and(
                                            Filter.equalTo("email", email),
                                            Filter.equalTo("password", password)
                                    )
                            )
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    progressDialog.dismiss(); // Hide loading when done
                                    List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                                    if (!documentSnapshotList.isEmpty()) {

                                        DocumentSnapshot firstDocument = documentSnapshotList.get(0);

                                        String fname = firstDocument.getString("fname");
                                        String lname = firstDocument.getString("lname");


                                        Toast.makeText(MainActivity.this, "LogIn Success", Toast.LENGTH_SHORT).show();

                                        if (firstDocument.getString("height")!=null && firstDocument.getString("weight")!=null) {

                                            SharedPreferences sharedPref = getSharedPreferences("lk.graynode.mobifit.data", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putString("email", email);
                                            editor.putString("fname", fname);
                                            editor.putString("lname", lname);
                                            editor.apply();



                                            Intent i = new Intent(MainActivity.this, Home.class);
                                            startActivity(i);
                                            finish();
                                        } else {
                                            Intent i = new Intent(MainActivity.this, GetHeightActivity.class);
                                            i.putExtra("uid", firstDocument.getString("uid"));
                                            i.putExtra("fname", fname);
                                            i.putExtra("lname", lname);
                                            i.putExtra("email", email);
                                            startActivity(i);
                                            finish();
                                        }

                                    } else {
                                        Toast.makeText(MainActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Task Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }


        });
    }

    private void fetchUniqueValue(String email) {
        // Create POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVLET_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        updateNewPassword(response, email);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("errorrrr", "Error sending email: " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        requestQueue.add(stringRequest);

    }

    private void updateNewPassword(String response, String email) {
        firestore.collection("user")
                .whereEqualTo("email", email)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            HashMap<String, Object> updatedUserData = new HashMap<>();
                            updatedUserData.put("password", response);

                            firestore.collection("user").document(documentSnapshot.getId()).update(updatedUserData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(MainActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Password Updating Failed", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }else{
                            Toast.makeText(MainActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "User Finding Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (Needs Post Notifications + Activity Recognition)
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.ACTIVITY_RECOGNITION
                }, PERMISSION_REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 (Needs only Activity Recognition)
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PERMISSION_REQUEST_CODE);
            }
        } else {
            // Android 9 and below (No need for explicit permissions for step tracking)
            Log.d("Permissions", "No special permissions needed for this Android version.");
        }
    }

    private boolean checkPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("Permissions", "onRequestPermissionsResult called with requestCode: " + requestCode);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Permissions granted!");

                Intent serviceIntent = new Intent(this, StepTrackerService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            } else {
                Log.d("Permissions", "Permissions denied!");
                Toast.makeText(this, "Permissions are required to track steps", Toast.LENGTH_LONG).show();
            }
        }
    }

}