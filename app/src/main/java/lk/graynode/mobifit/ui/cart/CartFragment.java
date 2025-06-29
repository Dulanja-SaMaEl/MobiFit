package lk.graynode.mobifit.ui.cart;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.graynode.mobifit.R;
import lk.graynode.mobifit.databinding.FragmentCartBinding;
import lk.graynode.mobifit.entity.Order;
import lk.graynode.mobifit.model.SQLiteHelper;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;

    private int PAYHERE_REQUEST = 9454;

    private  RecyclerView recyclerView1;

    FirebaseFirestore firestore;

    private TextView chkTotal;

    private String total, uid, fname, lname, uemail, mobile, address, new_order_id;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get SharedPreferences
        SharedPreferences sharedPref = getContext().getSharedPreferences("lk.graynode.mobifit.data", getContext().MODE_PRIVATE);
        String email = sharedPref.getString("email", null);

        firestore = FirebaseFirestore.getInstance();

        Button btnCheckout = binding.btnCheckout;
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                total = binding.cCheckoutTotal.getText().toString();
                chkTotal=binding.cCheckoutTotal;
                checkUserData(email);
            }
        });

        return root;
    }

    private void checkUserData(String email) {
        firestore.collection("user").whereEqualTo("email", email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    if (documentSnapshot.exists()) {
                        uid = documentSnapshot.getString("uid");
                        fname = documentSnapshot.getString("fname");
                        lname = documentSnapshot.getString("lname");
                        uemail = documentSnapshot.getString("email");
                        mobile = documentSnapshot.getString("mobile");
                        address = documentSnapshot.getString("address");

                        if (address==null && mobile==null) {
                            Toast.makeText(getContext(), "Please Update Your Mobile And Address", Toast.LENGTH_SHORT).show();
                        } else {
                            checkOrderData(fname, lname, email, mobile, address, total);
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "User Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "User Finding Failed ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkOrderData(String fname, String lname, String email, String mobile, String address, String total) {
        firestore.collection("orders").orderBy("order_id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot latestDoc = queryDocumentSnapshots.getDocuments().get(0);

                    String order_id = latestDoc.getString("order_id");
                    int newOrderId = Integer.parseInt(order_id) + 1;
                    new_order_id = String.valueOf(newOrderId);


                    String price = total;
                    Log.i("tot",total);
// First remove everything except digits
                    String numberOnly = price.replaceAll("[^0-9]", "");
// Then convert to double
                    Log.i("tot",numberOnly);
                    double totalPrice = Double.parseDouble(numberOnly);
                    Log.i("tot",String.valueOf(totalPrice));

                    String updatedMobile = "";

                    // Check if the number starts with '0' and replace it with '+94'
                    if (mobile.startsWith("0")) {
                        updatedMobile = "+94" + mobile.substring(1);
                    }

                    checkout(totalPrice, new_order_id);
                } else {
                    Toast.makeText(getContext(), "Order Not Found ", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Orders Finding Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

         recyclerView1 = getView().findViewById(R.id.cartRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView1.setLayoutManager(linearLayoutManager);

        TextView checkoutTotalView = getView().findViewById(R.id.cCheckoutTotal); // Ensure you have this TextView in your layout

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Toast.makeText(getContext(), "Swiped", Toast.LENGTH_SHORT).show();

                CartListAdapter.CartViewHolder holder = (CartListAdapter.CartViewHolder) viewHolder;

                SQLiteHelper sqLiteHelper = new SQLiteHelper(
                        viewHolder.itemView.getContext(),
                        "cart.db",
                        null,
                        1);


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();
                        int row = sqLiteDatabase.delete("products", "`id`=?", new String[]{holder.id});

                        // Query the updated data
                        SQLiteDatabase readableDb = sqLiteHelper.getReadableDatabase();
                        Cursor newCursor = readableDb.query(
                                "products",
                                null,
                                null,
                                null,
                                null,
                                null,
                                "`id` DESC"
                        );

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CartListAdapter adapter = (CartListAdapter) recyclerView1.getAdapter();
                                adapter.updateCursor(newCursor, checkoutTotalView);
                            }
                        });
                    }
                }).start();
            }

        });

        itemTouchHelper.attachToRecyclerView(recyclerView1);

        SQLiteHelper sqLiteHelper = new SQLiteHelper(
                getContext(),
                "cart.db",
                null,
                1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query(
                        "products",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "`id` DESC"
                );

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CartListAdapter cartListAdapter = new CartListAdapter(cursor, checkoutTotalView);
                        recyclerView1.setAdapter(cartListAdapter);
                    }
                });
            }
        }).start();
    }

    public void checkout(double total, String order_id) {

        Log.i("checkoutLog", fname);
        Log.i("checkoutLog", lname);
        Log.i("checkoutLog", uemail);
        Log.i("checkoutLog", mobile);
        Log.i("checkoutLog", address);
        Log.i("checkoutLog", String.valueOf(total));
        Log.i("checkoutLog", order_id);

        InitRequest req = new InitRequest();
        req.setMerchantId("1221698");       // Merchant ID
        req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
        req.setAmount(total);             // Final Amount to be charged
        req.setOrderId(order_id);        // Unique Reference ID
        req.setItemsDescription("Order");  // Item description title
        req.setCustom1("This is the custom message 1");
        req.setCustom2("This is the custom message 2");
        req.getCustomer().setFirstName(fname);
        req.getCustomer().setLastName(lname);
        req.getCustomer().setEmail(uemail);
        req.getCustomer().setPhone(mobile);
        req.getCustomer().getAddress().setAddress(address);
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

//Optional Params
        req.setNotifyUrl("");           // Notifiy Url
        req.getCustomer().getDeliveryAddress().setAddress(address);
        req.getCustomer().getDeliveryAddress().setCity("Kadawatha");
        req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");
        req.getItems().add(new Item(null, "Door bell wireless", 1, total));

        Intent intent = new Intent(getContext(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST); //unique request ID e.g. "11001"
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK) {
                String msg;
                if (response != null)
                    if (response.isSuccess()) {
                        msg = "Activity result:" + response.getData().toString();
                        saveOrder();
                    } else {
                        msg = "Result:" + response.toString();
                    }
                else
                    msg = "Result: no response";
                Log.i("gatewayLog", msg);
//                textView.setText(msg);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response != null)
//                    textView.setText(response.toString());
                    Log.i("gatewayLog", response.toString());
                else {
                    Toast.makeText(getContext(), "User canceled the request", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveOrder() {

        Map<String, Object> order = new HashMap<>();
        order.put("order_id", new_order_id);
        order.put("uid",uid);
        order.put("location", "7.476631146968194, 80.34998749479706");

        firestore.collection("orders").add(order).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                Toast.makeText(requireContext(), "Order Saved", Toast.LENGTH_SHORT).show();
                saveOrderItems();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(), "Order Saving Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOrderItems() {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(
                requireContext(),  // Using requireContext() instead of getContext()
                "cart.db",
                null,
                1);

        SQLiteDatabase readableDatabase = null;
        Cursor cursor = null;

        try {
            readableDatabase = sqLiteHelper.getReadableDatabase();
            cursor = readableDatabase.rawQuery("SELECT * FROM products", null);

            // Create a list to store all items
            List<Map<String, Object>> orderItems = new ArrayList<>();

            while (cursor.moveToNext()) {
                String pid = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String qty = cursor.getString(cursor.getColumnIndexOrThrow("qty"));

                Map<String, Object> item = new HashMap<>();
                item.put("pid", pid);
                item.put("qty", qty);
                orderItems.add(item);
            }

            // Now process all items
            processOrderItems(orderItems, sqLiteHelper);

        } catch (Exception e) {
            Log.e("SaveOrderItems", "Error: " + e.getMessage());
            Toast.makeText(requireContext(), "Error saving order items", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
            if (readableDatabase != null) readableDatabase.close();
        }
    }

    private void processOrderItems(List<Map<String, Object>> items, SQLiteHelper sqLiteHelper) {
        // First get the latest order item ID
        firestore.collection("order_item")
                .orderBy("order_item_id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int startingId;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot latestDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String order_item_id = latestDoc.getString("order_item_id");
                        startingId = Integer.parseInt(order_item_id);
                    } else {
                        startingId = 0;
                    }

                    // Create a batch for all items
                    WriteBatch batch = firestore.batch();

                    // Add each item to the batch
                    for (int i = 0; i < items.size(); i++) {
                        Map<String, Object> item = items.get(i);

                        DocumentReference newDocRef = firestore.collection("order_item").document();

                        Map<String, Object> orderItem = new HashMap<>(item);
                        orderItem.put("order_id", new_order_id);
                        orderItem.put("order_item_id", String.valueOf(startingId + i + 1));

                        batch.set(newDocRef, orderItem);
                    }

                    // Commit the batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Order Items Saved Successfully", Toast.LENGTH_SHORT).show();
                                // Clear the cart after successful save
                                clearCart(sqLiteHelper);
                                // Refresh the UI
                                refreshRecyclerView();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to save order items", Toast.LENGTH_SHORT).show();
                                Log.e("Firestore", "Error saving batch: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to get latest order item ID", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error getting latest ID: " + e.getMessage());
                });
    }

    private void clearCart(SQLiteHelper sqLiteHelper) {
        try {
            SQLiteDatabase database = sqLiteHelper.getWritableDatabase();
            database.beginTransaction();
            database.delete("products", null, null);
            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
        } catch (Exception e) {
            Log.e("ClearCart", "Error: " + e.getMessage());
        }
    }

    private void refreshRecyclerView() {
        if (isAdded() && getActivity() != null) {  // Check if fragment is still attached
            getActivity().runOnUiThread(() -> {
                try {
                    SQLiteHelper sqLiteHelper = new SQLiteHelper(
                            requireContext(),
                            "cart.db",
                            null,
                            1);
                    SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
                    Cursor newCursor = db.rawQuery("SELECT * FROM products", null);

                    if (recyclerView1 != null && recyclerView1.getAdapter() != null) {
                        ((CartListAdapter) recyclerView1.getAdapter()).updateCursor(newCursor, chkTotal);
                        chkTotal.setText("Rs .");
                    }
                } catch (Exception e) {
                    Log.e("RefreshUI", "Error: " + e.getMessage());
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.CartViewHolder> {

    int checkoutTotal;

    private TextView checkoutTotalView;
    Cursor cursor;

    public CartListAdapter(Cursor cursor, TextView checkoutTotalView) {
        this.cursor = cursor;
        this.checkoutTotalView = checkoutTotalView;
        this.checkoutTotal = 0; // Reset total
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView ctitle;
        TextView cdetails;
        TextView cCheckoutTotal;

        TextView ctotal;

        ImageView cItemImage;

        String id;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ctitle = itemView.findViewById(R.id.cItem_title);
            cdetails = itemView.findViewById(R.id.cItem_price_qty);
            ctotal = itemView.findViewById(R.id.cItem_tot);
            cItemImage = itemView.findViewById(R.id.cItemImage);
            cCheckoutTotal = itemView.findViewById(R.id.cCheckoutTotal);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.cart_item, parent, false);
        CartViewHolder cartViewHolder = new CartViewHolder(view);
        return cartViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        cursor.moveToPosition(position);

        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        String qty = cursor.getString(cursor.getColumnIndexOrThrow("qty"));
        String price = cursor.getString(cursor.getColumnIndexOrThrow("price"));
        String url = cursor.getString(cursor.getColumnIndexOrThrow("url"));


        holder.ctitle.setText(title);
        holder.id = cursor.getString(cursor.getColumnIndexOrThrow("id"));

        int total = Integer.parseInt(qty) * Integer.parseInt(price);
        checkoutTotal += total;

        Glide.with(holder.itemView.getContext())
                .load(url)
                .apply(new RequestOptions().timeout(60000))
                .into(holder.cItemImage);

        holder.cdetails.setText("Rs. " + price + " x " + qty);
        holder.ctotal.setText("Rs. " + String.valueOf(total));

        // Update the checkout total TextView
        if (checkoutTotalView != null) {
            checkoutTotalView.setText("Total: Rs. " + checkoutTotal);
        }

    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void removeItem(int position) {
        notifyItemRemoved(position);
    }

    public void updateCursor(Cursor newCursor, TextView checkoutTotalView) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        this.checkoutTotalView = checkoutTotalView;
        this.checkoutTotal = 0; // Reset total
        notifyDataSetChanged();
    }

}