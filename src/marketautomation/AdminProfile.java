package marketautomation;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class AdminProfile extends JFrame {
    private JLabel profilePicture;
    private int userId;
    private JTextField nameField, emailField;
    private JPasswordField passwordField;

    public AdminProfile() {
        setTitle("Admin Profile");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setVisible(true);

        
        profilePicture = new JLabel();
        profilePicture.setBounds(200, 20, 100, 100);
        profilePicture.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Çerçeve
        add(profilePicture);
        
        JButton uploadButton = new JButton("Upload Photo");
        uploadButton.setBounds(180, 130, 140, 25);
        add(uploadButton);
        
        uploadButton.addActionListener(e -> uploadPhoto());
        
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(50, 160, 100, 25);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(150, 160, 250, 25);
        add(nameField);

        
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(50, 210, 100, 25);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(150, 210, 250, 25);
        emailField.setEditable(false); // Email is non-editable
        add(emailField);

        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 260, 100, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 260, 250, 25);
        add(passwordField);

        
        JButton updateButton = new JButton("Update");
        updateButton.setBounds(100, 360, 120, 35);
        updateButton.addActionListener(e -> updateProfile());
        add(updateButton);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(250, 360, 120, 35);
        add(logoutButton);
        
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginPage();
        });
        
        JButton backButton = new JButton("\u2190");
        backButton.setBounds(10, 10, 50, 25);
        add(backButton);
        
        backButton.addActionListener(e -> {
            dispose();
            new Admin();
        });
        
        

        
        loadProfile();
    }
public void loadProfile() {
    try (Connection conn = DatabaseOperations.connect()) {
        String query = "SELECT * FROM users WHERE email = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, LoginPage.getLoggedInEmail());

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            userId = rs.getInt("id");  // userId değerini buradan al
            nameField.setText(rs.getString("name"));
            emailField.setText(rs.getString("email"));
            passwordField.setText(rs.getString("password"));
            String photoPath = rs.getString("photo_path");
            if (photoPath != null && !photoPath.isEmpty()) {
                ImageIcon imageIcon = new ImageIcon(photoPath);
                Image image = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                profilePicture.setIcon(new ImageIcon(image));
            }
        } else {
            JOptionPane.showMessageDialog(this, "User not found", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}



    
    private void updateProfile() {
        
        String email = emailField.getText();
        String name = nameField.getText();
        String password = new String(passwordField.getPassword());

        // Check for empty fields
        if (name.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseOperations.connect()) {
            String query = "UPDATE users SET name = ?, password = ? WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setString(3, email);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Error updating profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();  
        }
    }
       private void uploadPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ImageIcon imageIcon = new ImageIcon(file.getAbsolutePath());
                Image image = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                profilePicture.setIcon(new ImageIcon(image));
                
                String photoPath = file.getAbsolutePath();
                updatePhotoPathInDatabase(photoPath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid file type or error loading image.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No file selected.");
        }
    }
   
       private void updatePhotoPathInDatabase(String photoPath) {
    try (Connection conn = DatabaseOperations.connect()) {
        String query = "UPDATE users SET photo_path = ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, photoPath);
        stmt.setInt(2, userId);

        int rowsUpdated = stmt.executeUpdate();
        if (rowsUpdated > 0) {
            JOptionPane.showMessageDialog(this, "Photo updated successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Error updating photo.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
}
