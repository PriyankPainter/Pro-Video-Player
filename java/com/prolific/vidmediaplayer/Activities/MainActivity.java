package com.prolific.vidmediaplayer.Activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.prolific.vidmediaplayer.Adapter.ViewPagerAdapter;
import com.prolific.vidmediaplayer.Fragments.FoldersFrag;
import com.prolific.vidmediaplayer.Fragments.RecentFrag;
import com.prolific.vidmediaplayer.Fragments.VideosFrag;
import com.prolific.vidmediaplayer.Others.AdLoader;
import com.prolific.vidmediaplayer.R;

import java.util.Random;

import static com.prolific.vidmediaplayer.Others.AdLoader.bannerAds;

public class MainActivity extends BaseActivity implements View.OnClickListener {//implements NavigationView.OnNavigationItemSelectedListener
    public static boolean PreviousWasPressedInVideo = false;
    public static boolean audioSongIsPlaying = false;
    public static boolean cameFromAudio = false;
    public static boolean isActivityAvailable = false;
    public static boolean isAudioStartedFromNotificationCLick = false;
    public static boolean isStartedFromNotificationCLick = false;
    public static int mCurrentlyWhere = 1;
    public static ViewPager mViewPager = null;
    public static boolean mediaIsPlayed = false;
    public static boolean nextWasPressedInVideo = false;
    public static int playedSongCurrentPosition = 0;
    public static String playedSongName = "";
    public static int selectedSortValue = 0;
    public static int selectedThemeValue = 0;
    public static int selectedView = 0;
    public static boolean videoSongIsPlaying = false;
    private DrawerLayout mDrawer;
    private NavigationView navigationView;
    String[] sortOptions;
    String[] themeOptions;
    private Toolbar toolbar;
    String[] viewOptions;
    private ImageView ivDrawer, iv_top_more;
    private boolean isOpenFirstTime;
    private ViewPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    public static int currTab = 0;
    private LinearLayout llAdaptiveBanner;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
//        this.sharedPref = new SharedPref(this);
//        this.sharedSortBy = new SharedSortBy(this);
//        sharedPrefView = new SharedPrefView(this);
//        if (this.sharedPref.loading() == 0) {
//            setTheme(R.style.AppTheme);
//        } else if (this.sharedPref.loading() == 1) {
//            setTheme(R.style.secondTheme);
//        } else if (this.sharedPref.loading() == 2) {
//            setTheme(R.style.redTheme);
//        }

//        Window window = this.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                /*| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN*/
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                /*| View.SYSTEM_UI_FLAG_FULLSCREEN*/
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//        getWindow().getDecorView().setSystemUiVisibility(flags);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBlue300));
        setContentView((int) R.layout.activity_main);

//        loadInterstitialAds();
        initialization();
        setToolbar();
//        new LoadMainAsync().execute();
        setDefaultPreferences();
        preBuildCode();
    }

    public void loadInterstitialAds() {
        try {
            if (AdLoader.getAd().isLoaded()) {
                AdLoader.interstitialAd.show();
//                new AdLoader.LoadingAds(context).execute();
                AdLoader.getAd().setAdListener(new AdListener() {

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.e("TAG", "FAIL AD LOAD : " + loadAdError);
                        AdLoader.initInterstitialAds();
//                        openMainScreen();
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        AdLoader.initInterstitialAds();
//                        openMainScreen();
                    }
                });
            } else {
                AdLoader.initInterstitialAds();
//                openMainScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
//            openMainScreen();
        }
    }


    public class LoadMainAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            preBuildCode();
                        }
                    }, 1000);
                }
            });
            return null;
        }
    }

    private void initialization() {
        viewPager = findViewById(R.id.container);
        tabLayout = findViewById(R.id.tabs);

        llAdaptiveBanner = findViewById(R.id.llAdaptiveBanner);
        bannerAds(llAdaptiveBanner, getAdSize(this));
//         frameContainer = findViewById(R.id.frameContainer);
//        navigationView = findViewById(R.id.nav_view);
//        mDrawer = findViewById(R.id.drawer_layout);
//        this.navigationView.setNavigationItemSelectedListener(this);
//        Toolbar toolbar2 = findViewById(R.id.toolbar);
//        ivDrawer = findViewById(R.id.ivDrawer);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        iv_top_more = findViewById(R.id.iv_top_more);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setDefaultPreferences() {
        isOpenFirstTime = getBooleanPrefs(this, IS_ALREADY_OPENED);
        if (!isOpenFirstTime) {
            setBooleanPrefs(this, IS_ALREADY_OPENED, true);
            setStringPrefs(this, PREF_SCREEN_RATIO, "BEST FIT");
            setStringPrefs(this, PREF_SLEEP, "off");
            setBooleanPrefs(this, PREF_IS_LAST_PLAY_VISIBLE, true);
            setBooleanPrefs(this, PREF_IS_PLAYER_AUTO_PLAY, true);
            setIntPrefs(this, PREF_SCREEN_ORIENTATION, 0);
            setIntPrefs(this, PREF_SORT_BY, 3);//DATE
            setIntPrefs(this, PREF_VIEW_AS, 0);//LIST
        }
    }

    private void preBuildCode() {
        addFragmentToViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
//                loadRandomAd();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void addFragmentToViewPager(ViewPager viewPager) {
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new VideosFrag(), "Video");
        pagerAdapter.addFragment(new FoldersFrag(), "Folder");
        pagerAdapter.addFragment(new RecentFrag(), "Recent");

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(currTab);
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount());
    }

    public void loadRandomAd() {
        try {
            int gRandom = new Random().nextInt(10);
            if (gRandom == 1) {
                if (AdLoader.getAd().isLoaded()) {
                    AdLoader.interstitialAd.show();
//                new AdLoader.LoadingAds(context).execute();
                    AdLoader.getAd().setAdListener(new AdListener() {

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            Log.e("TAG", "FAIL AD LOAD : " + loadAdError);
                            AdLoader.initInterstitialAds();
                        }

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            AdLoader.initInterstitialAds();
                        }
                    });
                } else {
                    AdLoader.initInterstitialAds();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        mCurrentlyWhere = 1;
//        if (cameFromAudio) {
//            mViewPager.setCurrentItem(2);
//            cameFromAudio = false;
//        }
    }

    public void onBackPressed() {
//        if (this.mDrawer.isDrawerOpen((int) GravityCompat.START)) {
//            this.mDrawer.closeDrawer((int) GravityCompat.START);
//        } else {
//        new AlertDialogClass().show(getSupportFragmentManager(), "Rate Us");
        rateDialog();
//        }
    }

    private Dialog dRateDialog;

    private void rateDialog() {
        dRateDialog = new Dialog(this);
        dRateDialog.setContentView(R.layout.layout_delete);
        dRateDialog.getWindow().setLayout(-1, -2);

        Button btnCancel = (Button) dRateDialog.findViewById(R.id.btnCancel);
        Button btnDelete = (Button) dRateDialog.findViewById(R.id.btnDelete);
        Button btnRate = (Button) dRateDialog.findViewById(R.id.btnRate);
        Button btnExit = (Button) dRateDialog.findViewById(R.id.btnExit);
        TextView tvDeleteMsg = (TextView) dRateDialog.findViewById(R.id.tvDeleteMsg);
        TextView tvTitle = (TextView) dRateDialog.findViewById(R.id.tvTitle);
        LinearLayout llDeleteButtons = (LinearLayout) dRateDialog.findViewById(R.id.llDeleteButtons);
        LinearLayout llRate = (LinearLayout) dRateDialog.findViewById(R.id.llRate);

        tvTitle.setVisibility(View.GONE);
        tvDeleteMsg.setVisibility(View.GONE);
        llDeleteButtons.setVisibility(View.GONE);
        llRate.setVisibility(View.VISIBLE);

//        tvTitle.setText("Rate Us");
//        tvDeleteMsg.setText("If you enjoy using app, please recommend us to others by rating us on Play atore");

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dRateDialog.dismiss();
                finish();
            }
        });

        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dRateDialog.dismiss();
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + getPackageName())));
                } catch (ActivityNotFoundException unused) {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
            }
        });

        dRateDialog.show();
    }

    public void restartApp() {
        preBuildCode();
    }

    public void onDestroy() {
        super.onDestroy();
//        if (audioSongIsPlaying || BackgroundPlay.mediaPlayerIsPlaying) {
//            stopService(new Intent(this, BackgroundPlay.class));
//            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(10);
//            stopService(new Intent(this, PlayingAudioService.class));
//            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(10);
//            if (audioSongIsPlaying) {
//                PlayingAudioActivity.mHandler.removeCallbacks(PlayingAudioActivity.mRunnable);
//                PlayingAudioActivity.mediaPlayer.stop();
//                PlayingAudioActivity.mediaPlayer.release();
//                PlayingAudioActivity.mediaPlayer = null;
//                audioSongIsPlaying = false;
//            }
//        }
    }

    @Override
    public void onClick(View v) {

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.custom_top_option, menu);
//
//        menu.findItem(R.id.item_top_menu).setVisible(false);
//        menu.findItem(R.id.actionSelect).setVisible(false);
//        menu.findItem(R.id.actionViewAs).setVisible(false);
//        menu.findItem(R.id.actionSortBy).setVisible(false);
//        menu.findItem(R.id.actionRefresh).setVisible(false);
//        menu.findItem(R.id.actionSetting).setVisible(false);
//        menu.findItem(R.id.actionRemoveAll).setVisible(false);
//
//        return true;
//    }

    //
