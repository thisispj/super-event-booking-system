package com.demo.supereventbookingsystem.model;

public class Event {
    private int eventId;
    private String title;
    private String venue;
    private String day;
    private double price;
    private int soldTickets;
    private int totalTickets;

    // Constructor(s)
    public Event(String title, String venue, String day, double price, int soldTickets, int totalTickets) {
        this.eventId = 0; // Will be set by database
        this.title = title;
        this.venue = venue;
        this.day = day;
        this.price = price;
        this.soldTickets = soldTickets;
        this.totalTickets = totalTickets;
    }

    public Event(int eventId, String title, String venue, String day, double price, int soldTickets, int totalTickets) {
        this.eventId = eventId;
        this.title = title;
        this.venue = venue;
        this.day = day;
        this.price = price;
        this.soldTickets = soldTickets;
        this.totalTickets = totalTickets;
    }

    // Getters
    public int getEventId() {
        return eventId;
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

    public int getAvailableTickets() {
        return totalTickets - soldTickets;
    }

    // Setter for eventId (used when loading from database)
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}