package com.demo.supereventbookingsystem.model;

public class Event {
    private int eventId;
    private String title;
    private String venue;
    private String day;
    private double price;
    private int soldTickets;
    private int totalTickets;
    private boolean isDisabled;
    private boolean isDeleted;

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Event(int eventId, String title, String venue, String day, double price, int soldTickets, int totalTickets, boolean isDisabled, boolean isDeleted) {
        this.eventId = eventId;
        this.title = title;
        this.venue = venue;
        this.day = day;
        this.price = price;
        this.soldTickets = soldTickets;
        this.totalTickets = totalTickets;
        this.isDisabled = isDisabled;
        this.isDeleted = isDeleted;
    }

    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getVenue() { return venue; }
    public String getDay() { return day; }
    public double getPrice() { return price; }
    public int getSoldTickets() { return soldTickets; }
    public int getTotalTickets() { return totalTickets; }
    public boolean isDisabled() { return isDisabled; }
    public void setDisabled(boolean disabled) { isDisabled = disabled; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public int getAvailableTickets() {
        return Math.max(0, totalTickets - soldTickets); // Ensure non-negative value
    }

    public boolean isAvailable(int requestedTickets) {
        if (isDeleted || isDisabled) {
            return false;
        }
        int remainingTickets = totalTickets - soldTickets;
        return requestedTickets > 0 && requestedTickets <= remainingTickets;
    }

    @Override
    public String toString() {
        return title + " at " + venue + " on " + day;
    }
}