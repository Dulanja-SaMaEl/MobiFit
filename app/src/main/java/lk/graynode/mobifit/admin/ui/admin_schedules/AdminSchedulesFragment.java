package lk.graynode.mobifit.admin.ui.admin_schedules;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

import lk.graynode.mobifit.admin.ui.admin_home.AddProductFragment;
import lk.graynode.mobifit.admin.ui.admin_home.DeleteProductFragment;
import lk.graynode.mobifit.admin.ui.admin_home.UpdateProductFragment;
import lk.graynode.mobifit.admin.ui.admin_home.VPAdapter;
import lk.graynode.mobifit.databinding.FragmentAdminSchedulesBinding;


public class AdminSchedulesFragment extends Fragment {

    private FragmentAdminSchedulesBinding binding;

    private FSAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAdminSchedulesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupViewPager();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupViewPager();
    }

    private void setupViewPager() {
        adapter = new FSAdapter(requireActivity().getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new AddScheduleFragment(), "ADD");
        adapter.addFragment(new UpdateScheduleFragment(), "UPDATE");
        adapter.addFragment(new DeleteScheduleFragment(), "DELETE");

        binding.viewpager1.setAdapter(adapter);
        binding.viewpager1.setOffscreenPageLimit(3); // Keep all three fragments alive for smooth switching
        binding.tabLayout1.setupWithViewPager(binding.viewpager1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}