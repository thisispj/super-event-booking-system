package com.demo.supereventbookingsystem.model;

public class Event {
    private String title;
    private String venue;
    private String day;
    private double price;
    private int soldTickets;
    private int totalTickets;

    public Event(String title, String venue, String day, double price, int soldTickets, int totalTickets) {
        this.title = title;
        this.venue = venue;
        this.day = day;
        this.price = price;
        this.soldTickets = soldTickets;
        this.totalTickets = totalTickets;
    }

    // Getters and setters
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