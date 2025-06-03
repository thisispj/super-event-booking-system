package com.demo.supereventbookingsystem.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private String orderNumber;
    private LocalDateTime dateTime;
    private List<Booking> bookings;
    private double totalPrice;

    public Order(String orderNumber, LocalDateTime dateTime, List<Booking> bookings) {
        this.orderNumber = orderNumber;
        this.dateTime = dateTime;
        this.bookings = bookings;
        this.totalPrice = bookings.stream().mapToDouble(Booking::getTotalPrice).sum();
    }

    // Getters
    public String getOrderNumber() {
        return orderNumber;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}
