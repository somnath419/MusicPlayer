package com.example.somnath.mymusic;

import android.app.ListActivity;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.example.somnath.mymusic.adapters.TabPagerAdapter;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,TabLayout.OnTabSelectedListener {

    //This is our tablayout
    private TabLayout tabLayout;
    private  Menu menu;
    private  NavigationView navigationView;

    //This is our viewPager
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Adding toolbar to the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //drawerlayout started
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        //Adding the tabs using addTab() method
        tabLayout.addTab(tabLayout.newTab().setText("Songs"));
        tabLayout.addTab(tabLayout.newTab().setText("Albums"));
        tabLayout.addTab(tabLayout.newTab().setText("Artists"));
        tabLayout.addTab(tabLayout.newTab().setText("Genres"));
        tabLayout.addTab(tabLayout.newTab().setText("Playlists"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        //Creating our pager adapter
        TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        //Adding adapter to pager
        viewPager.setAdapter(adapter);

        //Adding onTabSelectedListener to swipe views
        tabLayout.setOnTabSelectedListener(this);


         navigationView =(NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }






    @Override
    public void onResume() {
        super.onResume();

    }
    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);

        return true;
    }







    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_nowplaying) {

            Intent i = new Intent(this, NowPlayingActivity.class);
            startActivity(i);
            return true;


        } else if (id == R.id.nav_scanmedia) {

            Intent i = new Intent(this, ScanActivity.class);
            startActivity(i);
            return  true;

        } else if (id == R.id.nav_manage) {
            Intent i = new Intent(this, SettingActivity.class);
            startActivity(i);
            return  true;

        } else if (id == R.id.equalizer) {
            Intent i = new Intent(this, EqualizerActivity.class);
            startActivity(i);
            return true;


        } else if (id == R.id.nav_share) {


            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "It is here my app. Give it a try! " +
                    " \n http://www.somgemyadav.tk");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.sendto)));
            return true;


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;


    }

}