//    public boolean onOptionsItemSelected(MenuItem menuItem) {
//        switch (menuItem.getItemId()) {
//            case R.id.choose_refresh:
//                finish();
//                startActivity(getIntent());
//                new Handler().postDelayed(new Runnable() {
//                    public void run() {
//                        Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
//                    }
//                }, 400);
//                break;
//
//            case R.id.choose_sort:
//                this.sortOptions = new String[]{"Name (A to Z)", "Name (Z to A)", "Date Added", "Album Name"};
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle((CharSequence) "Choose Sorting  option");
//                builder.setIcon((int) R.drawable.ic_sort);
//                builder.setSingleChoiceItems((CharSequence[]) this.sortOptions, selectedSortValue, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        if (i == 0) {
//                            MainActivity.this.sharedSortBy.settingSortBy(0);
//                            MainActivity.selectedSortValue = 0;
//                            MainActivity.this.restartApp();
//                            Toast.makeText(MainActivity.this, "Sorted By Name (A to Z)", Toast.LENGTH_SHORT).show();
//                        } else if (i == 1) {
//                            MainActivity.this.sharedSortBy.settingSortBy(1);
//                            MainActivity.selectedSortValue = 1;
//                            MainActivity.this.restartApp();
//                            Toast.makeText(MainActivity.this, "Sorted By Name (Z to A)", Toast.LENGTH_SHORT).show();
//                        } else if (i == 2) {
//                            MainActivity.this.sharedSortBy.settingSortBy(2);
//                            MainActivity.selectedSortValue = 2;
//                            MainActivity.this.restartApp();
//                            Toast.makeText(MainActivity.this, "Sorted By Date Added", Toast.LENGTH_SHORT).show();
//                        } else if (i == 3) {
//                            MainActivity.this.sharedSortBy.settingSortBy(3);
//                            MainActivity.selectedSortValue = 3;
//                            MainActivity.this.restartApp();
//                            Toast.makeText(MainActivity.this, "Sorted By Album Name", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//                builder.setPositiveButton((CharSequence) "Cancel", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                    }
//                });
//                builder.create().show();
//                break;
//
//            case R.id.choose_view:
//                this.viewOptions = new String[]{"Grid View", "List View"};
//                AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
//                builder3.setTitle((CharSequence) "Choose Sorting  option");
//                builder3.setIcon((int) R.drawable.ic_sort);
//                builder3.setSingleChoiceItems((CharSequence[]) this.viewOptions, selectedView, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        if (i == 0) {
//                            MainActivity.sharedPrefView.settingViewValue(0);
//                            MainActivity.selectedView = 0;
//                            Toast.makeText(MainActivity.this, "Grid View", Toast.LENGTH_SHORT).show();
//                            MainActivity.this.restartApp();
//                            return;
//                        }
//                        MainActivity.sharedPrefView.settingViewValue(1);
//                        MainActivity.selectedView = 1;
//                        Toast.makeText(MainActivity.this, "Linear View", Toast.LENGTH_SHORT).show();
//                        MainActivity.this.restartApp();
//                    }
//                });
//                builder3.setPositiveButton((CharSequence) "Cancel", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                    }
//                });
//                builder3.create().show();
//                break;


            /*case R.id.choose_theme:
                this.themeOptions = new String[]{"Dark", "Blue", "Red"};
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle((CharSequence) "Choose A Theme");
                builder2.setIcon((int) R.drawable.ic_themes);
                builder2.setSingleChoiceItems((CharSequence[]) this.themeOptions, selectedThemeValue, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            MainActivity.this.sharedPref.settingTheme(0);
                            MainActivity.this.restartApp();
                            Toast.makeText(MainActivity.this, "THEME CHANGED", Toast.LENGTH_SHORT).show();
                        } else if (i == 1) {
                            MainActivity.this.sharedPref.settingTheme(1);
                            MainActivity.this.restartApp();
                            Toast.makeText(MainActivity.this, "THEME CHANGED", Toast.LENGTH_SHORT).show();
                        } else if (i == 2) {
                            MainActivity.this.sharedPref.settingTheme(2);
                            MainActivity.this.restartApp();
                            Toast.makeText(MainActivity.this, "THEME CHANGED", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder2.setPositiveButton((CharSequence) "Cancel", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder2.create().show();
                break;

            case R.id.choose_searach:
                startActivity(new Intent(this, SearchActivity.class));
                break;

            case R.id.choose_share:
                Intent intent = new Intent();
                intent.setAction("android.intent.action.SEND");
                intent.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=com.prolific.vidmediaplayer&hl=en");
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, "Share Using"));
                break;*/
