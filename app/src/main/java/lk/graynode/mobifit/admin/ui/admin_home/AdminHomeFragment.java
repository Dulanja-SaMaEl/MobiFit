package lk.graynode.mobifit.admin.ui.admin_home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;


import lk.graynode.mobifit.MainActivity;
import lk.graynode.mobifit.databinding.FragmentAdminHomeBinding;



public class AdminHomeFragment extends Fragment {

    private FragmentAdminHomeBinding binding;
    private VPAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button adminLogout=binding.btnAdminLogout;

        SharedPreferences sharedPref = getContext().getSharedPreferences("lk.graynode.mobifit.admindata", Context.MODE_PRIVATE);

        adminLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear(); // Clears all stored preferences
                editor.apply(); // Apply changes asynchronously

                // Redirect to Login Activity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
                startActivity(intent);
                getActivity().finish();
            }
        });

        setupViewPager();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupViewPager();
    }

    private void setupViewPager() {
        adapter = new VPAdapter(requireActivity().getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new AddProductFragment(), "ADD");
        adapter.addFragment(new UpdateProductFragment(), "UPDATE");
        adapter.addFragment(new DeleteProductFragment(), "DELETE");

        binding.viewpager.setAdapter(adapter);
        binding.viewpager.setOffscreenPageLimit(3); // Keep all three fragments alive for smooth switching
        binding.tabLayout.setupWithViewPager(binding.viewpager);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }
}

