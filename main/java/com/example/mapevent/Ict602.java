package com.example.mapevent;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ict602 {

    @SerializedName("event_id")
    @Expose
    public String eventId;
    @SerializedName("event_name")
    @Expose
    public String eventName;
    @SerializedName("event_location")
    @Expose
    public String eventLocation;
    @SerializedName("event_date")
    @Expose
    public String eventDate;
    @SerializedName("event_status")
    @Expose
    public String eventStatus;
    @SerializedName("lat")
    @Expose
    public String lat;
    @SerializedName("longi")
    @Expose
    public String longi;
    @SerializedName("entry")
    @Expose
    public String entry;
    public String program_link;
}
