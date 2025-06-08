package com.demo.supereventbookingsystem.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    @Test
    void testCalculateTotalPrice_MultipleBookings() {
        List<Booking> bookings = new ArrayList<>();
        Event event1 = new Event(1, "Event1", "Venue1", "2025-06-09", 50.0, 0, 100, false, false);
        Event event2 = new Event(2, "Event2", "Venue2", "2025-06-10", 30.0, 0, 100, false, false);
        bookings.add(new Booking(event1, 2)); // 2 tickets at $50 = $100
        bookings.add(new Booking(event2, 3)); // 3 tickets at $30 = $90
        Order order = new Order("ORD123", LocalDateTime.now(), bookings);

        double totalPrice = order.calculateTotalPrice();

        assertEquals(190.0, totalPrice, 0.01); // $100 + $90 = $190
    }

    @Test
    void testGetBookingsSummary_MultipleBookings() {
        List<Booking> bookings = new ArrayList<>();
        Event event1 = new Event(1, "Event1", "Venue1", "2025-06-09", 50.0, 0, 100, false, false);
        Event event2 = new Event(2, "Event2", "Venue2", "2025-06-10", 30.0, 0, 100, false, false);
        bookings.add(new Booking(event1, 2));
        bookings.add(new Booking(event2, 3));
        Order order = new Order("ORD123", LocalDateTime.now(), bookings);

        String summary = order.getBookingsSummary();

        assertEquals("Event1 (2), Event2 (3)", summary);
    }

    @Test
    void testGetBookingsSummary_EmptyBookings() {
        List<Booking> bookings = new ArrayList<>();
        Order order = new Order("ORD123", LocalDateTime.now(), bookings);

        String summary = order.getBookingsSummary();

        assertEquals("", summary); // Empty string when no bookings
    }
}