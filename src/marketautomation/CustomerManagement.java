package marketautomation;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.sql.*;

public class CustomerManagement extends JFrame {
    private static DefaultTableModel customerTableModel;

    public CustomerManagement() {
        setTitle("Customer Management");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("\u2190");
        backButton.addActionListener(e -> {
            dispose();
            new Admin();
        });
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"Customer Name", "Email", "Role"};
        customerTableModel = new DefaultTableModel(columnNames, 0);
        JTable customerTable = new JTable(customerTableModel);
        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());


        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteCustomer(customerTable));
        buttonPanel.add(deleteButton);

        JButton exportButton = new JButton("Export to PDF");
        exportButton.addActionListener(e -> exportCustomersToPDF(customerTable)); // Export button
        buttonPanel.add(exportButton);

        add(buttonPanel, BorderLayout.SOUTH);

        
        loadCustomersFromDatabase();

        setVisible(true);
    }

    private void loadCustomersFromDatabase() {
        customerTableModel.setRowCount(0); 
        try (Connection conn = DatabaseOperations.connect()) {
            String query = "SELECT * FROM users WHERE role = 'customer'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            boolean dataFound = false;
            while (rs.next()) {
                dataFound = true;
                String name = rs.getString("name");
                String email = rs.getString("email");
                String role = rs.getString("role");

                customerTableModel.addRow(new Object[]{name, email, role});
            }
            
            if (!dataFound) {
                JOptionPane.showMessageDialog(this, "No customers found with 'Customer' role.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading customers from database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void deleteCustomer(JTable customerTable) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0) {
            String email = (String) customerTable.getValueAt(selectedRow, 1);

            try (Connection conn = DatabaseOperations.connect()) {
                String query = "DELETE FROM users WHERE email = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, email);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
                loadCustomersFromDatabase(); 
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.");
        }
    }

    
    private void exportCustomersToPDF(JTable customerTable) {
        try {
            String fileName = "Customers.pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            document.add(new Paragraph("Customer List", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" ")); 

            PdfPTable pdfTable = new PdfPTable(customerTable.getColumnCount());
            pdfTable.setWidthPercentage(100);

            
            for (int i = 0; i < customerTable.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(customerTable.getColumnName(i)));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfTable.addCell(cell);
            }

            
            for (int rows = 0; rows < customerTable.getRowCount(); rows++) {
                for (int cols = 0; cols < customerTable.getColumnCount(); cols++) {
                    pdfTable.addCell(customerTable.getValueAt(rows, cols).toString());
                }
            }

            document.add(pdfTable);
            document.close();

            JOptionPane.showMessageDialog(this, "PDF successfully created!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
