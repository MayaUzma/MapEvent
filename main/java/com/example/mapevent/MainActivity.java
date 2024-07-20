package com.example.mapevent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private Button btnAboutUs, btnEventMap;
    private RecyclerView eventList;
    private EventAdapter eventAdapter;
    private List<Ict602> events;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnAboutUs = findViewById(R.id.btnAboutUs);
        btnEventMap = findViewById(R.id.btnEventMap);
        eventList = findViewById(R.id.event_list);
        ImageView logo = findViewById(R.id.logo);

        btnAboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutUs.class);
            startActivity(intent);
        });

        btnEventMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Event Hunt");

        // Initialize event list and adapter
        events = new ArrayList<>();
        eventAdapter = new EventAdapter(events, this, this);
        eventList.setLayoutManager(new LinearLayoutManager(this));
        eventList.setAdapter(eventAdapter);

        // Fetch events from the server
        fetchEvents();
    }

    private void fetchEvents() {
        String url = "http://10.20.149.100/ict602/all.php"; // URL to your server endpoint
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parse the JSON response
                        Gson gson = new Gson();
                        Ict602[] eventArray = gson.fromJson(response, Ict602[].class);

                        // Clear the current events list
                        events.clear();

                        // Add all fetched events to the events list
                        for (Ict602 event : eventArray) {
                            events.add(event);
                        }

                        // Notify the adapter about the data change
                        eventAdapter.notifyDataSetChanged();
                    }
                },
                error -> {
                    // Handle error
                    Toast.makeText(MainActivity.this, "Failed to fetch events: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onEventClick(Ict602 event) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("event_id", event.eventId);
        startActivity(intent);
    }
}
