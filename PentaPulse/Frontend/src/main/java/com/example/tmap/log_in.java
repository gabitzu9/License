package com.example.tmap;

import static com.example.tmap.Variabile.email_global;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class log_in extends AppCompatActivity {

    private OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.log_in);

        client = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();


        Button signup_log = findViewById(R.id.fsignup);
        signup_log.setOnClickListener(v -> {
            Intent intent = new Intent(log_in.this, sign_up.class);
            startActivity(intent);
            finish();
        });

        Button log_in = findViewById(R.id.flogin);
        log_in.setOnClickListener(v -> {
                loginClick();
        });
    }

    public void loginClick() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);// inchiderea tastaturii virtuale
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        EditText email = findViewById(R.id.femaillog);
        EditText pass = findViewById(R.id.fpasswordlog);

        email_global = email.getText().toString();

        if (validateInput(email, pass)) {
            JSONObject post = new JSONObject();
            try {
                post.put("email", email.getText().toString());
                post.put("pass", pass.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(JSON, post.toString());
            Request request = new Request.Builder()
                    //.url("http://10.0.2.2:3000/login")
                    .url("http://192.168.1.241:3000/login")
                    //.url("http://72.14.201.94:3000/login")
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

                            if (mesaj.equalsIgnoreCase("Logged in successfully")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(log_in.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                        // Optionally navigate to another activity here
                                        Intent intent = new Intent(log_in.this, Acasa.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else if (mesaj.equalsIgnoreCase("Invalid password")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(log_in.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else if(mesaj.equalsIgnoreCase("Autentification failed")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(log_in.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(log_in.this, "Database Errorr", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(log_in.this, "Error reading response", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(log_in.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

    private boolean validateInput(EditText email, EditText pass) {
   /*     String userEmail = email.getText().toString();
        String userPass = pass.getText().toString();

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Email required");
            return false;
        } else if (!isValidEmail(userEmail)) {
            email.setError("Invalid email format");
            return false;
        }

        if (TextUtils.isEmpty(userPass)) {
            pass.setError("Password required");
            return false;
        } else if (userPass.length() < 6) {
            pass.setError("Password should be at least 6 characters");
            return false;
        }
*/
        return true;
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

}