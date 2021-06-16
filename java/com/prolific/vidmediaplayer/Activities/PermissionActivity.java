package com.prolific.vidmediaplayer.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.prolific.vidmediaplayer.Others.AdLoader;
import com.prolific.vidmediaplayer.R;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class PermissionActivity extends BaseActivity {

    private Button btnAllow;
    private final int REQ_READ_WRITE_PERMISSION = 100;
    private final int REQ_WRITE_SETTING = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                /*| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN*/
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                /*| View.SYSTEM_UI_FLAG_FULLSCREEN*/
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        setContentView(R.layout.activity_permission);

        btnAllow = findViewById(R.id.btnAllow);

        btnAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPermission();
            }
        });

    }

    public void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, REQ_READ_WRITE_PERMISSION);
            } else {
                checkNeededPermission();
            }
        } else {
            checkNeededPermission();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_READ_WRITE_PERMISSION) {
            if (permissions.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    checkNeededPermission();

                } else {
                    if (!shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) || !shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        alert.setTitle("Permission needed");
                        alert.setMessage("You must have to grant all required permission from settings to work app functions.");
                        alert.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, REQ_READ_WRITE_PERMISSION);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                dialogInterface.dismiss();
                            }
                        });

                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        });
                        alert.create();
                        alert.show();

                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        alert.setTitle("Permission needed");
                        alert.setMessage("App needs all permissions to work app functions.");
                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, REQ_READ_WRITE_PERMISSION);
                                dialogInterface.dismiss();
                            }
                        });

                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        });
                        alert.create();
                        alert.show();
                    }
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == RESULT_OK) {
        if (requestCode == REQ_READ_WRITE_PERMISSION) {
            getPermission();
        } else if (requestCode == REQ_WRITE_SETTING) {
            getPermission();
        }
//        }
    }

    public void alertForWritePermission() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Permission needed");
        alert.setMessage("App needs to allow modify system setting permission from settings window.");
        alert.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                getPermission();

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                loadInterstitialAds(PermissionActivity.this);
            }
        });
        alert.create();
        alert.show();
    }


    public void checkNeededPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, REQ_WRITE_SETTING);
            } else {
                openMainScreen();
            }
        } else {
            openMainScreen();
        }
    }


    public void loadInterstitialAds(Activity context) {
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
                        openMainScreen();
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        AdLoader.initInterstitialAds();
                        openMainScreen();
                    }
                });
            } else {
                AdLoader.initInterstitialAds();
                openMainScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
            openMainScreen();
        }
    }

    public void openMainScreen() {
        startActivity(new Intent(PermissionActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        finish();
    }
}