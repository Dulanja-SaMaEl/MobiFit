package lk.graynode.mobifit.admin.ui.admin_schedules;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lk.graynode.mobifit.R;
import lk.graynode.mobifit.databinding.FragmentAddProductBinding;
import lk.graynode.mobifit.databinding.FragmentUpdateProductBinding;
import lk.graynode.mobifit.databinding.FragmentUpdateScheduleBinding;
import lk.graynode.mobifit.entity.ScheduleItem;


public class UpdateScheduleFragment extends Fragment {

    private FragmentUpdateScheduleBinding binding;

    private FirebaseFirestore firestore;

    EditText usTitle, usDescription, usDay;

    private Spinner spinner;
    private List<String> itemList;
    private ArrayAdapter<String> adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding=FragmentUpdateScheduleBinding.inflate(inflater,container,false);
        View root=binding.getRoot();

        firestore = FirebaseFirestore.getInstance();

        spinner = binding.spinner2;

        itemList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, itemList);
        spinner.setAdapter(adapter);

        loadDataFromFirestore();

        Button updateSchedule=binding.btnUpdateSchedule;
        updateSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                usTitle = binding.usTitle;
                usDescription = binding.usDescription;
                usDay = binding.usDay;

                String sid = binding.spinner2.getSelectedItem().toString();
                String title = usTitle.getText().toString();
                String description = usDescription.getText().toString();
                String day =usDay.getText().toString();

                if (sid.isEmpty()) {
                    Toast.makeText(getContext(), "Schedule ID Can Not Be Empty", Toast.LENGTH_SHORT).show();
                } else if (title.isEmpty()) {
                    Toast.makeText(getContext(), "Schedule Title Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Schedule Description Can Not Be Empty", Toast.LENGTH_SHORT).show();

                } else if (day.isEmpty()) {
                    Toast.makeText(getContext(), "Schedule Day Can Not Be Empty", Toast.LENGTH_SHORT).show();

                }else{

                    ScheduleItem scheduleItem = new ScheduleItem();
                    scheduleItem.setSchedule_id(sid);
                    scheduleItem.setTitle(title);
                    scheduleItem.setContent(description);
                    scheduleItem.setDay(Integer.parseInt(day));

                    updateSchedule(scheduleItem);
                }
            }
        });


        // Inflate the layout for this fragment
        return root;
    }

    private void updateSchedule(ScheduleItem scheduleItem) {
        firestore.collection("schedules").where(
                Filter.and(
                        Filter.equalTo("schedule_id",scheduleItem.getSchedule_id()),
                        Filter.equalTo("day",scheduleItem.getDay())
                )
        ).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if(!queryDocumentSnapshots.isEmpty()){
                    DocumentSnapshot documentSnapshot=queryDocumentSnapshots.getDocuments().get(0);
                    if(documentSnapshot.exists()){
                        // Create a map with updated data
                        HashMap<String, Object> scheduleUpdatedData = new HashMap<>();
                        scheduleUpdatedData.put("title", scheduleItem.getTitle());
                        scheduleUpdatedData.put("content", scheduleItem.getContent());


                        firestore.collection("schedules").document(documentSnapshot.getId()).update(scheduleUpdatedData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getContext(), "Schedule Updated Successfully", Toast.LENGTH_SHORT).show();
                                usTitle.setText("");
                                usDescription.setText("");
                                usDay.setText("");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Schedule Updating Failed", Toast.LENGTH_SHORT).show();

                            }
                        });

                    }else{
                        Toast.makeText(getContext(), "Schedule Not Available", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), "Schedule Not Available", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void loadDataFromFirestore() {
        firestore.collection("scheduletypes").orderBy("schedule_id", Query.Direction.ASCENDING) // Change this to your Firestore collection name
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        itemList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String id = document.getString("schedule_id"); // Change "name" to your field key
                            if (id != null) {
                                itemList.add(id);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Error getting data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}