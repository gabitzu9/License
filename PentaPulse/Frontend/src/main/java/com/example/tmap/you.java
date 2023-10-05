package com.example.tmap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class you extends AppCompatActivity {

    private int currentSelectedItem = R.id.action_you;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.you_activity);


        Button log_out = findViewById(R.id.log_out);
        log_out.setOnClickListener(v -> {
            Intent intent = new Intent(you.this, log_in.class);
            startActivity(intent);
            finish();
        });







        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(currentSelectedItem);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        currentSelectedItem = R.id.action_home;
                        startActivity(new Intent(you.this, Acasa.class));
                        break;
                    case R.id.action_map:
                        currentSelectedItem = R.id.action_map;
                        startActivity(new Intent(you.this, MapsActivity.class));
                        break;
                    case R.id.action_register:
                        currentSelectedItem = R.id.action_register;
                        startActivity(new Intent(you.this, rec_traseu.class));
                        break;
                    case R.id.action_you:
                        currentSelectedItem = R.id.action_you;
                        startActivity(new Intent(you.this, you.class));
                        break;
                }

                // Setează starea selectată pentru elementul curent
                bottomNavigationView.getMenu().findItem(currentSelectedItem).setChecked(true);

                return true;
            }
        });}
}