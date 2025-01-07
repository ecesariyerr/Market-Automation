package marketautomation;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Admin extends JFrame {
   // String email;
    public Admin() {
        setTitle("Admin Page");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(4, 1, 10, 10));

        JButton ordersButton = new JButton("Orders");
        JButton productsButton = new JButton("Products");
        JButton profileButton = new JButton("Profile");
        JButton customerButton = new JButton("Customers");

        ordersButton.addActionListener(e -> {
            dispose();
            new Orders(); 
        });

        productsButton.addActionListener(e -> {
            dispose();
            new ProductManagement(); 
        });

        profileButton.addActionListener(e -> {
            dispose();
            new AdminProfile(); 
            
        });

        customerButton.addActionListener(e -> {
            dispose();
            new CustomerManagement(); 
        });
        
        

        mainPanel.add(ordersButton);
        mainPanel.add(productsButton);
        mainPanel.add(profileButton);
        mainPanel.add(customerButton);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }
}
