package app.solution.barcode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class BarcodeResultActivity extends AppCompatActivity {

    private Intent intent;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_result);

        intent = getIntent();
        result = intent.getStringExtra("result");

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }
}
