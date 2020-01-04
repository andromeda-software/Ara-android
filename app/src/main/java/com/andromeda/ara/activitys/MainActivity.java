/*
 * Copyright (c) 2020. Fulton Browne
 *  This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.andromeda.ara.activitys;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.andromeda.ara.R;
import com.andromeda.ara.constants.DrawerModeConstants;
import com.andromeda.ara.constants.ServerUrl;
import com.andromeda.ara.constants.User;
import com.andromeda.ara.feeds.Drawer;
import com.andromeda.ara.feeds.Rss;
import com.andromeda.ara.search.Search;
import com.andromeda.ara.util.AraPopUps;
import com.andromeda.ara.util.*;
import com.andromeda.ara.voice.VoiceMain;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.auth.Auth;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.data.Data;
import com.microsoft.appcenter.data.DefaultPartitions;
import com.microsoft.appcenter.data.models.DocumentWrapper;
import com.microsoft.appcenter.data.models.PaginatedDocuments;
import com.microsoft.appcenter.push.Push;
import com.microsoft.appcenter.utils.async.AppCenterConsumer;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    /**
     * these have to do with permissions
     **/
    private final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_RECORD_AUDIO = 13;
    //this is the text for the greeting it is hello by default for compatibility reasons
    private String mTime = "hello";
    //this is the navigation Drawer
    private com.mikepenz.materialdrawer.Drawer drawer = null;
    //Adapter
    private RecyclerView.Adapter mAdapter;
    // Data set for list out put
    private List<RssFeedModel> rssFeedModel1 = new ArrayList<>();
    //RecyclerView
    private RecyclerView recyclerView;
    //Device screen width
    private int screenWidth;
    private long mode = 0;
    SharedPreferences mPrefs;
    String mEmail;
    String mName;
    Activity act;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Read Contacts permission");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Please enable access to contacts.");
                    builder.setOnDismissListener(dialog -> requestPermissions(
                            new String[]
                                    {Manifest.permission.READ_CALENDAR, Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION}
                            , 123));
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION},
                            123);
                }
            }
        }
        act = this;
        System.out.println("done part 1");

        Push.setListener(new PushUtil());
        AppCenter.start(getApplication(), "fbc54802-e5ba-4a5d-9e02-e3a5dcf4922b",
                Analytics.class, Crashes.class, Auth.class, Data.class, Push.class);
        new GetSettings().starUp(this);

        final TagManager main53 = new TagManager(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        //name of the preference

        mName = mPrefs.getString("name", "please log in");
        mEmail = mPrefs.getString("email", "please log in");

        screenWidth = checkScreenWidth();

        final Activity ctx = this;
        //requestLocationPermission();


        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            time();
        }
        System.out.println("prefs");

        Toolbar mActionBarToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        recyclerView = findViewById(R.id.list);


        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(DrawerModeConstants.HOME).withName("Home").withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.home);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(DrawerModeConstants.TAGS).withName("Tags").withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.tag);
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(DrawerModeConstants.FOOD).withName("Food").withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.food);
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withIdentifier(DrawerModeConstants.SHOP).withName("Shopping").withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.shop);
        SecondaryDrawerItem item5 = new SecondaryDrawerItem().withIdentifier(DrawerModeConstants.CAL).withName("Agenda").withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.ic_today_black_24dp);
        SecondaryDrawerItem item6 = new SecondaryDrawerItem().withIdentifier(DrawerModeConstants.SHORTCUTS).withName("Shortcuts").withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.shortcut);
        SecondaryDrawerItem item7 = new SecondaryDrawerItem().withIdentifier(DrawerModeConstants.DEVICES).withName("Devices").withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.devices);
        SecondaryDrawerItem news1 = new SecondaryDrawerItem().withIdentifier(102).withName("Tech").withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.technews);
        SecondaryDrawerItem news3 = new SecondaryDrawerItem().withIdentifier(104).withName(getString(R.string.domeNews)).withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.domnews);
        SecondaryDrawerItem news4 = new SecondaryDrawerItem().withIdentifier(105).withName(getString(R.string.moneyText)).withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.money);
        SecondaryDrawerItem news2 = new SecondaryDrawerItem().withIdentifier(103).withName("World").withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.worldnews);
        SecondaryDrawerItem newsmain = new SecondaryDrawerItem().withIdentifier(101).withName("News").withTextColorRes(R.color.md_white_1000).withSelectedColorRes(R.color.card_color).withSubItems(news1, news2, news3, news4).withSelectedTextColorRes(R.color.md_white_1000).withIcon(R.drawable.news);
        System.out.println("items");
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                //.withHeaderBackground(R.drawable.back)


                .addProfiles(
                        new ProfileDrawerItem().withName(mName).withEmail(mEmail))

                .withOnAccountHeaderListener((view, profile, currentProfile) -> false).withTextColorRes(R.color.md_white_1000)
                .withHeaderBackground(R.color.card_color)
                .withThreeSmallProfileImages(true)

                .build();
        runOnUiThread(() -> drawer = new DrawerBuilder()
                .withActivity(ctx)
                .withToolbar(mActionBarToolbar)
                .withAccountHeader(headerResult)
                .withSliderBackgroundDrawableRes(R.drawable.drawerimage)
                .withFullscreen(true).withTranslucentNavigationBarProgrammatically(true)
                .withTranslucentStatusBar(true)
                .addDrawerItems(
                        item1,
                        item2,
                        item3,
                        newsmain,
                        item4,
                        item5,
                        item6,
                        item7
                )

                .withOnDrawerItemClickListener((view, position, drawerItem) -> {

                    MainActivity.this.runOnUiThread(() -> {
                        if(drawerItem.getIdentifier() == DrawerModeConstants.DEVICES){
                            System.out.println("devices");

                            Data.list(DeviceModel.class, DefaultPartitions.USER_DOCUMENTS).thenAccept(documentWrappers -> {
                                rssFeedModel1.clear();
                                if (!(documentWrappers == null)) {
                                    for (DocumentWrapper<DeviceModel> i : documentWrappers.getCurrentPage().getItems()) {
                                        System.out.println(i);
                                        if(!(null == i.getDeserializedValue().getType())) rssFeedModel1.add(new RssFeedModel(i.getDeserializedValue().getName(), i.getId(), i.getDeserializedValue().getGroup(), "", "", false));
                                    }
                                    recyclerView.setAdapter(new Adapter(rssFeedModel1));
                                    mode = drawerItem.getIdentifier();
                                } else System.out.println("fail");
                            });}
                        else if (drawerItem.getIdentifier() == DrawerModeConstants.SHORTCUTS) {
                            System.out.println("shortcuts");
                            Toast.makeText(ctx, "test", Toast.LENGTH_SHORT).show();
                            Data.list(SkillsDBModel.class, DefaultPartitions.USER_DOCUMENTS).thenAccept(new AppCenterConsumer<PaginatedDocuments<SkillsDBModel>>() {
                                @Override
                                public void accept(PaginatedDocuments<SkillsDBModel> documentWrappers) {
                                    rssFeedModel1.clear();
                                    if (!(documentWrappers == null)) {
                                        for (DocumentWrapper<SkillsDBModel> i : documentWrappers.getCurrentPage().getItems()) {
                                            if(!(i.getDeserializedValue().getAction()==null))rssFeedModel1.add(new RssFeedModel(i.getDeserializedValue().getName(), i.getId(), "", "", "", false));
                                        }
                                        recyclerView.setAdapter(new Adapter(rssFeedModel1));
                                        mode = drawerItem.getIdentifier();
                                    } else System.out.println("fail");
                                }

                            });

                        }
                        else {
                            try {
                                rssFeedModel1 = new Drawer().main(drawerItem.getIdentifier(), ctx, main53, MainActivity.this);
                                recyclerView.setAdapter(new Adapter(rssFeedModel1));
                                mode = drawerItem.getIdentifier();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    return false;
                    // do something with the clicked item :D
                })
                .build());

        System.out.println("drawer");

        Objects.requireNonNull(getSupportActionBar()).setTitle(mTime);
        StrictMode.setThreadPolicy(policy);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                new CardOnClick().mainFun(mode, rssFeedModel1.get(position).link, act, getApplicationContext());

            }


            @Override
            public void onLongClick(View view, int position) {
                new CardOnClick().longClick(rssFeedModel1.get(position), getApplicationContext(), main53, mode, act);

            }
        }));
        System.out.println("pre feed");

            try {
                rssFeedModel1 = (new Rss().parseRss(0, ctx));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("feed done");
            mAdapter = new Adapter(rssFeedModel1);

            recyclerView.setAdapter(mAdapter);
            FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            // Start the recording and recognition thread
            requestMicrophonePermission();
            Intent intent = new Intent(ctx, VoiceMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        new LogIn().logIn(mPrefs, getApplication());

    }

    private void checkScreenOrientation() {
        //GridLayoutManager
        GridLayoutManager gridLayoutManager;
        if (this.recyclerView.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(gridLayoutManager);
        } else if (this.recyclerView.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutManager = new GridLayoutManager(this, 4);
            recyclerView.setLayoutManager(gridLayoutManager);
        }
    }

    private int checkScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public void openSettingsActivity(MenuItem menuItem) {
        startActivity(new Intent(this, PrefsActivity.class));


    }


    public void about(MenuItem menuItem) {
        startActivity(new Intent(this, About.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (screenWidth > getResources().getInteger(R.integer.max_screen_width)) {
            checkScreenOrientation();
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        runOnUiThread(() -> {
            System.out.println("menu 1");
            getMenuInflater().inflate(R.menu.menu_main, menu);
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search)
                    .getActionView();
                assert searchManager != null;
                searchView.setSearchableInfo(searchManager
                        .getSearchableInfo(getComponentName()));
                searchView.setIconifiedByDefault(true);


            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                public boolean onQueryTextChange(String newText) {
                    // this is your adapter that will be filtered
                    return true;
                }

                public boolean onQueryTextSubmit(String query) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), query, Toast.LENGTH_SHORT).show();
                        LocationManager locationManager = locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                        assert locationManager != null;
                        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    try {
                        ArrayList<RssFeedModel> rssFeedModel2 = (new Search().main(query, getApplicationContext(), MainActivity.this));
                        rssFeedModel1.addAll(0, rssFeedModel2);
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);


    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the Drawer first and if the Drawer is closed close the activity
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if (drawer != null && !drawer.isDrawerOpen()) {
            drawer.openDrawer();
        } else {
            super.onBackPressed();
        }
    }
    @RequiresApi(26)
    public void time() {

        int mHour = LocalTime.now().getHour();
        if (mHour < 12) {
            mTime = "Good morning";
        } else if (mHour >= 12 && mHour < 16) {
            mTime = "good afternoon";
        } else {
            mTime = "Good evening";
        }
    }
    @Override
    protected void onPause(){
        super.onPause();

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }

    }

    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);

        }


    }

    public void logOut(MenuItem item) {
        Auth.signOut();
    }
    public void addSkill(MenuItem item) {
       new AraPopUps().newSkill(this);
    }

    public void addDevice(MenuItem item) throws MalformedURLException {
        new AraPopUps().newDevice(this, this);

    }
}