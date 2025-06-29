package lk.graynode.mobifit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import lk.graynode.mobifit.databinding.ActivityHomeBinding;

public class Home extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);


        SharedPreferences sharedPref = getSharedPreferences("lk.graynode.mobifit.data", Context.MODE_PRIVATE);
        String fname = sharedPref.getString("fname", "");
        String lname = sharedPref.getString("lname", "");
        String email = sharedPref.getString("email", "");

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        View headerView=navigationView.getHeaderView(0);

        TextView headerName = headerView.findViewById(R.id.nav_header_text1);  // Assuming you have this ID in your header layout
        TextView headerEmail = headerView.findViewById(R.id.nav_header_text2);  // Assuming you have this ID in your header layout

        // Set the values to the TextViews
        headerName.setText(fname + " " + lname);  // Set the full name
        headerEmail.setText(email);  // You can also set the email here if needed

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.admin_nav_home, R.id.nav_schedules, R.id.nav_buyitems,R.id.nav_trackorders,R.id.nav_user,R.id.nav_cart)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}