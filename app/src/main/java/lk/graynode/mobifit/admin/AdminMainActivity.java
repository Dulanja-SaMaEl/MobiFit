package lk.graynode.mobifit.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import lk.graynode.mobifit.R;
import lk.graynode.mobifit.databinding.ActivityAdminMainBinding;

public class AdminMainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityAdminMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set Toolbar as the ActionBar
        setSupportActionBar(binding.appBarAdminMain.toolbar);

        SharedPreferences sharedPref = getSharedPreferences("lk.graynode.mobifit.admindata", Context.MODE_PRIVATE);
        String fname = sharedPref.getString("fname", "");
        String lname = sharedPref.getString("lname", "");
        String email = sharedPref.getString("email", "");

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        View headerView=navigationView.getHeaderView(0);

        TextView headerName = headerView.findViewById(R.id.admin_nav_header_text1);  // Assuming you have this ID in your header layout
        TextView headerEmail = headerView.findViewById(R.id.admin_nav_header_text2);  // Assuming you have this ID in your header layout

        // Set the values to the TextViews
        headerName.setText(fname + " " + lname);  // Set the full name
        headerEmail.setText(email);  // You can also set the email here if needed

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.admin_nav_home, R.id.admin_nav_order, R.id.admin_nav_schedules)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_admin_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin_main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_admin_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}