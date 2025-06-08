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
import java.util.Map;
import java.util.HashMap;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private static final Map<String, Integer> DAY_ORDER = new HashMap<>();
    static {
        DAY_ORDER.put("Mon", 1);
        DAY_ORDER.put("Tue", 2);
        DAY_ORDER.put("Wed", 3);
        DAY_ORDER.put("Thu", 4);
        DAY_ORDER.put("Fri", 5);
        DAY_ORDER.put("Sat", 6);
        DAY_ORDER.put("Sun", 7);
    }

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
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT, preferred_name TEXT, user_type_id INTEGER DEFAULT 1, FOREIGN KEY (user_type_id) REFERENCES user_types(user_type_id))";
        String createEventsTable = "CREATE TABLE IF NOT EXISTS events (event_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, venue TEXT, day TEXT, price REAL, sold_tickets INTEGER, total_tickets INTEGER, is_disabled INTEGER DEFAULT 0, is_deleted INTEGER DEFAULT 0)";
        String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (order_number TEXT PRIMARY KEY, username TEXT, date_time TEXT, total_price REAL, FOREIGN KEY (username) REFERENCES users(username))";
        String createBookingsTable = "CREATE TABLE IF NOT EXISTS bookings (order_number TEXT, event_title TEXT, event_venue TEXT, event_day TEXT, quantity INTEGER, FOREIGN KEY (order_number) REFERENCES orders(order_number))";
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
            if (!eventExists(event)) {
                insertEvent(event);
            }
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
                    Event event = new Event(0, title, venue, day, price, soldTickets, totalTickets, false, false);
                    events.add(event);
                }
            }
        } catch (java.io.IOException | NumberFormatException e) {
            System.err.println("Error parsing events.dat: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }

    public boolean eventExists(Event event) throws SQLException { // Changed to public
        String sql = "SELECT COUNT(*) FROM events WHERE title = ? AND venue = ? AND day = ? AND is_deleted = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, event.getTitle());
            pstmt.setString(2, event.getVenue());
            pstmt.setString(3, event.getDay());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public void insertEvent(Event event) throws SQLException {
        if (!eventExists(event)) {
            String sql = "INSERT INTO events (title, venue, day, price, sold_tickets, total_tickets, is_disabled, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, event.getTitle());
                pstmt.setString(2, event.getVenue());
                pstmt.setString(3, event.getDay());
                pstmt.setDouble(4, event.getPrice());
                pstmt.setInt(5, event.getSoldTickets());
                pstmt.setInt(6, event.getTotalTickets());
                pstmt.setInt(7, event.isDisabled() ? 1 : 0);
                pstmt.setInt(8, event.isDeleted() ? 1 : 0);
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        event.setEventId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    public Event getEvent(int eventId) throws SQLException {
        String sql = "SELECT event_id, title, venue, day, price, sold_tickets, total_tickets, is_disabled, is_deleted " +
                "FROM events WHERE event_id = ? AND is_deleted = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Event(
                            rs.getInt("event_id"),
                            rs.getString("title"),
                            rs.getString("venue"),
                            rs.getString("day"),
                            rs.getDouble("price"),
                            rs.getInt("sold_tickets"),
                            rs.getInt("total_tickets"),
                            rs.getBoolean("is_disabled"),
                            rs.getBoolean("is_deleted")
                    );
                } else {
                    throw new SQLException("Event with ID " + eventId + " not found or is deleted");
                }
            }
        }
    }

    public void updateEventTickets(Event event, int additionalTickets) throws SQLException {
        String sql = "UPDATE events SET sold_tickets = sold_tickets + ? WHERE event_id = ? AND is_deleted = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, additionalTickets);
            pstmt.setInt(2, event.getEventId());
            pstmt.executeUpdate();
        }
    }

    public void updateSoldTicketsAfterCheckout(Cart cart) throws SQLException {
        for (Map.Entry<Integer, Integer> entry : cart.getItems().entrySet()) {
            Event event = getEvent(entry.getKey());
            if (event != null) {
                updateEventTickets(event, entry.getValue());
            }
        }
    }

    public String validateEventDates(Cart cart) throws SQLException {
        String currentDay = "Sat"; // Updated to current date (June 07, 2025, 09:03 PM AEST)
        int currentDayOrder = DAY_ORDER.getOrDefault(currentDay, 1);

        for (Integer eventId : cart.getItems().keySet()) {
            Event event = getEvent(eventId);
            if (event != null) {
                String eventDay = event.getDay();
                int eventDayOrder = DAY_ORDER.getOrDefault(eventDay, 1);
                if (eventDayOrder < currentDayOrder) {
                    return String.format("Cannot book %s on %s. Booking is only allowed for events from %s to Sunday.",
                            event.getTitle(), eventDay, currentDay);
                }
            }
        }
        return null; // No issues
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
        String encryptedPassword = encryptPassword(user.getPassword());
        String sql = "INSERT INTO users (username, password, preferred_name, user_type_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, encryptedPassword);
            pstmt.setString(3, user.getPreferredName());
            pstmt.setInt(4, user.getUserTypeId());
            pstmt.setString(4, user.getMemberSince().toString());
            pstmt.setString(4, user.getMemberSince().toString());
            pstmt.executeUpdate();
        }
    }

    private String encryptPassword(String password) {
        return password;
    }

    public User getUser(String username) throws SQLException {
        String sql = "SELECT username, password, preferred_name, user_type_id FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("password"), rs.getString("preferred_name"), rs.getInt("user_type_id"));
            }
        }
        return null;
    }

    public String getNextOrderNumber() throws SQLException {
        String sql = "SELECT MAX(order_number) FROM orders";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastOrderNumber = rs.getString(1);
                if (lastOrderNumber == null) {
                    return "0001";
                }
                int nextNumber = Integer.parseInt(lastOrderNumber) + 1;
                return String.format("%04d", nextNumber);
            }
        }
        return "0001";
    }

    public void saveOrder(Order order, String username) throws SQLException {
        String sqlOrder = "INSERT INTO orders (order_number, username, date_time, total_price) VALUES (?, ?, ?, ?)";
        String sqlBooking = "INSERT INTO bookings (order_number, event_title, event_venue, event_day, quantity) VALUES (?, ?, ?, ?, ?)";

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

    public List<Order> getUserOrders(String username) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sqlOrders = "SELECT order_number, date_time, total_price FROM orders WHERE username = ? ORDER BY date_time DESC";
        try (PreparedStatement pstmtOrders = connection.prepareStatement(sqlOrders)) {
            pstmtOrders.setString(1, username);
            ResultSet rsOrders = pstmtOrders.executeQuery();
            while (rsOrders.next()) {
                String orderNumber = rsOrders.getString("order_number");
                LocalDateTime dateTime = LocalDateTime.parse(rsOrders.getString("date_time"));
                double totalPrice = rsOrders.getDouble("total_price");

                List<Booking> bookings = new ArrayList<>();
                String sqlBookings = "SELECT event_title, event_venue, event_day, quantity FROM bookings WHERE order_number = ?";
                try (PreparedStatement pstmtBookings = connection.prepareStatement(sqlBookings)) {
                    pstmtBookings.setString(1, orderNumber);
                    ResultSet rsBookings = pstmtBookings.executeQuery();
                    while (rsBookings.next()) {
                        String eventTitle = rsBookings.getString("event_title");
                        String eventVenue = rsBookings.getString("event_venue");
                        String eventDay = rsBookings.getString("event_day");
                        int quantity = rsBookings.getInt("quantity");

                        Event event = new Event(0, eventTitle, eventVenue, eventDay, 0.0, 0, 0, false, false);
                        Booking booking = new Booking(event, quantity);
                        bookings.add(booking);
                    }
                }

                Order order = new Order(orderNumber, dateTime, bookings, totalPrice);
                orders.add(order);
            }
        }
        return orders;
    }

    public void addToCart(String username, Event event, int quantity) throws SQLException {
        int availableSeats = event.getAvailableTickets();
        if (quantity > availableSeats) {
            throw new SQLException(String.format("Not enough seats available for %s on %s. Requested: %d, Available: %d",
                    event.getTitle(), event.getDay(), quantity, availableSeats));
        }

        Cart tempCart = new Cart();
        tempCart.addItem(event, quantity);
        String dateValidationError = validateEventDates(tempCart);
        if (dateValidationError != null) {
            throw new SQLException(dateValidationError);
        }

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

    public List<Event> getEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT event_id, title, venue, day, price, sold_tickets, total_tickets, is_disabled, is_deleted FROM events WHERE is_disabled = 0 AND is_deleted = 0";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                events.add(new Event(
                        rs.getInt("event_id"),
                        rs.getString("title"),
                        rs.getString("venue"),
                        rs.getString("day"),
                        rs.getDouble("price"),
                        rs.getInt("sold_tickets"),
                        rs.getInt("total_tickets"),
                        rs.getInt("is_disabled") == 1,
                        rs.getInt("is_deleted") == 1
                ));
            }
        }
        return events;
    }

    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT event_id, title, venue, day, price, sold_tickets, total_tickets, is_disabled, is_deleted FROM events WHERE is_deleted = 0";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                events.add(new Event(
                        rs.getInt("event_id"),
                        rs.getString("title"),
                        rs.getString("venue"),
                        rs.getString("day"),
                        rs.getDouble("price"),
                        rs.getInt("sold_tickets"),
                        rs.getInt("total_tickets"),
                        rs.getInt("is_disabled") == 1,
                        rs.getInt("is_deleted") == 1
                ));
            }
        }
        return events;
    }

    public void updateEventStatus(int eventId, boolean isDisabled) throws SQLException {
        String sql = "UPDATE events SET is_disabled = ? WHERE event_id = ? AND is_deleted = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, isDisabled ? 1 : 0);
            pstmt.setInt(2, eventId);
            pstmt.executeUpdate();
        }
    }

    public void softDeleteEvent(int eventId) throws SQLException {
        String sql = "UPDATE events SET is_deleted = 1 WHERE event_id = ? AND is_deleted = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Event not found or already deleted.");
            }
        }
    }

    public void updateEvent(Event event) throws SQLException {
        String sql = "UPDATE events SET title = ?, venue = ?, day = ?, price = ?, sold_tickets = ?, total_tickets = ?, is_disabled = ?, is_deleted = ? WHERE event_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, event.getTitle());
            pstmt.setString(2, event.getVenue());
            pstmt.setString(3, event.getDay());
            pstmt.setDouble(4, event.getPrice());
            pstmt.setInt(6, event.getSoldTickets());
            pstmt.setInt(6, event.getTotalTickets());
            pstmt.setBoolean(7, event.isDisabled());
            pstmt.setBoolean(8, event.isDeleted());
            pstmt.setInt(9, event.getEventId());

            // Execute update
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No rows updated. Event ID " + event.getEventId() + " may not exist.");
            }
        } catch (SQLException e) {
            // Log the error (assuming a logger is available, e.g., java.util.logging)
            System.err.println("SQL Error while updating event: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be handled by the caller
        } catch (Exception e) {
            // Handle unexpected exceptions (e.g., null pointer if event is invalid)
            System.err.println("Unexpected error while updating event: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Unexpected error during event update: " + e.getMessage(), e);
        }
    }

    public Event getEventById(int eventId) throws SQLException {
        String sql = "SELECT * FROM events WHERE event_id = ? AND is_deleted = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Event(
                            rs.getInt("event_id"),
                            rs.getString("title"),
                            rs.getString("venue"),
                            rs.getString("day"),
                            rs.getDouble("price"),
                            rs.getInt("sold_tickets"),
                            rs.getInt("total_tickets"),
                            rs.getBoolean("is_disabled"),
                            rs.getBoolean("is_deleted")
                    );
                }
                return null; // Event not found or deleted
            }
        } catch (SQLException e) {
            // Log SQL-related errors (e.g., connection failure, invalid query)
            System.err.println("SQL Error while fetching event with ID " + eventId + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be handled by the caller
        } catch (Exception e) {
            // Handle unexpected exceptions (e.g., DriverManager issues, resource initialization)
            System.err.println("Unexpected error while fetching event with ID " + eventId + ": " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Unexpected error while fetching event: " + e.getMessage(), e);
        }
    }

    public List<Order> getOrdersByUsername(String username) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.order_number, o.date_time, o.total_price, b.event_id, b.quantity " +
                "FROM orders o " +
                "JOIN bookings b ON o.order_number = b.order_number " +
                "JOIN users u ON o.username = u.username " +
                "WHERE u.username = ? AND u.user_type_id = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                Map<String, Order> orderMap = new HashMap<>();
                while (rs.next()) {
                    String orderNumber = rs.getString("order_number");
                    Order order = orderMap.getOrDefault(orderNumber, new Order(orderNumber, rs.getObject("date_time", LocalDateTime.class), new ArrayList<>(), 0.0));
                    int eventId = rs.getInt("event_id");
                    try {
                        Event event = getEvent(eventId);
                        order.getBookings().add(new Booking(event, rs.getInt("quantity")));
                    } catch (SQLException e) {
                        System.err.println("Skipping booking for order " + orderNumber + " due to missing event ID " + eventId + ": " + e.getMessage());
                        continue; // Skip this booking if the event is not found
                    }
                    order.setTotalPrice(rs.getDouble("total_price")); // Update total if needed
                    orderMap.put(orderNumber, order);
                }
                orders.addAll(orderMap.values());
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve orders for " + username + ": " + e.getMessage(), e);
        }
        return orders;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, preferred_name, created_at, user_type_id FROM users";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        rs.getString("preferred_name"),
                        rs.getTimestamp("created_at"),
                        rs.getInt("user_type_id")
                ));
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve users: " + e.getMessage(), e);
        }
        return users;
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT username, preferred_name, created_at, user_type_id FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("preferred_name"),
                            rs.getTimestamp("created_at"),
                            rs.getInt("user_type_id")
                    );
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve user " + username + ": " + e.getMessage(), e);
        }
    }

}