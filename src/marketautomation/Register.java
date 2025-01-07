package marketautomation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Register extends JFrame {
    public Register() {
        setTitle("Register Page");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

       
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(50, 20, 100, 25);
        add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 20, 200, 25);
        add(nameField);

        
        JLabel surnameLabel = new JLabel("Surname:");
        surnameLabel.setBounds(50, 60, 100, 25);
        add(surnameLabel);

        JTextField surnameField = new JTextField();
        surnameField.setBounds(150, 60, 200, 25);
        add(surnameField);

       
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(50, 100, 100, 25);
        add(emailLabel);

        JTextField emailField = new JTextField();
        emailField.setBounds(150, 100, 200, 25);
        add(emailField);

        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 140, 100, 25);
        add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(150, 140, 200, 25);
        add(passwordField);

       
        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setBounds(50, 180, 100, 25);
        add(roleLabel);

        JRadioButton adminRadioButton = new JRadioButton("Admin");
        adminRadioButton.setBounds(150, 180, 80, 25);
        add(adminRadioButton);

        JRadioButton customerRadioButton = new JRadioButton("Customer");
        customerRadioButton.setBounds(240, 180, 100, 25);
        add(customerRadioButton);

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(adminRadioButton);
        roleGroup.add(customerRadioButton);

        
        JButton registerButton = new JButton("Register");
        registerButton.setBounds(200, 220, 100, 25);
        add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String surname = surnameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String role = adminRadioButton.isSelected() ? "admin" : "customer";

                
                if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "All fields must be filled!");
                    return;
                }

                
                try (Connection conn = DatabaseOperations.connect()) {
                    String query = "INSERT INTO users (name, surname, email, password, role) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.setString(1, name);
                    preparedStatement.setString(2, surname);
                    preparedStatement.setString(3, email);
                    preparedStatement.setString(4, password);
                    preparedStatement.setString(5, role);
                    preparedStatement.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Registration successful!");
                    dispose();
                    new LoginPage();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error registering user: " + ex.getMessage());
                }
            }
        });

        setVisible(true);
    }
}
