package com.demo.supereventbookingsystem.model;

public class Event {
    private int eventId; // Unique ID for the event, set by SQLite AUTOINCREMENT
    private String title;
    private String venue;
    private String day;
    private double price;
    private int soldTickets;
    private int totalTickets;

    // Constructor for creating new events (eventId will be set by the database)
    public Event(String title, String venue, String day, double price, int soldTickets, int totalTickets) {
        this.title = title;
        this.venue = venue;
        this.day = day;
        this.eventId = 0; // Default value; will be set by the database
        this.price = price;
        this.soldTickets = soldTickets;
        this.totalTickets = totalTickets;
    }

    // Constructor for loading from database (with eventId)
    public Event(int eventId, String title, String venue, String day, double price, int soldTickets, int totalTickets) {
        this.eventId = eventId;
        this.title = title;
        this.venue = venue;
        this.day = day;
        this.price = price;
        this.soldTickets = soldTickets;
        this.totalTickets = totalTickets;
    }

    // Getters and setters
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getVenue() {
        return venue;
    }

    public String getDay() {
        return day;
    }

    public double getPrice() {
        return price;
    }

    public int getSoldTickets() {
        return soldTickets;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setSoldTickets(int soldTickets) {
        this.soldTickets = soldTickets;
    }

    public int getAvailableTickets() {
        return totalTickets - soldTickets;
    }

    @Override
    public String toString() {
        return title + " at " + venue + " on " + day + " ($" + price + ")";
    }
}