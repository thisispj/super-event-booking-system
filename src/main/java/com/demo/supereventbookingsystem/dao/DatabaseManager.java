package com.demo.supereventbookingsystem.dao;

import com.demo.supereventbookingsystem.model.Event;
import com.demo.supereventbookingsystem.model.User;
import com.demo.supereventbookingsystem.model.Order;
import com.demo.supereventbookingsystem.model.Booking;
import com.demo.supereventbookingsystem.model.Cart;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:events.db");
            createTables();
            loadEventsFromFile("src/main/resources/com/demo/supereventbookingsystem/events.dat");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() throws SQLException {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT, preferred_name TEXT)";
        String createEventsTable = "CREATE TABLE IF NOT EXISTS events (event_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, venue TEXT, day TEXT, price REAL, sold_tickets INTEGER, total_tickets INTEGER)";
        String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (order_number TEXT PRIMARY KEY, username TEXT, date_time TEXT, total_price REAL)";
        String createBookingsTable = "CREATE TABLE IF NOT EXISTS bookings (order_number TEXT, event_title TEXT, event_venue TEXT, event_day TEXT, quantity INTEGER)";
        String createCartTable = "CREATE TABLE IF NOT EXISTS cart (username TEXT NOT NULL, event_id INTEGER NOT NULL, quantity INTEGER NOT NULL, total_price REAL NOT NULL, FOREIGN KEY (username) REFERENCES users(username), FOREIGN KEY (event_id) REFERENCES events(event_id))";
        String createIndexSql = "CREATE INDEX IF NOT EXISTS idx_cart_event_id ON cart (event_id)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createEventsTable);
            stmt.execute(createOrdersTable);
            stmt.execute(createBookingsTable);
            stmt.execute(createCartTable);
            stmt.execute(createIndexSql);
        }
    }

    public void loadEventsFromFile(String filePath) throws SQLException {
        List<Event> events = parseEventsFile(filePath);
        for (Event event : events) {
            insertEvent(event);
        }
    }

    private List<Event> parseEventsFile(String filePath) {
        List<Event> events = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length == 6) {
                    String title = parts[0].trim();
                    String venue = parts[1].trim();
                    String day = parts[2].trim();
                    double price = Double.parseDouble(parts[3].trim());
                    int soldTickets = Integer.parseInt(parts[4].trim());
                    int totalTickets = Integer.parseInt(parts[5].trim());
                    Event event = new Event(title, venue, day, price, soldTickets, totalTickets);
                    events.add(event);
                }
            }
        } catch (java.io.IOException | NumberFormatException e) {
            System.err.println("Error parsing events.dat: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }

    public void insertEvent(Event event) throws SQLException {
        String sql = "INSERT INTO events (title, venue, day, price, sold_tickets, total_tickets) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, event.getTitle());
            pstmt.setString(2, event.getVenue());
            pstmt.setString(3, event.getDay());
            pstmt.setDouble(4, event.getPrice());
            pstmt.setInt(5, event.getSoldTickets());
            pstmt.setInt(6, event.getTotalTickets());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setEventId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT event_id, title, venue, day, price, sold_tickets, total_tickets FROM events";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Event event = new Event(
                        rs.getInt("event_id"),
                        rs.getString("title"),
                        rs.getString("venue"),
                        rs.getString("day"),
                        rs.getDouble("price"),
                        rs.getInt("sold_tickets"),
                        rs.getInt("total_tickets")
                );
                events.add(event);
            }
        }
        return events;
    }

    public Event getEvent(int eventId) throws SQLException {
        String sql = "SELECT * FROM events WHERE event_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Event(
                        rs.getInt("event_id"),
                        rs.getString("title"),
                        rs.getString("venue"),
                        rs.getString("day"),
                        rs.getDouble("price"),
                        rs.getInt("sold_tickets"),
                        rs.getInt("total_tickets")
                );
            }
        }
        return null;
    }

    public void updateEventTickets(Event event, int additionalTickets) throws SQLException {
        String sql = "UPDATE events SET sold_tickets = sold_tickets + ? WHERE event_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, additionalTickets);
            pstmt.setInt(2, event.getEventId());
            pstmt.executeUpdate();
        }
    }

    public boolean validateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            return pstmt.executeQuery().next();
        }
    }

    public void saveUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, preferred_name) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getPreferredName());
            pstmt.executeUpdate();
        }
    }

    public User getUser(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("password"), rs.getString("preferred_name"));
            }
        }
        return null;
    }

    public void saveOrder(Order order, String username) throws SQLException {
        String sqlOrder = "INSERT INTO orders (order_number, username, date_time, total_price) VALUES (?, ?, ?, ?)";
        String sqlBooking = "INSERT INTO bookings (order_number, event_title, event_venue TEXT, event_day TEXT, quantity) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmtOrder = connection.prepareStatement(sqlOrder);
             PreparedStatement pstmtBooking = connection.prepareStatement(sqlBooking)) {
            pstmtOrder.setString(1, order.getOrderNumber());
            pstmtOrder.setString(2, username);
            pstmtOrder.setString(3, order.getDateTime().toString());
            pstmtOrder.setDouble(4, order.getTotalPrice());
            pstmtOrder.executeUpdate();

            for (Booking booking : order.getBookings()) {
                pstmtBooking.setString(1, order.getOrderNumber());
                pstmtBooking.setString(2, booking.getEvent().getTitle());
                pstmtBooking.setString(3, booking.getEvent().getVenue());
                pstmtBooking.setString(4, booking.getEvent().getDay());
                pstmtBooking.setInt(5, booking.getQuantity());
                pstmtBooking.executeUpdate();
            }
        }
    }

    public void addToCart(String username, Event event, int quantity) throws SQLException {
        double totalPrice = event.getPrice() * quantity;
        String sql = "INSERT INTO cart (username, event_id, quantity, total_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, event.getEventId());
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, totalPrice);
            pstmt.executeUpdate();
        }
    }

    public Cart getCartItems(String username) throws SQLException {
        Cart cart = new Cart();
        String sql = "SELECT event_id, quantity, total_price FROM cart WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int eventId = rs.getInt("event_id");
                int quantity = rs.getInt("quantity");
                Event event = getEvent(eventId);
                if (event != null) {
                    cart.addItem(event, quantity);
                }
            }
        }
        return cart;
    }

    public void clearCart(String username) throws SQLException {
        String sql = "DELETE FROM cart WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Database connection closed.");
        }
    }
}