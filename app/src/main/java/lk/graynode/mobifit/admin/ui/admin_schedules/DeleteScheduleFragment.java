package lk.graynode.mobifit.admin.ui.admin_schedules;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import lk.graynode.mobifit.R;
import lk.graynode.mobifit.databinding.FragmentDeleteProductBinding;
import lk.graynode.mobifit.databinding.FragmentDeleteScheduleBinding;
import lk.graynode.mobifit.entity.ScheduleItem;


public class DeleteScheduleFragment extends Fragment {

    private FragmentDeleteScheduleBinding binding;

    private FirebaseFirestore firestore;

    EditText dsDay;

    private Spinner spinner;
    private List<String> itemList;
    private ArrayAdapter<String> adapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDeleteScheduleBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        firestore = FirebaseFirestore.getInstance();

        spinner = binding.spinner3;

        itemList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, itemList);
        spinner.setAdapter(adapter);

        loadDataFromFirestore();

        Button btnDeleteSchedule=binding.btnDeleteSchedule;
        btnDeleteSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dsDay = binding.dsDay;

                String dsday=dsDay.getText().toString();
                String dsid = binding.spinner3.getSelectedItem().toString();

                if(dsday.isEmpty()){
                    Toast.makeText(getContext(), "Schedule Id Can Not Be Empty", Toast.LENGTH_SHORT).show();
                }else{
                    deleteSchedule(dsday,dsid);
                }
            }
        });

        // Inflate the layout for this fragment
        return root;
    }

    private void deleteSchedule(String dsday,String dsid) {

        Log.i("dataaa",dsday);
        Log.i("dataaa",dsid);

        firestore.collection("schedules")
                .where(
                        Filter.and(
                                Filter.equalTo("schedule_id", dsid),
                                Filter.equalTo("day",Integer.parseInt(dsday))
                        )
                ) // Find document by pid
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            document.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), "Schedule Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    dsDay.setText("");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Schedule Deleting Failed", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), "Schedule Not Found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error Finding Schedule", Toast.LENGTH_SHORT).show()
                );
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