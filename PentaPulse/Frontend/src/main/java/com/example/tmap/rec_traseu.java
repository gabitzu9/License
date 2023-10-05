package com.example.tmap;

import static com.example.tmap.Variabile.contor;
import static com.example.tmap.Variabile.email_global;
import static com.example.tmap.Variabile.nume;
import static com.example.tmap.log_in.JSON;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class rec_traseu extends AppCompatActivity {

    private OkHttpClient client;
    private int currentSelectedItem = R.id.action_register;

    JSONArray routesWithPoints;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rec_traseu_activity);

        // Setarea stilului pentru textul din bara de titlu
        SpannableString spannableString = new SpannableString("                             PentaPulse");
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(com.google.android.libraries.places.R.color.quantum_deeporangeA700)), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(spannableString);
            // Setarea fundalului barei de titlu
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(com.google.android.libraries.places.R.color.quantum_googblueA700)));
        }

        client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        ImageView deleteRoute = findViewById(R.id.deleteButton);
        deleteRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRoute();
            }
        });
        ImageView viewRoute = findViewById(R.id.viewButton);
        viewRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<LatLng> coordonate = new ArrayList<>();
                try {
                    coordonate= getCoordonates();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                MapsActivity mapsActivity = new MapsActivity();

                if (mapsActivity != null) {
                    mapsActivity.drawRoute(coordonate);
                }
            }
        });
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText text1= findViewById(R.id.ruta);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText text2= findViewById(R.id.start);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText text3= findViewById(R.id.finish);


        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button rec = findViewById(R.id.btn_rec_traseu);
        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsActivity mapsActivity = new MapsActivity();
                nume[0] =text1.getText().toString();
                nume[1] =text2.getText().toString();
                nume[2] =text3.getText().toString();
                if(!nume[0].equals("")) {
                    //mapsActivity.start();
                    contor=true;
                    Intent intent = new Intent(rec_traseu.this, MapsActivity.class);
                    intent.putExtra("activateStartRoute", true);
                    startActivity(intent);
                }
            }
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button rute = findViewById(R.id.btn_show_routes);
        rute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllRoutesClick();
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
                        startActivity(new Intent(rec_traseu.this, Acasa.class));
                        break;
                    case R.id.action_map:
                        currentSelectedItem = R.id.action_map;
                        startActivity(new Intent(rec_traseu.this, MapsActivity.class));
                        break;
                    case R.id.action_register:
                        currentSelectedItem = R.id.action_register;
                        startActivity(new Intent(rec_traseu.this, rec_traseu.class));
                        break;
                    case R.id.action_you:
                        currentSelectedItem = R.id.action_you;
                        startActivity(new Intent(rec_traseu.this, you.class));
                        break;
                }

                // Setează starea selectată pentru elementul curent
                bottomNavigationView.getMenu().findItem(currentSelectedItem).setChecked(true);

                return true;
            }
        });
    }

    public void getAllRoutesClick() {

        //email_global="q";
        //String url = "http://10.0.2.2:3000/get_routes/" + email_global;
        String url = "http://192.168.1.241:3000/get_routes/" + email_global;
        //String url = "http://72.14.201.94:3000/get_routes/" + email_global;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Handle error, e.g., show a toast message
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        List<String> routeNames = new ArrayList<>();
                        String responseBody = response.body().string();

                        // Parse the JSON response
                        routesWithPoints = new JSONArray(responseBody);

                        // Process and collect route names
                        for (int i = 0; i < routesWithPoints.length(); i++) {
                            JSONObject routeWithPoints = routesWithPoints.getJSONObject(i);
                            JSONObject route = routeWithPoints.getJSONObject("route");

                            String routeName = route.getString("nume_traseu");
                            routeNames.add(routeName);
                        }
                        // Display route names in the UI
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Display the route names, e.g., in a TextView or RecyclerView
                                // Replace "textView" with the actual ID of your TextView
                                TextView textView = findViewById(R.id.textView);
                                textView.setText(TextUtils.join("\n", routeNames));
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(rec_traseu.this, "Errorrrrrrrrrrr", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(rec_traseu.this, "Erroare la rute si puncte ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void deleteRoute(){

        EditText ruta=findViewById(R.id.textRoute);
        String text = ruta.getText().toString();

        JSONObject post = new JSONObject();

        try {
            post.put("email",email_global);
            post.put("cauta_nume_traseu",text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, post.toString());
        Request request = new Request.Builder()
                //.url("http://10.0.2.2:3000/delete_rec")
                .url("http://192.168.1.241:3000/delete_rec")
                //.url("http://72.14.201.94:3000/delete_rec")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Handle error, e.g., show a toast message
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String mesaj = response.body().string();

                        if (mesaj.equalsIgnoreCase("Database updated - DELETE")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(rec_traseu.this, "Route deleted successfully", Toast.LENGTH_SHORT).show();
                                    // Optionally navigate to another activity here
                                }
                            });
                        } else if (mesaj.equalsIgnoreCase("Database Error")) {
                            Toast.makeText(rec_traseu.this, "Database Error", Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(rec_traseu.this, "Error reading response", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(rec_traseu.this, "Err la trimiterea datelor", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        ruta.setText("");
    }


    private List<LatLng> getCoordonates() throws JSONException {

        List<LatLng> coordonate = new ArrayList<>();
        EditText ruta=findViewById(R.id.textRoute);
        String text = ruta.getText().toString();

        for (int i = 0; i < routesWithPoints.length(); i++) {
            JSONObject routeWithPoints = routesWithPoints.getJSONObject(i);
            JSONObject route = routeWithPoints.getJSONObject("route");

            if(text.equalsIgnoreCase(route.getString("nume_traseu")))
            {
                JSONObject points=routeWithPoints.getJSONObject("points");
                for (int j=0;j<points.length();j++){
                    LatLng coordPunct=new LatLng(Double.parseDouble(points.getString("lat")),Double.parseDouble(points.getString("lng")));
                    coordonate.add(coordPunct);
                }
            }
        }
        return coordonate;
    }
}
