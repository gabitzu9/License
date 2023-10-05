package com.example.tmap;

import static com.example.tmap.Variabile.contor;
import static com.example.tmap.Variabile.email_global;
import static com.example.tmap.Variabile.nume;
import static com.example.tmap.log_in.JSON;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dtos.CoordinatesDTO;
import dtos.LocationDTO;
import dtos.RoutePointsDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private int currentSelectedItem = R.id.action_map;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION =111 ;
    private GoogleMap mMap;
    private boolean locationPermissionGranted;
    private static final int DEFAULT_ZOOM = 12;
    private static final int DEFAULT_ZOOM_2 = 15;

    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastKnownLocation;
    private LatLng selectedLocation;

    private LatLng startLocationLatLng;
    private LatLng destinationLatLng;
    private List<LatLng> routePoints = new ArrayList<>();
    private Polyline routePolyline;
    private boolean isRouteTrackingActive = false;
    private List<List<LatLng>> recordedRoutes = new ArrayList<>();
    private OkHttpClient client;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadMapFragment();
    }
    private void loadMapFragment() {

        setContentView(R.layout.activity_maps);      // Retrieve the content view that renders the map.

        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Create the LocationRequest to request location updates
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //locationRequest.setInterval(10); // 1000 seconds
        // Create the LocationCallback to handle location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lastKnownLocation = location;
                    if (mMap != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    }
                }
            }
        };

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Call this method to request location permission
        getLocationPermission();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap mMap) {
         this.mMap = mMap;

        // Add a marker on the current location (if available)
        if (lastKnownLocation != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                    .title("Current Location"));
        }

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Start requesting location updates
        startLocationUpdates();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedLocation = latLng;
                //mMap.clear(); // Clear map of any previous markers
                mMap.addMarker(new MarkerOptions().position(selectedLocation).title("Selected Location"));
            }
        });

        // Handle the buttons click to find the destination
        ImageView buttonFindLocation = findViewById(R.id.ic_location);
        buttonFindLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), DEFAULT_ZOOM_2));

            }
        });
        ImageView buttonFindPlace = findViewById(R.id.ic_gps);
        buttonFindPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the destination name from an EditText or any other input method
                EditText startLocation = findViewById(R.id.searchDestination);
                EditText destinationLocation = findViewById(R.id.textDestination);

                mMap.clear();
                findPlaceByName(startLocation.getText().toString(), destinationLocation.getText().toString());
                enableStartLocationLayout();

            }
        });
        ImageView buttonDrivePlace = findViewById(R.id.ic_drive);
        buttonDrivePlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);// inchiderea tastaturii virtuale
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                // Get the destination name from an EditText or any other input method
                EditText editTextDestination = findViewById(R.id.textDestination);
                String destinationName = editTextDestination.getText().toString();

                if(startLocationLatLng != null && destinationLatLng != null){
                    createRoute(startLocationLatLng,destinationLatLng);
                }else if(selectedLocation != null){
                    LatLng originLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    createRoute(originLatLng, selectedLocation);
                }else{
                    LocationDTO locationDTO = populateLocationByName(destinationName);
                    if(locationDTO == null){
                        return;
                    }

                    MarkerOptions startLocationMarkerOptions = new MarkerOptions()
                            .position(locationDTO.getDestinationLatLng())
                            .title("Destination");
                    mMap.addMarker(startLocationMarkerOptions);

                    startLocationLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    createRoute(startLocationLatLng, locationDTO.getDestinationLatLng());


                    enableStartLocationLayout();
                }
            }
        });
        ImageView clearMap = findViewById(R.id.ic_clear);
        clearMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the destination name from an EditText or any other input method
                EditText startLocation = findViewById(R.id.searchDestination);
                EditText destinationLocation = findViewById(R.id.textDestination);
                startLocation.setText(null);
                destinationLocation.setText(null);
                mMap.clear();
            }
        });
        ImageView startRouteButton = findViewById(R.id.ic_start_route);
        startRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableStartRouteLayout();
            }
        });
        ImageView saveRouteButton = findViewById(R.id.ic_save);
        saveRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println(routePoints);

                // Salvează traseul în lista de înregistrări
                if (routePoints != null) {
                    saveroute(routePoints);
                }
                routePoints.clear();

                ImageView save_route=(ImageView) findViewById(R.id.ic_save);
                save_route.setVisibility(View.GONE);
            }
        });


        if (contor==true) {
            boolean activateStartRoute = getIntent().getBooleanExtra("activateStartRoute", false);
            if (activateStartRoute) {
                // Activate the startRouteButton here
                enableStartRouteLayout();
                activateStartRoute = false;
            }
            contor=false;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(currentSelectedItem);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        currentSelectedItem = R.id.action_home;
                        startActivity(new Intent(MapsActivity.this, Acasa.class));
                        break;
                    case R.id.action_map:
                        currentSelectedItem = R.id.action_map;
                        startActivity(new Intent(MapsActivity.this, MapsActivity.class));
                        break;
                    case R.id.action_register:
                        currentSelectedItem = R.id.action_register;
                        startActivity(new Intent(MapsActivity.this, rec_traseu.class));
                        break;
                    case R.id.action_you:
                        currentSelectedItem = R.id.action_you;
                        startActivity(new Intent(MapsActivity.this, you.class));
                        break;
                }
                // Setează starea selectată pentru elementul curent
                bottomNavigationView.getMenu().findItem(currentSelectedItem).setChecked(true);
                return true;
            }
        });
    }
    // [END maps_current_place_on_map_ready]

    @SuppressLint("MissingPermission")
    private void startRouteTracking() {
        // Clear previous route and points
        mMap.clear();
        routePoints.clear();

        // Request location updates
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(90000);
        //locationRequest.setInterval(10000);// Update every 10 seconds
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates when the app is paused
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        //saveRoutesToSharedPreferences();  /// salvarea rutei intro variabila locala
    }

    private void stopRouteTracking() {
        // Oprește înregistrarea traseului
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }


    private void saveroute(List<LatLng> routePoints){

        client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        //email_global="q";

        if (routePoints.size()!= 0) {

            RoutePointsDTO routePointsDTO = new RoutePointsDTO();
            List<CoordinatesDTO> coordinatesDTOList = new ArrayList<>();
            try {
                routePointsDTO.setEmail(email_global);
                routePointsDTO.setNume_traseu(nume[0]);
                routePointsDTO.setStart_loc(nume[1]);
                routePointsDTO.setFinish_loc(nume[2]);

                for (int i = 0; i < routePoints.size(); i++) {
                    JSONObject post = new JSONObject();
                    coordinatesDTOList.add(new CoordinatesDTO(routePoints.get(i).latitude,routePoints.get(i).longitude));
                }
                routePointsDTO.setCoordinatesDTOList(coordinatesDTOList);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResource = null;
           try {
                jsonResource = objectMapper.writeValueAsString(routePointsDTO);
           } catch (JsonProcessingException e) {
               e.printStackTrace();
           }

            RequestBody body = RequestBody.create(JSON, jsonResource);
            Request request = new Request.Builder()
                    //.url("http://10.0.2.2:3000/rec")
                    .url("http://192.168.1.241:3000/rec")
                    //.url("http://72.14.201.94:3000/rec")
                    .post(body)
                    .build();

            System.out.println(body);
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

                            if (mesaj.equalsIgnoreCase("Database updated successfully")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MapsActivity.this, "Route updated successfully", Toast.LENGTH_SHORT).show();
                                        // Optionally navigate to another activity here
                                        Intent intent = new Intent(MapsActivity.this, Acasa.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else if (mesaj.equalsIgnoreCase("Database Error")) {
                                Toast.makeText(MapsActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MapsActivity.this, "Error reading response", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MapsActivity.this, "Err la trimiterea datelor", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

    public void drawRoute(List<LatLng> route) {
        if (routePolyline != null) {
            routePolyline.remove();
        }

        // Desenează traseul selectat pe hartă
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(route)
                .color(ContextCompat.getColor(MapsActivity.this, R.color.colorRoute))
                .width(10f);
        routePolyline = mMap.addPolyline(polylineOptions);

        double totalDistance = calculateTotalDistance(route);
        TextView dist=findViewById(R.id.distanta);
        dist.setText(String.format("%.2f km", totalDistance));

        // Mută camera pe traseul selectat
        if (!route.isEmpty()) {
            LatLng lastLatLng = route.get(route.size() - 1);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 15));

            // Adăugați un marker pentru a afișa distanța pe hartă
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(lastLatLng)
                    .title("Distanța totală a traseului")
                    .snippet(String.format("%.2f metri", totalDistance)); // Formatare a distanței cu două zecimale
            mMap.addMarker(markerOptions);

        }
    }

    private double calculateTotalDistance(List<LatLng> route) {
        double totalDistance = 0;

        for (int i = 1; i < route.size(); i++) {
            LatLng prevLatLng = route.get(i - 1);
            LatLng currentLatLng = route.get(i);

            float[] results = new float[1];
            Location.distanceBetween(
                    prevLatLng.latitude, prevLatLng.longitude,
                    currentLatLng.latitude, currentLatLng.longitude,
                    results);

            totalDistance += results[0];
        }
        return totalDistance;
    }

    private final LocationCallback locationCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                routePoints.add(latLng);

                if (routePolyline != null) {
                    routePolyline.remove();
                }

                // Draw the updated route polyline
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(routePoints)
                        .color(ContextCompat.getColor(MapsActivity.this, R.color.colorRoute))
                        .width(10f);
                routePolyline = mMap.addPolyline(polylineOptions);

                // Move the camera to the latest location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 24));
            }
        }
    };

    public void start (){
        enableStartRouteLayout();
    }
    public void enableStartRouteLayout(){
        RelativeLayout relLayout1 = (RelativeLayout) findViewById(R.id.relLayout1);
        RelativeLayout relLayout2 = (RelativeLayout) findViewById(R.id.relLayout2);
        RelativeLayout relLayout3 = (RelativeLayout) findViewById(R.id.relLayout3);
        RelativeLayout relLayout4 = (RelativeLayout) findViewById(R.id.relLayout4);
        ImageView start_route=(ImageView) findViewById(R.id.ic_start_route);
        ImageView save_route=(ImageView) findViewById(R.id.ic_save);
        if(relLayout2.getVisibility() == View.VISIBLE){
            relLayout1.setVisibility(View.GONE);
            relLayout2.setVisibility(View.GONE);
            relLayout3.setVisibility(View.GONE);
            relLayout4.setVisibility(View.GONE);
            save_route.setVisibility(View.GONE);
            start_route.setVisibility(View.VISIBLE);
            start_route.setY(start_route.getY()-460);
            start_route.setX(start_route.getX()+15);
            start_route.setBackgroundResource(R.drawable.rec_route);
            AnimationDrawable animationDrawable = (AnimationDrawable) start_route.getBackground();
            animationDrawable.start();
        }
        if (isRouteTrackingActive==true) {

            stopRouteTracking();// Oprește înregistrarea traseului și salvează traseul în variabilă
            if(relLayout2.getVisibility() == View.GONE){
                //relLayout1.setVisibility(View.VISIBLE);
                relLayout2.setVisibility(View.VISIBLE);
                relLayout3.setVisibility(View.VISIBLE);
                start_route.setY(start_route.getY()+483);
                start_route.setX(start_route.getX()-10);
                start_route.setBackground(null);
                save_route.setVisibility(View.VISIBLE);
                mMap.clear();
            }
        } else {
            startRouteTracking(); // Începe înregistrarea traseului
        }
        isRouteTrackingActive = !isRouteTrackingActive; // Inversează starea de înregistrare
    }

    private void enableStartLocationLayout(){

        RelativeLayout relLayout1 = (RelativeLayout) findViewById(R.id.relLayout1);
        RelativeLayout relLayout2 = (RelativeLayout) findViewById(R.id.relLayout2);
        RelativeLayout relLayout4 = (RelativeLayout) findViewById(R.id.relLayout4);
        if (relLayout1.getVisibility() == View.GONE) {
            relLayout1.setVisibility(View.VISIBLE);
            relLayout2.setY(relLayout1.getY() + relLayout1.getHeight() + 25);
            relLayout4.setVisibility(View.VISIBLE);
        }

    }

    private LocationDTO populateLocationByName(String locationString){

        try {
            LatLng locationLng;
            // Convert the placeName to LatLng directly and create the route
            Geocoder geocoder = new Geocoder(this);

                List<Address> addresses = geocoder.getFromLocationName(locationString, 1);
                Address address;

                if (addresses != null && !addresses.isEmpty()) {

                    address = addresses.get(0);
                     locationLng = new LatLng(address.getLatitude(), address.getLongitude());

                } else {
                    // Handle the case when no matching place is found
                    Toast.makeText(this, "Locația specificată nu a fost găsită.", Toast.LENGTH_SHORT).show();
                    return null;
                }


            if (locationLng != null) {

                LocationDTO locationDTO = new LocationDTO(locationLng,address);
                return locationDTO;

            } else {
                // Handle the case when no matching place is found
                Toast.makeText(this, "Locația specificată nu a fost găsită.", Toast.LENGTH_SHORT).show();
                return null;
            }
        } catch (IOException e) {
            // Handle the error case
            e.printStackTrace();
            Toast.makeText(this, "Eroare la căutarea locației.", Toast.LENGTH_SHORT).show();
            return  null;
        }

    }

    private void findPlaceByName(@NonNull String startLocationString,@NonNull String destionationLocationString) {

        if(!startLocationString.trim().isEmpty()){

            LocationDTO locationDTO = populateLocationByName(startLocationString);
            if(locationDTO != null){
                startLocationLatLng = locationDTO.getDestinationLatLng();
                EditText  searchDestination = findViewById(R.id.searchDestination);
                searchDestination.setText(locationDTO.getAddress().getAddressLine(0));
                searchDestination.setHint("Current Location");

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng( locationDTO.getDestinationLatLng().latitude, locationDTO.getDestinationLatLng().longitude), DEFAULT_ZOOM));


                MarkerOptions startLocationMarkerOptions = new MarkerOptions()
                        .position(locationDTO.getDestinationLatLng())
                        .title("Start location");
                mMap.addMarker(startLocationMarkerOptions);

            }

        }else{
            startLocationLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        }

        if( !destionationLocationString.trim().isEmpty()){


            LocationDTO locationDTO = populateLocationByName(destionationLocationString);
            if(locationDTO != null){
                destinationLatLng = locationDTO.getDestinationLatLng();
                EditText  Destination = findViewById(R.id.textDestination);
                Destination.setText(locationDTO.getAddress().getAddressLine(0));

                MarkerOptions startLocationMarkerOptions = new MarkerOptions()
                        .position(locationDTO.getDestinationLatLng())
                        .title("Destination");
                mMap.addMarker(startLocationMarkerOptions);

            }
            if(startLocationString.trim().isEmpty()) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(locationDTO.getDestinationLatLng().latitude, locationDTO.getDestinationLatLng().longitude), DEFAULT_ZOOM));
            }
        }else {
            Toast.makeText(this, "Eroare la căutarea destinatiei.", Toast.LENGTH_SHORT).show();
        }


    }

    // Call this method with the destination's latitude and longitude
    private void createRoute(@NonNull LatLng startLocationLatLng, @NonNull LatLng destinationLatLng) {
        mMap.clear();

        String apiKey = "AIzaSyDJtW1DQvT21P5_c5WCeOK3HCzz6FqJNVk";

        // Create a GeoApiContext with your Google Maps API key
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();

        // Request directions using the Directions API
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext)
                .origin(new com.google.maps.model.LatLng(startLocationLatLng.latitude, startLocationLatLng.longitude))
                .destination(new com.google.maps.model.LatLng(destinationLatLng.latitude, destinationLatLng.longitude))
                .mode(TravelMode.DRIVING); // Change this if you want different travel modes

        try {
            // Get the directions result
            DirectionsResult result = directions.await();

            // Process the result and draw the route on the map
            if (result.routes != null && result.routes.length > 0) {
                // Get the first route from the result
                DirectionsRoute route = result.routes[0];

                // Add a marker at the destination
                MarkerOptions destinationMarkerOptions = new MarkerOptions()
                        .position(destinationLatLng)
                        .title("Destination");
                mMap.addMarker(destinationMarkerOptions);
                // Decode the encoded polyline to get the list of LatLng points
                List<LatLng> decodedPath = PolyUtil.decode(route.overviewPolyline.getEncodedPath());


                // Draw the route on the map
                runOnUiThread(() -> {
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(decodedPath)
                            .color(ContextCompat.getColor(MapsActivity.this, R.color.colorRoute))
                            .width(10f);

                    Polyline polyline = mMap.addPolyline(polylineOptions);
                });

                // Calculate the distance between start and destination
                float[] distanceResults = new float[1];
                Location.distanceBetween(
                        startLocationLatLng.latitude, startLocationLatLng.longitude,
                        destinationLatLng.latitude, destinationLatLng.longitude,
                        distanceResults);

                float distance = distanceResults[0] / 1000; // Distanța în km
                TextView dist=findViewById(R.id.distanta);
                dist.setText(String.format("%.2f km", distance));

                /*// Add a marker with the distance information
                MarkerOptions distanceMarkerOptions = new MarkerOptions()
                        .position(new LatLng((startLocationLatLng.latitude + destinationLatLng.latitude) / 2, (startLocationLatLng.longitude + destinationLatLng.longitude) / 2))
                        .title("Distance")
                        .snippet(String.format("%.2f meters", distance));
                mMap.addMarker(distanceMarkerOptions);*/
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to get directions: " + e.getMessage());
        }

    }

    private void getLocationPermission() {
        /* Request location permission, so that we can get the location of the device. The result of the permission request
                                                                        is handled by a callback, onRequestPermissionsResult.*/
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }
    /**Updates the map's UI settings based on whether the user has granted location permission.   */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        if (mMap == null) {
            return;    }

        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // [END maps_current_place_update_location_ui]
    private void startLocationUpdates() {
        try {
            // Check if location permission is granted
            if (locationPermissionGranted) {
                // Request location updates using FusedLocationProviderClient
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

  /*
  try {
            if(!checkIfListOfPreferencesIsNull()){
                recordedRoutes = loadRoutesFromSharedPreferences();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Afișați mesajul de eroare sau faceți ceva altceva în funcție de nevoile dvs.
            Toast.makeText(this, "Eroare la incarcarea rutelor!", Toast.LENGTH_SHORT).show();
        }

  private List<List<LatLng>> loadRoutesFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("RoutePreferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String defValue = "";
        String json = sharedPreferences.getString("recordedRoutes", defValue);

        Type type = new TypeToken<List<List<LatLng>>>() {}.getType();
        return gson.fromJson(json, type);
    }
    private boolean checkIfListOfPreferencesIsNull() {
        SharedPreferences sharedPreferences = getSharedPreferences("RoutePreferences", Context.MODE_PRIVATE);
        String defValue = "";
        String json = sharedPreferences.getString("recordedRoutes", defValue);

        if(json.equalsIgnoreCase(defValue)){
            return true;
        }
        return false;
    }
    private void saveRoutesToSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("RoutePreferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(recordedRoutes);
        sharedPreferences.edit().putString("recordedRoutes", json).apply();
    }

    */
}