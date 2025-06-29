package lk.graynode.mobifit.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.graynode.mobifit.ProductItemView;
import lk.graynode.mobifit.R;
import lk.graynode.mobifit.databinding.FragmentHomeBinding;
import lk.graynode.mobifit.entity.ProductItem;
import lk.graynode.mobifit.entity.StepData;
import lk.graynode.mobifit.model.BMI;
import lk.graynode.mobifit.model.StepTracker;


public class HomeFragment extends Fragment implements StepTracker.StepDataCallback {

    private FragmentHomeBinding binding;
    private BarChart barChart;
    private StepTracker stepTracker;

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private FeaturedProductItemAdapter adapter;
    private List<ProductItem> productItems;

    private TextView stepCount, calCount, avgCal, name,bmiCount;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        barChart = root.findViewById(R.id.barChart1);
        bmiCount = root.findViewById(R.id.bmiCount);
        name = root.findViewById(R.id.admin_name);
        stepCount = root.findViewById(R.id.stepsCount);
        calCount = root.findViewById(R.id.calCount);
        avgCal = root.findViewById(R.id.avgCal);
        stepTracker = new StepTracker(requireContext(), this);

        // Get SharedPreferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("lk.graynode.mobifit.data", getContext().MODE_PRIVATE);
        String email = sharedPref.getString("email", null);

        // Check if user is already logged in
        if (email != null) {
            name.setText(sharedPref.getString("fname",null)+" "+sharedPref.getString("lname",null));
        }

        setupBarChart();
        stepTracker.startTracking();

        recyclerView = root.findViewById(R.id.featuredProductRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        productItems = new ArrayList<>();
        adapter = new FeaturedProductItemAdapter(productItems);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        loadProducts();

        calculateBMI(email);

        return root;
    }

