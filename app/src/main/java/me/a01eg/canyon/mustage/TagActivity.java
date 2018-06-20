package me.a01eg.canyon.mustage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pchmn.materialchips.ChipsInput;

public class TagActivity extends AppCompatActivity {

    private ChipsInput chipsInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        chipsInput = findViewById(R.id.chipsInput);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
