package app.solution.barcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.solution.barcode.database.DBHandler;
import app.solution.barcode.database.DbModelClass;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    Vibrator vibrator;
    private Fragment fragment;
    DrawerLayout drawer;
    String title;
    private ImageView imgSave, imgReset, imgSearch, imgFrame;
    private static final int REQUEST_GET_ACCOUNT = 112;
    boolean flashmode = false;
    private Camera cam = null;
    private SurfaceView cameraView;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private TextView barcodeValue;
    private LinearLayout linearLayout;
    private FrameLayout frameLayout;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initUI();

        loadBannerAd();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    if (isCameraPermissin()) {
                        cameraSource.start(cameraView.getHolder());
                    } else {
                        requestStoragePermission();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
                barcodeDetector.release();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor() {

            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    barcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            //Update barcode value to TextView
                            Log.d("Response", barcodes.toString());
                            for (int i = 0; i < 1; i++) {
                                Log.d("Barcodesss", barcodes.valueAt(i).toString());
                            }
                            String barCodeResult = barcodes.valueAt(0).rawValue;
                            vibrator.vibrate(50);
                            //barcodeValue.setMovementMethod(LinkMovementMethod.getInstance());
                            barcodeValue.setText(barCodeResult);
                        }
                    });
                }
            }
        });
    }

    private void loadBannerAd() {
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
    }


    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_GET_ACCOUNT);
    }

    private boolean isCameraPermissin() {
        //Getting the permission status
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        //If permission is granted returning true
        if (cameraPermission == PackageManager.PERMISSION_GRANTED)
            return true;
        //If permission is not granted returning false
        return false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_GET_ACCOUNT:

                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted) {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Snackbar.make(view, "Permission Granted, Now you can access location data and camera.", Snackbar.LENGTH_LONG).show();
                else {
                    //Snackbar.make(view, "Permission Denied, You cannot access location data and camera.", Snackbar.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{Manifest.permission.CAMERA},
                                                        REQUEST_GET_ACCOUNT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void initUI() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        linearLayout = findViewById(R.id.linearLayout);
        imgReset = findViewById(R.id.imgReset);
        imgSave = findViewById(R.id.imgSave);
        imgSearch = findViewById(R.id.imgSearch);

        imgSearch.setOnClickListener(this);
        imgSave.setOnClickListener(this);
        imgReset.setOnClickListener(this);

        barcodeValue = findViewById(R.id.barcodeValue);
        cameraView = (SurfaceView) findViewById(R.id.surface_view);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();


        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce){
                super.onBackPressed();
                return;
            }else {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please press BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2000);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.ic_action_flash || id == R.id.ic_action_flash_off) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = getCamera(cameraSource);
                if (cam != null) {
                    try {
                        Camera.Parameters param = cam.getParameters();
                        param.setFlashMode(!flashmode ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                        cam.setParameters(param);
                        flashmode = !flashmode;
                        if (flashmode) {
                            itemOn.setVisible(false);
                            itemOff.setVisible(true);
                        } else {
                            itemOn.setVisible(true);
                            itemOff.setVisible(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(this, "Flash not available on your device", Toast.LENGTH_LONG).show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    MenuItem itemOn, itemOff;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        itemOn = menu.findItem(R.id.ic_action_flash);
        itemOff = menu.findItem(R.id.ic_action_flash_off);

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            drawer.closeDrawers();
            fragment = new HistoryFragment();
            openFragment(fragment);
            // Handle the camera action
        } else if (id == R.id.nav_home) {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            }

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey check out my app at: https://play.google.com/store/apps/details?id=");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_send) {
            launchMarket();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static Camera getCamera(@NonNull CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
        Log.d("LifeCycle", "OnDestroy");
        cameraSource.release();
        barcodeDetector.release();
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
        Log.d("LifeCycle", "onPause");
        cameraSource.stop();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();
        Log.d("LifeCycle", "onResume");
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.imgReset:
                final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
                imgReset.startAnimation(myAnim);
                startActivity(new Intent(MainActivity.this, ScannerActivity.class));
                //barcodeValue.setText("Processing...");
                break;
            case R.id.imgSave:
                final Animation myAnimSave = AnimationUtils.loadAnimation(this, R.anim.bounce);
                imgSave.startAnimation(myAnimSave);
                if (barcodeValue.getText().toString().contains("Processing...")) {
                    Toast.makeText(this, "Nothing to save!", Toast.LENGTH_LONG).show();
                    return;
                }
                showTitleDialog();
                break;
            case R.id.imgSearch:

                final Animation myAnimSearch = AnimationUtils.loadAnimation(this, R.anim.bounce);
                imgSearch.startAnimation(myAnimSearch);
                if (barcodeValue.getText().toString().contains("Processing...")) {
                    Toast.makeText(this, "Nothing to search!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (isOnline(this)){
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, barcodeValue.getText().toString());
                    startActivity(intent);
                }else {
                    Toast.makeText(getApplicationContext(), "Please check your internet connection!",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void showTitleDialog() {
        final String currentDateandTime = new SimpleDateFormat("EEE, d MMM yyyy HH:mma").format(new Date());
        Log.d("CurrentTime", currentDateandTime);
        final Dialog dialogConfirm = new Dialog(MainActivity.this,
                R.style.MyDialog);
        dialogConfirm.setContentView(R.layout.custom_dialog);
        dialogConfirm.setCancelable(true);
        final Button btnOk = (Button) dialogConfirm.findViewById(R.id.btnOk);
        final Button btnCancel = (Button) dialogConfirm.findViewById(R.id.btnCancel);
        final EditText etTitle = (EditText) dialogConfirm.findViewById(R.id.etTitle);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogConfirm.dismiss();
                title = etTitle.getText().toString();
                if (!title.equals("")) {
                    DBHandler db = new DBHandler(MainActivity.this);
                    DbModelClass dbModelClass = new DbModelClass();
                    dbModelClass.setTitle(title);
                    dbModelClass.setScanQuery(barcodeValue.getText().toString());
                    dbModelClass.setDateTime(currentDateandTime);
                    db.addItem(dbModelClass);
                    Toast.makeText(MainActivity.this, "Data Saved Successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please give a title", Toast.LENGTH_LONG).show();
                }
                title = "";
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogConfirm.dismiss();
            }
        });

        dialogConfirm.show();
    }

    public void openFragment(final Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.frameLayout, fragment);
        transaction.addToBackStack("tag");
        transaction.commit();

    }

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }
}
