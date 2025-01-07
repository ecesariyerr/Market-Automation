package marketautomation;

import java.awt.BorderLayout;
import javax.swing.*;
import java.sql.*;

public class Checkout extends JFrame {

    public Checkout() {
        setTitle("Checkout Page");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);

        
        JButton checkoutButton = new JButton("Confirm Order");
        checkoutButton.addActionListener(e -> processCheckout());
        add(checkoutButton, BorderLayout.CENTER);

        setVisible(true);
    }

    private void processCheckout() {
        
        try (Connection conn = DatabaseOperations.connect()) {
            String query = "INSERT INTO orders (user_id, product_id, quantity, total_price, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1); 
            stmt.setInt(2, 1); 
            stmt.setInt(3, 2); 
            stmt.setDouble(4, 100.00); 
            stmt.setString(5, "Pending"); 

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Order placed successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error processing order: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
