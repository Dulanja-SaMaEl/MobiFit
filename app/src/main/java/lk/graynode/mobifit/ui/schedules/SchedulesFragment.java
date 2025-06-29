package lk.graynode.mobifit.ui.schedules;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.graynode.mobifit.ProductItemView;
import lk.graynode.mobifit.R;
import lk.graynode.mobifit.ScheduleItemView;
import lk.graynode.mobifit.databinding.FragmentBuyitemsBinding;
import lk.graynode.mobifit.databinding.FragmentSchedulesBinding;
import lk.graynode.mobifit.entity.ProductItem;
import lk.graynode.mobifit.entity.ScheduleTypeItem;


public class SchedulesFragment extends Fragment {

    private @NonNull FragmentSchedulesBinding binding;
    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private ScheduleTypeItemAdapter adapter;
    private List<ScheduleTypeItem> scheduleTypeItems;

    private FragmentSchedulesBinding fragmentSchedulesBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = fragmentSchedulesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.scheduleTypesRecylclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleTypeItems = new ArrayList<>();
        adapter = new ScheduleTypeItemAdapter(scheduleTypeItems);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        loadScheduleTypes();

        return root;
    }

    private void loadScheduleTypes() {
        firestore.collection("scheduletypes").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scheduleTypeItems.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ScheduleTypeItem item = document.toObject(ScheduleTypeItem.class);
                        if (item != null) scheduleTypeItems.add(item);
                    }
                    requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Products Searching Failed", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

class ScheduleTypeItemAdapter extends RecyclerView.Adapter<ScheduleTypeItemAdapter.ScheduleTypeItemViewHolder> {
    private final List<ScheduleTypeItem> scheduleTypeItems;

    static class ScheduleTypeItemViewHolder extends RecyclerView.ViewHolder {
        TextView schedule_type_title;
        ImageView schedule_type_img;

        public ScheduleTypeItemViewHolder(@NonNull View itemView) {
            super(itemView);
            schedule_type_title = itemView.findViewById(R.id.schedule_type_title);
            schedule_type_img = itemView.findViewById(R.id.schedule_type_img);
        }
    }

    public ScheduleTypeItemAdapter(List<ScheduleTypeItem> scheduleTypeItems) {
        this.scheduleTypeItems = scheduleTypeItems;
    }

    @NonNull
    @Override
    public lk.graynode.mobifit.ui.schedules.ScheduleTypeItemAdapter.ScheduleTypeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_type_item, parent, false);
        return new lk.graynode.mobifit.ui.schedules.ScheduleTypeItemAdapter.ScheduleTypeItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleTypeItemAdapter.ScheduleTypeItemViewHolder holder, int position) {
        ScheduleTypeItem scheduleTypeItem = scheduleTypeItems.get(position);

        holder.schedule_type_title.setText(scheduleTypeItem.getName());


        Glide.with(holder.itemView.getContext())
                .load(scheduleTypeItem.getUrl())
                .apply(new RequestOptions().timeout(60000))
                .into(holder.schedule_type_img);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ScheduleItemView.class);
            intent.putExtra("scId", scheduleTypeItem.getSchedule_id());
            intent.putExtra("scTitle", scheduleTypeItem.getName());
            intent.putExtra("scDes", scheduleTypeItem.getDescription());
            intent.putExtra("url", scheduleTypeItem.getUrl());
            view.getContext().startActivity(intent);
        });
    }

    private String formatDescription(String description) {
        int maxLength = 50;
        return (description.length() > maxLength ? description.substring(0, maxLength) + "..." : description);
    }

    @Override
    public int getItemCount() {
        return scheduleTypeItems.size();
    }
}