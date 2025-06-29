package lk.graynode.mobifit;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import lk.graynode.mobifit.model.SQLiteHelper;

public class ProductItemView extends AppCompatActivity {

    int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_item_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView itemTitle = findViewById(R.id.itemTitle);
        TextView itemDescription = findViewById(R.id.itemDescription);
        TextView itemPrice = findViewById(R.id.itemPrice);
        ImageView itemImage = findViewById(R.id.itemImage);

        itemTitle.setText(getIntent().getStringExtra("ptitle"));
        itemDescription.setText(getIntent().getStringExtra("pdescription"));
        itemPrice.setText("Rs. " + getIntent().getStringExtra("price"));

        int maxQuantity = Integer.parseInt(getIntent().getStringExtra("qty"));
        Button plusButton = findViewById(R.id.btnLogout);
        Button minusButton = findViewById(R.id.btn_minus);
        TextView qtyText = findViewById(R.id.qty_text);

        qtyText.setText(String.valueOf(quantity));

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity < maxQuantity) {
                    quantity++;
                    qtyText.setText(String.valueOf(quantity));
                }
            }
        });

        minusButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                qtyText.setText(String.valueOf(quantity));
            }
        });

        RequestOptions requestOptions = new RequestOptions()
                .timeout(60000); // 60 seconds

        Glide.with(ProductItemView.this)
                .load(getIntent().getStringExtra("url"))
                .apply(requestOptions)
                .into(itemImage);


        Button addToCartBtn = findViewById(R.id.addToCartBtn);
        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Save To Cart

                SQLiteHelper sqLiteHelper = new SQLiteHelper(
                        ProductItemView.this,
                        "cart.db",
                        null,
                        1);

                SQLiteDatabase db = sqLiteHelper.getReadableDatabase(); // Get readable database


                String query = "SELECT * FROM products WHERE id = ?";
                Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(getIntent().getStringExtra("pid"))});

                if (cursor.moveToFirst()) { // If data exists
                    String qty = cursor.getString(cursor.getColumnIndexOrThrow("qty"));
                    TextView qtyText = findViewById(R.id.qty_text);
                    Log.i("stockCount",String.valueOf(Integer.parseInt(qty) ));
                    Log.i("stockCount",String.valueOf(Integer.parseInt(qtyText.getText().toString())));
                    Log.i("stockCount",String.valueOf(Integer.parseInt(qty) + Integer.parseInt(qtyText.getText().toString())));
                    if ((Integer.parseInt(qty) + Integer.parseInt(qtyText.getText().toString())) < maxQuantity) {
                        //Update Qty
                        SQLiteDatabase dbUpdate = sqLiteHelper.getWritableDatabase();

                        // Try to update the record first
                        ContentValues values = new ContentValues();
                        values.put("qty", String.valueOf(Integer.parseInt(qty) + Integer.parseInt(qtyText.getText().toString())));

                        int rowsAffected = db.update("products", values, "id=?", new String[]{String.valueOf(getIntent().getStringExtra("pid"))});

                        // If no rows were updated, insert a new record
                        if (rowsAffected != 0) {
                            Toast.makeText(ProductItemView.this, "Quantity Updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProductItemView.this, "Quantity Updating Failed", Toast.LENGTH_SHORT).show();
                        }
                        dbUpdate.close();
                        db.close();

                    } else {
                        //Cant Add Qty Exceeded
                        Toast.makeText(ProductItemView.this, "No Such Stock Select Less Quantity", Toast.LENGTH_SHORT).show();
                    }

                    db.close();
                } else {
                    //new product added to the cart
                    SQLiteDatabase dbInsert = sqLiteHelper.getWritableDatabase();

                    // Try to update the record first
                    ContentValues values = new ContentValues();

                    TextView qtyText = findViewById(R.id.qty_text);

                    Log.i("stockCount",qtyText.getText().toString());

                    values.put("id", getIntent().getStringExtra("pid"));
                    values.put("title", getIntent().getStringExtra("ptitle"));
                    values.put("description",getIntent().getStringExtra("pdescription"));
                    values.put("price", getIntent().getStringExtra("price"));
                    values.put("qty", qtyText.getText().toString());
                    values.put("url", getIntent().getStringExtra("url"));

                    long count = dbInsert.insert("products", null,values);

                    if (count == -1) {
                        Toast.makeText(ProductItemView.this, "Failed to insert data", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductItemView.this, "Data inserted successfully", Toast.LENGTH_SHORT).show();
                    }

                    dbInsert.close(); // Close the database
                }

            }
        });
    }
}