package com.demo.supereventbookingsystem.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items;

    public Cart() {
        this.items = new ArrayList<>();
    }

    public void addItem(Event event, int quantity) {
        items.add(new CartItem(event, quantity));
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public void clear() {
        items.clear();
    }

    public static class CartItem {
        private final Event event;
        private final int quantity;

        public CartItem(Event event, int quantity) {
            this.event = event;
            this.quantity = quantity;
        }

        public Event getEvent() {
            return event;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getTotalPrice() {
            return event.getPrice() * quantity;
        }
    }
}