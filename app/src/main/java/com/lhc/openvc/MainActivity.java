package com.lhc.openvc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void face(View view) {
        startActivity(new Intent(this, FaceActivity.class));
    }


    public void idCard(View view) {
        startActivity(new Intent(this, IdCardActivity.class));
    }

}