//        }
//        return super.onOptionsItemSelected(menuItem);
//    }




    /*public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_exit:
                finish();
                break;
            case R.id.nav_folders:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.nav_privacy:
                startActivity(new Intent(this, PrivacyPolicy.class));
                finish();
                break;
            case R.id.nav_rate:
                try {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + getPackageName())));
                    break;
                } catch (ActivityNotFoundException unused) {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                    break;
                }
            case R.id.nav_share:
                Intent intent = new Intent();
                intent.setAction("android.intent.action.SEND");
                intent.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=com.prolific.vidmediaplayer&hl=en");
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, "Share Using"));
                break;
            case R.id.nav_theme:
                this.themeOptions = new String[]{"Dark", "Blue", "Red"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle((CharSequence) "Choose A Theme");
                builder.setIcon((int) R.drawable.ic_themes);
                builder.setSingleChoiceItems((CharSequence[]) this.themeOptions, selectedThemeValue, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            MainActivity.this.sharedPref.settingTheme(0);
                            MainActivity.this.restartApp();
                            Toast.makeText(MainActivity.this, "THEME CHANGED", Toast.LENGTH_SHORT).show();
                        } else if (i == 1) {
                            MainActivity.this.sharedPref.settingTheme(1);
                            MainActivity.this.restartApp();
                            Toast.makeText(MainActivity.this, "THEME CHANGED", Toast.LENGTH_SHORT).show();
                        } else if (i == 2) {
                            MainActivity.this.sharedPref.settingTheme(2);
                            MainActivity.this.restartApp();
                            Toast.makeText(MainActivity.this, "THEME CHANGED", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setPositiveButton((CharSequence) "Cancel", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.create().show();
                break;
            case R.id.nav_video:
                mViewPager.setCurrentItem(0);
                break;
        }
        this.mDrawer.closeDrawer((int) GravityCompat.START);
        return true;
    }*/


}
