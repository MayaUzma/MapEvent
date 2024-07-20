package com.example.mapevent;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mapevent.databinding.ActivityMaps3Binding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMaps3Binding binding;

    private LatLng centerlocation;
    private final String URL = "http://10.20.149.100/ict602/all.php";
    RequestQueue requestQueue;
    Gson gson;
    Ict602[] ict602s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMaps3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gson = new GsonBuilder().create();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        centerlocation = new LatLng(3.0, 101.0);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerlocation, 6));
        enableMyLocation();
        sendRequest();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                showEventDetailsDialog(marker);
                return true;
            }
        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 200);
        }
    }

    public void sendRequest() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ict602s = gson.fromJson(response, Ict602[].class);

                Log.d("Maklumat", "Number of Maklumat Data Point : " + ict602s.length);

                if (ict602s.length < 1) {
                    Toast.makeText(getApplicationContext(), "Problem retrieving JSON data", Toast.LENGTH_LONG).show();
                    return;
                }

                for (Ict602 info : ict602s) {
                    Double lat = Double.parseDouble(info.lat);
                    Double longi = Double.parseDouble(info.longi);
                    String title = info.eventName;
                    String snippet = info.eventStatus;

                    MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, longi))
                            .title(title)
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

                    Marker mapMarker = mMap.addMarker(marker);
                    mapMarker.setTag(info);
                }

                // Call filterEventOnMap after the data has been fetched
                String eventId = getIntent().getStringExtra("event_id");
                if (eventId != null) {
                    filterEventOnMap(eventId);
                }
            }
        }, error -> {
            String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
        });

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest);
    }

    private void filterEventOnMap(String eventId) {
        for (Ict602 info : ict602s) {
            if (info.eventId.equals(eventId)) {
                Double lat = Double.parseDouble(info.lat);
                Double longi = Double.parseDouble(info.longi);
                LatLng position = new LatLng(lat, longi);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
                break;
            }
        }
    }

    private void showEventDetailsDialog(Marker marker) {
        Ict602 event = (Ict602) marker.getTag();
        if (event != null) {
            // Create a LinearLayout to hold the TextViews
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(32, 32, 32, 32);

            // Add TextViews for each event detail
            TextView locationTextView = new TextView(this);
            locationTextView.setText("Location: " + event.eventLocation);
            layout.addView(locationTextView);

            TextView dateTextView = new TextView(this);
            dateTextView.setText("Date: " + event.eventDate);
            layout.addView(dateTextView);

            TextView statusTextView = new TextView(this);
            statusTextView.setText("Status: " + event.eventStatus);
            layout.addView(statusTextView);

            TextView entryTextView = new TextView(this);
            entryTextView.setText("Entry: " + event.entry);
            layout.addView(entryTextView);

            // Create a SpannableString for the program link
            SpannableString spannableString = new SpannableString("Program Link: " + event.program_link);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull android.view.View widget) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.program_link));
                    startActivity(browserIntent);
                }
            };
            spannableString.setSpan(clickableSpan, 14, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextView programLinkTextView = new TextView(this);
            programLinkTextView.setText(spannableString);
            programLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
            layout.addView(programLinkTextView);

            // Add the layout to a ScrollView
            ScrollView scrollView = new ScrollView(this);
            scrollView.addView(layout);

            // Show dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(event.eventName)
                    .setView(scrollView)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
