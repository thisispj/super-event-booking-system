package com.demo.supereventbookingsystem.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class Order {
    private String orderNumber;
    private LocalDateTime dateTime;
    private List<Booking> bookings;
    private double totalPrice;

    // Constructor for creating a new order (e.g., during checkout)
    public Order(String orderNumber, LocalDateTime dateTime, List<Booking> bookings) {
        this.orderNumber = orderNumber;
        this.dateTime = dateTime;
        this.bookings = bookings;
        this.totalPrice = calculateTotalPrice(); // Calculate for new orders
    }

    // Constructor for loading from database (avoids recalculation)
    public Order(String orderNumber, LocalDateTime dateTime, List<Booking> bookings, double totalPrice) {
        this.orderNumber = orderNumber;
        this.dateTime = dateTime;
        this.bookings = bookings;
        this.totalPrice = totalPrice; // Use the value from the database
    }

    double calculateTotalPrice() {
        return bookings.stream()
                .mapToDouble(booking -> booking.getEvent().getPrice() * booking.getQuantity())
                .sum();
    }

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

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    // New method to format bookings for TableView display
    public String getBookingsSummary() {
        return bookings.stream()
                .map(booking -> booking.getEvent().getTitle() + " (" + booking.getQuantity() + ")")
                .collect(Collectors.joining(", "));
    }
}