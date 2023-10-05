package com.example.tmap;

import static com.example.tmap.Variabile.nume_utilizator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Random;

public class Acasa extends AppCompatActivity {

    private Handler handler;
    private int currentSelectedItem = R.id.action_home;
    private String[] mesajeMotivationale = {
            "Every step counts! Keep moving.",
            "Today's effort is tomorrow's victory.",
            "Make your body smile through movement!",
            "You have the power to overcome any obstacle. Including starting to exercise!",
            "Movement is the medicine for a healthy mind",
            "You will never regret a training session.",
            "Start with what you can, where you are. The important thing is to start!",
            "It doesn't matter how slow you go, as long as you don't stop.",
            "Sport is not just about being better than someone else.",
            "It's about being better than you were yesterday.",
            "Pain is temporary, but letting go lasts forever.",
            "Each workout brings you one step closer to your goal.",
            "Every day is a chance to improve yourself.Don't lose it.",
            "If you can dream something, you can do it.",
            "Be active, be healthy, be happy!"
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.AppTheme_Launch);

        setContentView(R.layout.acasa_activity);

        // Setarea stilului pentru textul din bara de titlu
        SpannableString spannableString = new SpannableString("                             PentaPulse");
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(com.google.android.libraries.places.R.color.quantum_deeporangeA700)), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(spannableString);
            // Setarea fundalului barei de titlu
           actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(com.google.android.libraries.places.R.color.quantum_googblueA700)));
        }

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView mesaj = findViewById(R.id.mesajmic);
        mesaj.setText("Hey " + nume_utilizator + "!");

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageView smile_face=findViewById(R.id.robot);
        TextView mesaj2 = findViewById(R.id.mesajmare);
        handler = new Handler();

        smile_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Random random = new Random();
                int index = random.nextInt(mesajeMotivationale.length);
                /*mesaj2.setText(mesajeMotivationale[index]);
*/

                if (index < mesajeMotivationale.length) {
                    String mesaj = mesajeMotivationale[index];
                    mesaj2.setText(""); // Resetarea textului

                    // Divizează mesajul în cuvinte
                    String[] cuvinte = mesaj.split(" ");

                    // Afișează cuvintele cu un interval
                    for (int i = 0; i < cuvinte.length; i++) {
                        final int finalI = i;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String cuvant = cuvinte[finalI];
                                mesaj2.append(cuvant + " ");
                            }
                        }, 200 * i); // Intervalul de timp între cuvinte (200 milisecunde * index)
                    }
                }
            }
        });









        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(currentSelectedItem);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        currentSelectedItem = R.id.action_home;
                        startActivity(new Intent(Acasa.this, Acasa.class));
                        break;
                    case R.id.action_map:
                        currentSelectedItem = R.id.action_map;
                        startActivity(new Intent(Acasa.this, MapsActivity.class));
                        break;
                    case R.id.action_register:
                        currentSelectedItem = R.id.action_register;
                        startActivity(new Intent(Acasa.this, rec_traseu.class));
                        break;
                    case R.id.action_you:
                        currentSelectedItem = R.id.action_you;
                        startActivity(new Intent(Acasa.this, you.class));
                        break;
                }

                // Setează starea selectată pentru elementul curent
                bottomNavigationView.getMenu().findItem(currentSelectedItem).setChecked(true);

                return true;
            }
        });
    }
}