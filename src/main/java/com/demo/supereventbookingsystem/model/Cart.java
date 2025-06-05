package com.demo.supereventbookingsystem.model;

import com.demo.supereventbookingsystem.dao.DatabaseManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Cart {
    private Map<Integer, Integer> items; // Map of eventId to quantity

    public Cart() {
        this.items = new HashMap<>();
    }

    public void addItem(Event event, int quantity) {
        items.merge(event.getEventId(), quantity, Integer::sum);
    }

    public Map<Integer, Integer> getItems() {
        return new HashMap<>(items); // Return a copy to prevent external modification
    }

    public void clear() {
        items.clear();
    }

    public double getTotalPrice() {
        double total = 0.0;
        try {
            for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
                Event event = DatabaseManager.getInstance().getEvent(entry.getKey());
                if (event != null) {
                    total += event.getPrice() * entry.getValue();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total price: " + e.getMessage());
            e.printStackTrace();
        }
        return total;
    }

    public int getItemCount() {
        return items.size();
    }
}