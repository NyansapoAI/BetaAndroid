package com.example.edward.nyansapo;


import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.edward.nyansapo.R;
public class registerSchool extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_school);
    }

    public void selectStudent(View v){
        Intent myIntent = new Intent(getBaseContext(), student_activity.class);
        startActivity(myIntent);
    }
}
