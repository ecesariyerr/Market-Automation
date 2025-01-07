package marketautomation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;


public class MyOrders extends JFrame {
    private DefaultTableModel ordersTableModel;
    private JTable ordersTable;

    public MyOrders(int userId) {
        setTitle("My Orders");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        
        ordersTableModel = new DefaultTableModel(new String[]{"Order ID", "Product Name", "Quantity", "Total Price", "Status"}, 0);
        ordersTable = new JTable(ordersTableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            dispose();
            new Customer(userId);
        });
        buttonPanel.add(backButton);
        JButton updateButton = new JButton("Update Quantity");
        updateButton.addActionListener(e -> updateOrderQuantity(userId));
        buttonPanel.add(updateButton);
        
        JButton deleteButton = new JButton("Delete Order");
        deleteButton.addActionListener(e -> deleteOrder(userId));
        buttonPanel.add(deleteButton);
        
        JButton exportPdfButton = new JButton("Export to PDF");
        exportPdfButton.addActionListener(e -> exportTableToPDF("MyOrders_"+ LoginPage.loggedInEmail+".pdf"));
        buttonPanel.add(exportPdfButton);

        
        add(buttonPanel, BorderLayout.SOUTH);

       
        loadApprovedOrders( userId);

        setVisible(true);
    }
    
    private void loadApprovedOrders(int userId) {
    try (Connection conn = DatabaseOperations.connect()) {
        String query = """
            SELECT orders.id, products.name, orders.quantity, orders.total_price, orders.status 
            FROM orders 
            JOIN products ON orders.product_id = products.id 
            WHERE orders.user_id = ?
        """;

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userId);

        ResultSet rs = stmt.executeQuery();
        ordersTableModel.setRowCount(0); 

        while (rs.next()) {
            int orderId = rs.getInt("id");
            String productName = rs.getString("name");
            int quantity = rs.getInt("quantity");
            double totalPrice = rs.getDouble("total_price");
            String status = rs.getString("status");

            ordersTableModel.addRow(new Object[]{orderId, productName, quantity, totalPrice, status});
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
 private void updateOrderQuantity(int userId) {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) ordersTable.getValueAt(selectedRow, 0); 
            String productName = (String) ordersTable.getValueAt(selectedRow, 1); 
            int currentQuantity = (int) ordersTable.getValueAt(selectedRow, 2);
            double currentTotalPrice = (double) ordersTable.getValueAt(selectedRow, 3);
            String status = (String) ordersTable.getValueAt(selectedRow, 4); 

            
            try {
                if ("approved".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(this, "Quantity cannot be changed for approved orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                    return;
                } else if ("rejected".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(this, "No changes are allowed for rejected orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                
                String newQuantityStr = JOptionPane.showInputDialog("Enter new quantity for " + productName + " (Current: " + currentQuantity + "):");

                int newQuantity = Integer.parseInt(newQuantityStr);
                if (newQuantity > 0) {
                    double unitPrice = currentTotalPrice/currentQuantity;
                    double newTotalPrice = unitPrice* newQuantity;
                    
                    try (Connection conn = DatabaseOperations.connect()) {
                        String query = "UPDATE orders SET quantity = ?, total_price= ? WHERE id = ? AND user_id = ?";
                        PreparedStatement stmt = conn.prepareStatement(query);
                        stmt.setInt(1, newQuantity);
                        stmt.setDouble(2, newTotalPrice);
                        stmt.setInt(3, orderId);
                        stmt.setInt(4, userId);

                        int rowsUpdated = stmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            JOptionPane.showMessageDialog(this, "Order quantity updated successfully!");
                            loadApprovedOrders(userId); 
                        } else {
                            JOptionPane.showMessageDialog(this, "Error updating quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error updating order: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity entered.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to update.");
        }
    }



    
    private void deleteOrder(int userId) {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) ordersTable.getValueAt(selectedRow, 0); 
            String productName = (String) ordersTable.getValueAt(selectedRow, 1);

            
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the order for " + productName + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseOperations.connect()) {
                    String query = "DELETE FROM orders WHERE id = ? AND user_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, orderId);
                    stmt.setInt(2, userId);

                    int rowsDeleted = stmt.executeUpdate();
                    if (rowsDeleted > 0) {
                        JOptionPane.showMessageDialog(this, "Order deleted successfully!");
                        loadApprovedOrders(userId);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error deleting order.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting order: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to delete.");
        }
    }

       private void exportTableToPDF(String fileName) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            document.add(new Paragraph("My Orders", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" ")); 

            PdfPTable pdfTable = new PdfPTable(ordersTable.getColumnCount());
            pdfTable.setWidthPercentage(100);

            for (int i = 0; i < ordersTable.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(ordersTable.getColumnName(i)));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfTable.addCell(cell);
            }

            for (int rows = 0; rows < ordersTable.getRowCount(); rows++) {
                for (int cols = 0; cols < ordersTable.getColumnCount(); cols++) {
                    pdfTable.addCell(ordersTable.getValueAt(rows, cols).toString());
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
