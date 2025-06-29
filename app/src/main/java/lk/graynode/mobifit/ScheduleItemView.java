package lk.graynode.mobifit;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import lk.graynode.mobifit.databinding.FragmentBuyitemsBinding;
import lk.graynode.mobifit.entity.ScheduleItem;


public class ScheduleItemView extends AppCompatActivity {

    private FragmentBuyitemsBinding binding;
    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private ScheduleItemAdapter adapter;
    private List<ScheduleItem> scheduleItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule_item_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView scheduleTitle = findViewById(R.id.scheduleTitle);
        TextView scheduleDescription = findViewById(R.id.scheduleDescription);
        ImageView scheduleImg = findViewById(R.id.scheduleImg);

        scheduleTitle.setText(getIntent().getStringExtra("scTitle"));
        scheduleDescription.setText(getIntent().getStringExtra("scDes"));

        RequestOptions requestOptions = new RequestOptions()
                .timeout(60000); // 60 seconds

        Glide.with(ScheduleItemView.this)
                .load(getIntent().getStringExtra("url"))
                .apply(requestOptions)
                .into(scheduleImg);

        recyclerView = findViewById(R.id.scheduleRecylerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(ScheduleItemView.this));
        scheduleItems = new ArrayList<>();
        adapter = new ScheduleItemAdapter(scheduleItems);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        loadScheduleItems(getIntent().getStringExtra("scId"));
    }

    private void loadScheduleItems(String schedule_id) {
        firestore.collection("schedules").whereEqualTo("schedule_id", schedule_id).orderBy("day", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scheduleItems.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ScheduleItem item = document.toObject(ScheduleItem.class);
                        if (item != null) scheduleItems.add(item);
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                })
                .addOnFailureListener(e ->
//                        Toast.makeText(ScheduleItemView.this, String.valueOf(e), Toast.LENGTH_SHORT).show()
                                Log.e("FirestoreError", "Query failed", e)
                );

    }
}

class ScheduleItemAdapter extends RecyclerView.Adapter<lk.graynode.mobifit.ScheduleItemAdapter.ScheduleItemViewHolder> {
    private final List<ScheduleItem> scheduleItems;
    private List<ScheduleItem> mList;

    static class ScheduleItemViewHolder extends RecyclerView.ViewHolder {
        private TextView schedule_day;
        private RelativeLayout expandableLayout;
        private CardView linearLayout;
        private ImageView arrowImage;
        private TextView scheduleContent;


        public ScheduleItemViewHolder(@NonNull View itemView) {
            super(itemView);
            schedule_day = itemView.findViewById(R.id.schedule_day);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            arrowImage = itemView.findViewById(R.id.arrowImg);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            scheduleContent = itemView.findViewById(R.id.scheduleContent);

        }
    }

    public ScheduleItemAdapter(List<ScheduleItem> scheduleItems) {

        this.scheduleItems = scheduleItems;
        this.mList = scheduleItems;
    }

    @NonNull
    @Override
    public ScheduleItemAdapter.ScheduleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_item, parent, false);
        return new ScheduleItemAdapter.ScheduleItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleItemAdapter.ScheduleItemViewHolder holder, int position) {
        ScheduleItem scheduleItem = scheduleItems.get(position);

        ScheduleItem model = scheduleItems.get(position);

        holder.schedule_day.setText("Day " + scheduleItem.getDay());

        boolean isExpandable = model.isExpandable();
        holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

        if (isExpandable) {
            holder.arrowImage.setImageResource(R.drawable.arrow_upward);
        } else {
            holder.arrowImage.setImageResource(R.drawable.arrow_downward);
        }
        String workoutRoutine = scheduleItem.getContent();
        holder.scheduleContent.setText(replaceLiteralNewlines(workoutRoutine));



        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.setExpandable(!model.isExpandable());
                notifyItemChanged(holder.getAdapterPosition());

            }
        });


    }

    public String replaceLiteralNewlines(String inputText) {
        if (inputText != null) {
            return inputText.replace("\\n", "\n");
        }
        return "";
    }


    @Override
    public int getItemCount() {
        return scheduleItems.size();
    }
}


