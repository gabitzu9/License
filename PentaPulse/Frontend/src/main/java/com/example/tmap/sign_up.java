package com.example.tmap;

import static com.example.tmap.Variabile.nume_utilizator;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
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

public class sign_up extends AppCompatActivity {

    private OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.sign_up);

        // Initialize OkHttpClient once
        client = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();

        // Set up click listener for the sign-up button
        Button signUpButton = findViewById(R.id.fsign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSignUp();
            }
        });
    }

    private void performSignUp() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);// inchiderea tastaturii virtuale
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        EditText name = findViewById(R.id.fname);
        EditText surname = findViewById(R.id.fsurname);
        EditText email = findViewById(R.id.femaillog);
        EditText phone = findViewById(R.id.fnr_tel);
        EditText pass = findViewById(R.id.fpasswordlog);
        EditText confirmPassword = findViewById(R.id.fconfirmpassword);

        nume_utilizator = name.getText().toString();

        if (!validateEmpty(name, surname, email, phone, pass, confirmPassword)) {
            return;
        }

        JSONObject post = new JSONObject();
        try {
            post.put("name", name.getText().toString());
            post.put("surname", surname.getText().toString());
            post.put("email", email.getText().toString());
            post.put("phone", phone.getText().toString());
            post.put("pass", pass.getText().toString());
            post.put("confirmpass", confirmPassword.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON, post.toString());
        Request request = new Request.Builder()
                //.url("http://10.0.2.2:3000/signup")
                .url("http://192.168.1.241:3000/signup")
                //.url("http://72.14.201.94:3000/signup")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String mesaj = response.body().string();

                        if (mesaj.equalsIgnoreCase("Database updated successfully")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(sign_up.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    // Optionally navigate to another activity here
                                    Intent intent = new Intent(sign_up.this, log_in.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else if (mesaj.equalsIgnoreCase("Current email already exists in database")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(sign_up.this, "Current email already exists!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(sign_up.this, "Database Errorr", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(sign_up.this, "Error reading response", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(sign_up.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }
    private Boolean validateEmpty(EditText name, EditText surname, EditText email, EditText phone, EditText pass, EditText confirmpass){
        boolean error = false;
        Drawable icon = getDrawable(R.drawable.ic_warning);

        if( TextUtils.isEmpty(name.getText())){
            name.setError("name required", icon);error = true;}
        if( TextUtils.isEmpty(surname.getText())){
            surname.setError("surname required", icon);error = true;}
        if( TextUtils.isEmpty(email.getText())){
            email.setError("email required", icon);error = true;}
        if( TextUtils.isEmpty(phone.getText())){
            phone.setError("phone required", icon);error = true;}
        if( TextUtils.isEmpty(pass.getText())){
            pass.setError("pass required", icon);error = true;}
        if( TextUtils.isEmpty(confirmpass.getText())){
            confirmpass.setError("confirmpass required", icon);error = true;}

        if(name.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\\\.+[a-z]+")){
            if(pass.getText().toString().length()>=2){
                if(confirmpass.getText().toString().equals(pass.getText().toString())){
                    Toast.makeText(getApplicationContext(),"registre successful",Toast.LENGTH_SHORT).show();
                }else{
                    confirmpass.setError("pass din`t match",icon);
                    error = true;
                }
            }else{
                pass.setError("enter at least 6 character",icon);
                error = true;
            }
        }else{
            name.setError("enter valid email id",icon);
            error = true;
        }
        return error;
    }

}