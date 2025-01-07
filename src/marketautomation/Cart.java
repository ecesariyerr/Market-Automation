package marketautomation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Cart extends JFrame {
    private static Cart instance;
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private int userId;

    public Cart(int userId) {
        this.userId = userId;


        setTitle("My Cart");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cartTableModel = new DefaultTableModel(new String[]{"Order Id","Product Name", "Quantity", "Price", "Total"}, 0);
        cartTable = new JTable(cartTableModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());

        JButton confirmOrderButton = new JButton("Confirm Order");
        confirmOrderButton.addActionListener(e -> confirmOrder());
        bottomPanel.add(confirmOrderButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        bottomPanel.add(deleteButton);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateSelectedProduct());
        bottomPanel.add(updateButton);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            dispose();
            new Customer(userId);
            
        });
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
        
        loadOrders();
        setVisible(true);
    }

      public static Cart getInstance(int userId) {
          if (instance == null || instance.userId != userId) {
            instance = new Cart(userId);
        }
        return instance;
    }


    public void addProductToCart(String productName, double price, int quantity) {
        double total = price * quantity;
        cartTableModel.addRow(new Object[]{productName, quantity, price, total});
    }

    private void deleteSelectedProduct() {
        int selectedRow = cartTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            String query = "DELETE FROM orders WHERE id = ?";
            try (Connection conn = DatabaseOperations.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
                int orderId= (int) cartTableModel.getValueAt(selectedRow, 0);
                stmt.setInt(1, orderId);
                stmt.executeUpdate();
                loadOrders();
            } catch (SQLException e) {
                 JOptionPane.showMessageDialog(this, "Error confirming order: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            JOptionPane.showMessageDialog(this, "Product removed successfully.");
        }
    }

    private void updateSelectedProduct() {
        int selectedRow = cartTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String quantityInput = JOptionPane.showInputDialog("Enter new quantity:");
            if (quantityInput != null && !quantityInput.isEmpty()) {
                int newQuantity = Integer.parseInt(quantityInput);
                
                if (newQuantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int orderId= (int) cartTableModel.getValueAt(selectedRow, 0);
                double price = Double.parseDouble(cartTableModel.getValueAt(selectedRow, 3).toString());
                double newTotal = price * newQuantity;

                String query = "UPDATE orders SET quantity = ?, total_price = ?  WHERE id = ?";
                try (Connection conn = DatabaseOperations.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, newQuantity);
                    stmt.setDouble(2, newTotal);
                    stmt.setInt(3, orderId);
                    stmt.executeUpdate();
                    loadOrders();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error confirming order: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }

                JOptionPane.showMessageDialog(this, "Product updated successfully.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity entered!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmOrder() {
    int selectedRow = cartTable.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to confirm!", "Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (cartTableModel.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Cart is empty!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    String query = "UPDATE orders SET status = ? WHERE id = ?";
    try (Connection conn = DatabaseOperations.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
        int orderId = (int) cartTableModel.getValueAt(selectedRow, 0);
        stmt.setString(1, "Pending");
        stmt.setInt(2, orderId);
        stmt.executeUpdate();
        loadOrders();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error confirming order: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

    
    private void loadOrders() {
        try (Connection conn = DatabaseOperations.connect()) {
            String query = "SELECT orders.id, orders.product_id, orders.user_id, users.name AS user_name, products.price, products.name AS product_name, " +
                    "orders.quantity, orders.total_price, orders.status " +
                    "FROM orders " +
                    "JOIN users ON orders.user_id = users.id " +
                    "JOIN products ON orders.product_id = products.id WHERE status = 'user-pending'";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            cartTableModel.setRowCount(0); 
            while (rs.next()) {
                int userIdd = rs.getInt("user_id");
                if(userId == userIdd){
                    String productName = rs.getString("product_name");
                    int orderId = rs.getInt("id");
                    int quantity = rs.getInt("quantity");
                    double totalPrice = rs.getDouble("total_price");
                    double price = rs.getDouble("price");

                    cartTableModel.addRow(new Object[]{orderId, productName, quantity, price, totalPrice});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
