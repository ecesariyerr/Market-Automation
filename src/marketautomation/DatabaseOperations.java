package marketautomation;

import java.sql.*;

public class DatabaseOperations {

    
    private static final String DB_URL = "jdbc:sqlite:db_"; 

    
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL); 
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
        return conn;
    }

    
    public static void initializeDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,   
                surname TEXT,                            
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                photo_path TEXT                
            );
        """;
        
        String createProductsTable = """
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                price REAL NOT NULL,
                unit TEXT,
                stock INTEGER NOT NULL
            );
        """;

        String createOrdersTable = """
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                total_price REAL NOT NULL,
                status TEXT DEFAULT 'Pending',
                FOREIGN KEY (user_id) REFERENCES users (id),
                FOREIGN KEY (product_id) REFERENCES products (id)
            );
        """;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createProductsTable);
            stmt.execute(createOrdersTable);
            System.out.println("Tables created successfully.");
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }
    
    public static void updateUserProfile(int userId, String name, String password, String photoPath) {
        String query = "UPDATE users SET name = ?, password = ?, photo_path = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setString(3, photoPath);
            stmt.setInt(4, userId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Profile updated successfully!");
            } else {
                System.out.println("No profile updated. Check userId.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating user profile: " + e.getMessage());
        }
    }


    
    public static void addOrder(int userId, int productId, int quantity, double totalPrice) {
        String query = "INSERT INTO orders (user_id, product_id, quantity, total_price, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, totalPrice);
            stmt.setString(5, "Pending"); 
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding order: " + e.getMessage());
        }
    }

    
    public static void updateOrderStatus(int orderId, String newStatus) {
        String query = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating order status: " + e.getMessage());
        }
    }

    
    public static ResultSet getAllOrders() {
    String query = "SELECT orders.id, users.name, products.name AS product_name, orders.quantity, orders.total_price, orders.status " +
                       "FROM orders " +
                       "JOIN users ON orders.user_id = users.id " +
                       "JOIN products ON orders.product_id = products.id";
        try {
            Connection conn = connect();
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query); 
        } catch (SQLException e) {
            System.out.println("Error fetching orders: " + e.getMessage());
            return null;
        }
    }
}
