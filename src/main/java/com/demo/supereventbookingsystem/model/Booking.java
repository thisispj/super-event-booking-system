package com.demo.supereventbookingsystem.model;

public class Booking {
    private Event event;
    private int quantity;

    public Booking(Event event, int quantity) {
        this.event = event;
        this.quantity = quantity;
    }

    // Getters and setters
    public Event getEvent() {
        return event;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return event.getPrice() * quantity;
    }
}
