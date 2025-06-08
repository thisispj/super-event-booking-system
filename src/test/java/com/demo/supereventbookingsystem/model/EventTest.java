package com.demo.supereventbookingsystem.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {

    @Test
    void testIsAvailable_SufficientTickets() {
        Event event = new Event(1, "Concert", "VenueA", "2025-06-09", 50.0, 50, 100, false, false);
        int requestedTickets = 20;
        boolean isAvailable = event.isAvailable(requestedTickets);
        assertTrue(isAvailable, "Should be available with 50 remaining tickets");
    }

    @Test
    void testIsAvailable_NoTicketsAvailable() {
        Event event = new Event(1, "Concert", "VenueA", "2025-06-09", 50.0, 100, 100, false, false);
        int requestedTickets = 1;
        boolean isAvailable = event.isAvailable(requestedTickets);
        assertFalse(isAvailable, "Should not be available when sold out");
    }

    @Test
    void testIsAvailable_NegativeRequestedTickets() {
        Event event = new Event(1, "Concert", "VenueA", "2025-06-09", 50.0, 50, 100, false, false);
        int requestedTickets = -5;
        boolean isAvailable = event.isAvailable(requestedTickets);
        assertFalse(isAvailable, "Should not be available for negative ticket requests");
    }

    @Test
    void testIsAvailable_DeletedEvent() {
        Event event = new Event(1, "Concert", "VenueA", "2025-06-09", 50.0, 50, 100, false, true);
        int requestedTickets = 20;
        boolean isAvailable = event.isAvailable(requestedTickets);
        assertFalse(isAvailable, "Should not be available for deleted events");
    }

    @Test
    void testIsAvailable_DisabledEvent() {
        Event event = new Event(1, "Concert", "VenueA", "2025-06-09", 50.0, 50, 100, true, false);
        int requestedTickets = 20;
        boolean isAvailable = event.isAvailable(requestedTickets);
        assertFalse(isAvailable, "Should not be available for disabled events");
    }
}