package app.solution.barcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;

public class ScannerActivity extends AppCompatActivity implements View.OnClickListener {

    Vibrator vibrator;
    private Toolbar toolbar;
    boolean flashmode = false;
    private SurfaceView cameraView;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private Camera cam = null;
    private ImageView imgFlashOn, imgFlashOff, imgFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.tool_bar_color), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Implemented by activity
            }
        });


        initUI();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    cameraSource.start(cameraView.getHolder());
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

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            int i = 0;

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    //Update barcode value to TextView
                    String barCodeResult = barcodes.valueAt(0).rawValue;
                    if (i==0){
                        i = 1;
                        Intent intent = new Intent(ScannerActivity.this, BarcodeResultActivity.class);
                        intent.putExtra("result", barCodeResult);
                        startActivity(intent);
                        barcodeDetector.release();
                        finish();
                    }else {
                        return;
                    }
                    Log.d("QRCodeValue", barCodeResult);
                }
            }
        });
    }

    private void initUI() {

        imgFlashOff = findViewById(R.id.imgFlashOff);
        imgFlashOn = findViewById(R.id.imgFlashOn);

        imgFlashOff.setOnClickListener(this);
        imgFlashOn.setOnClickListener(this);

        imgFrame = findViewById(R.id.imgFrame);
        imgFrame.getLayoutParams().height = (int) (getScreenHeight()/1.5);
        imgFrame.getLayoutParams().width = (int) (getScreenWidth()/1.5);
        imgFrame.requestLayout();

        cameraView = findViewById(R.id.surface_view);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.imgFlashOff:
                flashOnOff();
                break;
            case R.id.imgFlashOn:
                flashOnOff();
                break;
        }
    }

    private void flashOnOff() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            cam = getCamera(cameraSource);
            if (cam != null) {
                try {
                    Camera.Parameters param = cam.getParameters();
                    param.setFlashMode(!flashmode ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                    cam.setParameters(param);
                    flashmode = !flashmode;
                    if (flashmode) {
                        imgFlashOn.setVisibility(View.VISIBLE);
                        imgFlashOff.setVisibility(View.GONE);
                    } else {
                        imgFlashOn.setVisibility(View.GONE);
                        imgFlashOff.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else {
            Toast.makeText(ScannerActivity.this, "Flash not available on your device",Toast.LENGTH_LONG).show();
        }
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

    public double getScreenHeight() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        return height;
    }
    public int getScreenWidth() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        return width;
    }

    public static boolean isOnline(final Context ctx) {
        try {
            final ConnectivityManager cm = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null) {
                return ni.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

}
