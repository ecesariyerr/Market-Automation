package marketautomation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPage extends JFrame {
    private static int loggedInUserId;
    protected static String loggedInEmail;
    public LoginPage() {
        setTitle("Login Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(null);

        
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(110, 60, 100, 40);
        add(emailLabel);

        JTextField emailField = new JTextField();
        emailField.setBounds(180, 60, 200, 40);
        add(emailField);

        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(110, 120, 100, 40);
        add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(180, 120, 200, 40);
        add(passwordField);

        
        JButton loginButton = new JButton("Login");
        loginButton.setBounds(220, 220, 100, 35);
        add(loginButton);

        
        JLabel createAccountLabel = new JLabel("Create an Account");
        createAccountLabel.setBounds(220, 260, 150, 40);
        createAccountLabel.setForeground(Color.BLUE);
        createAccountLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(createAccountLabel);

        
        createAccountLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new Register(); 
            }
        });


        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseOperations.connect()) {
                String query = "SELECT id, role FROM users WHERE email = ? AND password = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, email);
                pstmt.setString(2, password);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int  loggedInUserId= rs.getInt("id");

                    String role = rs.getString("role");
                    if ("admin".equalsIgnoreCase(role)) {
                        loggedInEmail =email ;
                        dispose();
                        new Admin(); 
                    } else if ("customer".equalsIgnoreCase(role)) {
                        loggedInEmail =email ;
                        dispose();
                        new Customer(loggedInUserId); 
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid role.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid email or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });


                setVisible(true);
            }
    
            public static int getLoggedInUserId() {
            return loggedInUserId;
            }
            public static String getLoggedInEmail() {
                return loggedInEmail;
            }
        }
