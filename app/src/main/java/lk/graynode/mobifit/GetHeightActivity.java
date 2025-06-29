package lk.graynode.mobifit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class GetHeightActivity extends AppCompatActivity {

    private NumberPicker heightPicker;
    private MaterialButton continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get_height);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        heightPicker = findViewById(R.id.heightPicker);
        continueButton = findViewById(R.id.continueButton);

        // Setup NumberPicker
        setupHeightPicker();



        continueButton.setOnClickListener(v -> {
            int selectedHeight = heightPicker.getValue();

            Intent intent=new Intent(this, GetWeightActivity.class);
            intent.putExtra("uid",getIntent().getStringExtra("uid"));
            intent.putExtra("height",String.valueOf(selectedHeight));
            intent.putExtra("fname",getIntent().getStringExtra("fname"));
            intent.putExtra("lname",getIntent().getStringExtra("lname"));
            intent.putExtra("email",getIntent().getStringExtra("email"));

            startActivity(intent);
            finish();
        });
    }

    private void setupHeightPicker() {
        // Set NumberPicker properties
        heightPicker.setMinValue(15);  // Minimum height
        heightPicker.setMaxValue(260); // Maximum height
        heightPicker.setValue(150);     // Default selected height



        // Custom formatter to show only numbers
        heightPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.valueOf(value);
            }
        });
    }
}