    private void calculateBMI(String email) {
       firestore.collection("user").whereEqualTo("email",email).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
           @Override
           public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
               DocumentSnapshot documentSnapshot=queryDocumentSnapshots.getDocuments().get(0);
               if(!documentSnapshot.exists()){
                   Toast.makeText(getContext(), "User Not Exists", Toast.LENGTH_SHORT).show();
               }else{

                   if(documentSnapshot.getString("weight")!=null && documentSnapshot.getString("height")!=null){
                       double weight=Double.parseDouble(documentSnapshot.getString("weight"));
                       double height=Double.parseDouble(documentSnapshot.getString("height"));
                       BMI bmi=new BMI(weight,height);

                       double v = bmi.calculateBMI();

                       requireActivity().runOnUiThread(() -> {
                           bmiCount.setText(String.format(Locale.getDefault(), "%.2f",v)+ " Kg/m²");
                       });
                   }
               }
           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               Toast.makeText(getContext(), "User Searching Failed", Toast.LENGTH_SHORT).show();
           }
       });
    }

    private void loadProducts() {
        firestore.collection("product").orderBy("pid").limit(4).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productItems.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ProductItem item = document.toObject(ProductItem.class);
                        if (item != null) productItems.add(item);
                    }
                    requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Products Searching Failed", Toast.LENGTH_SHORT).show());
    }

    private void setupBarChart() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<StepData> weeklyData = stepTracker.getWeeklyData();

        StepData data = getTodayStepData(weeklyData);
        StepData avgData = getWeeklyAverage(weeklyData);

        if (avgData != null) {
            avgCal.setText(String.valueOf(avgData.getCaloriesBurned()) + " KCAL");
        } else {
            Log.e("SetupBarChart", "avg Data is null");
        }

        if (data != null) {
            stepCount.setText(String.valueOf(stepTracker.getCurrentSteps()));
            calCount.setText(String.format(Locale.getDefault(), "%.2f", stepTracker.getCurrentCalories())+ " KCAL");
        } else {
            Log.e("SetupBarChart", "StepData is null");
        }

        for (int i = 0; i < weeklyData.size(); i++) {
            barEntries.add(new BarEntry(i, weeklyData.get(i).getCaloriesBurned()));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Calories Burned");
        barDataSet.setColor(Color.parseColor("#C3D92C"));
        barDataSet.setValueTextColor(Color.WHITE);

        try {
            Typeface poppins = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular);
            if (poppins != null) {
                barDataSet.setValueTypeface(poppins);
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error loading font: " + e.getMessage());
            barDataSet.setValueTypeface(Typeface.DEFAULT);
        }

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);

        // Customize X-Axis with actual dates
        XAxis xAxis = barChart.getXAxis();
        String[] dates = getDayLabels(weeklyData);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);

        barChart.invalidate();
    }

    public StepData getTodayStepData(ArrayList<StepData> weeklyData) {
        // Get today's date in the same format as your StepData class
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Search for today's data
        for (StepData data : weeklyData) {
            if (data.getDate().equals(todayDate)) {
                return data; // Return today's step data
            }
        }

        // If no data is found for today, return default values
        return new StepData(0, 0.0f, todayDate);
    }

    private String[] getDayLabels(ArrayList<StepData> weeklyData) {
        String[] labels = new String[weeklyData.size()];
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        for (int i = 0; i < weeklyData.size(); i++) {
            try {
                Date date = inputFormat.parse(weeklyData.get(i).getDate());
                labels[i] = outputFormat.format(date).toUpperCase();
            } catch (ParseException e) {
                labels[i] = "DAY" + i;
            }
        }
        return labels;
    }

    @Override
    public void onStepDataUpdated(StepData stepData) {
        // Update the chart when new step data is available
        setupBarChart();
    }

    public static StepData getWeeklyAverage(ArrayList<StepData> weeklyData) {
        if (weeklyData == null || weeklyData.isEmpty()) {
            return new StepData(0, 0.0f, "Average"); // Default if no data
        }

        int totalSteps = 0;
        float totalCalories = 0.0f;
        int count = 0;

        for (StepData data : weeklyData) {
            totalSteps += data.getSteps();
            totalCalories += data.getCaloriesBurned();
            count++;
        }

        // Calculate the average
        int avgSteps = count > 0 ? totalSteps / count : 0;
        float avgCalories = count > 0 ? totalCalories / count : 0.0f;

        return new StepData(avgSteps, avgCalories, "Weekly Average");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
class FeaturedProductItemAdapter extends RecyclerView.Adapter<lk.graynode.mobifit.ui.home.FeaturedProductItemAdapter.FeaturedProductItemViewHolder> {
    private final List<ProductItem> productItems;

    static class FeaturedProductItemViewHolder extends RecyclerView.ViewHolder {
        TextView pItemTitle, pItemPrice;
        ImageView pItemImage;

        Button pItemButton;

        public FeaturedProductItemViewHolder(@NonNull View itemView) {
            super(itemView);
            pItemTitle = itemView.findViewById(R.id.fpItem_title);
            pItemPrice = itemView.findViewById(R.id.fpItem_price);
            pItemImage = itemView.findViewById(R.id.fpItemImage);
            pItemButton = itemView.findViewById(R.id.fpItemButton);
        }
    }

    public FeaturedProductItemAdapter(List<ProductItem> productItems) {
        this.productItems = productItems;
    }

    @NonNull
    @Override
    public lk.graynode.mobifit.ui.home.FeaturedProductItemAdapter.FeaturedProductItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.featured_product_item, parent, false);
        return new lk.graynode.mobifit.ui.home.FeaturedProductItemAdapter.FeaturedProductItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull lk.graynode.mobifit.ui.home.FeaturedProductItemAdapter.FeaturedProductItemViewHolder holder, int position) {
        ProductItem productItem = productItems.get(position);

        holder.pItemTitle.setText(formatTitle(productItem.getPtitle()));
        holder.pItemPrice.setText("Rs." + productItem.getPrice());

        Glide.with(holder.itemView.getContext())
                .load(productItem.getUrl())
                .apply(new RequestOptions().timeout(60000))
                .into(holder.pItemImage);

        holder.pItemButton.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ProductItemView.class);
            intent.putExtra("pid", productItem.getPid());
            intent.putExtra("ptitle", productItem.getPtitle());
            intent.putExtra("pdescription", productItem.getPdescription());
            intent.putExtra("price", productItem.getPrice());
            intent.putExtra("qty", productItem.getQty());
            intent.putExtra("url", productItem.getUrl());
            view.getContext().startActivity(intent);
        });
    }

    private String formatTitle(String description) {
        int maxLength = 20;
        return (description.length() > maxLength ? description.substring(0, maxLength) + "..." : description);
    }

    @Override
    public int getItemCount() {
        return productItems.size();
    }
}
