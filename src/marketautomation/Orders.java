package marketautomation;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.sql.*;

public class Orders extends JFrame {

    private DefaultTableModel ordersTableModel;
    private JTable ordersTable;

    public Orders() {
        setTitle("Orders");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("\u2190");
        backButton.addActionListener(e -> {
            dispose();
            new Admin();
        });
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        
        ordersTableModel = new DefaultTableModel(new String[]{"Order ID", "Product ID", "User", "Product", "Quantity", "Total Price", "Status"}, 0);
        ordersTable = new JTable(ordersTableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);

        
        ordersTable.getColumnModel().getColumn(1).setMinWidth(0);
        ordersTable.getColumnModel().getColumn(1).setMaxWidth(0);
        ordersTable.getColumnModel().getColumn(1).setWidth(0);

        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton updateButton = new JButton("Update Status");
        JButton exportButton = new JButton("Export to PDF");

        JRadioButton approvedButton = new JRadioButton("approved");
        JRadioButton rejectedButton = new JRadioButton("rejected");
        ButtonGroup statusGroup = new ButtonGroup();
        statusGroup.add(approvedButton);
        statusGroup.add(rejectedButton);

        buttonPanel.add(approvedButton);
        buttonPanel.add(rejectedButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(exportButton);
        add(buttonPanel, BorderLayout.SOUTH);

       
        approvedButton.setActionCommand("approved");
        rejectedButton.setActionCommand("rejected");

        
        updateButton.addActionListener(e -> updateOrderStatus(statusGroup));
        exportButton.addActionListener(e -> exportOrdersToPDF());
        approvedButton.addActionListener(e -> approveSelectedOrder());

        
        loadOrders();

        setVisible(true);
    }

    private void loadOrders() {
        try (Connection conn = DatabaseOperations.connect()) {
            String query = "SELECT orders.id, orders.product_id, users.name AS user_name, products.name AS product_name, " +
                    "orders.quantity, orders.total_price, orders.status " +
                    "FROM orders " +
                    "JOIN users ON orders.user_id = users.id " +
                    "JOIN products ON orders.product_id = products.id WHERE status != 'user-pending'";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            ordersTableModel.setRowCount(0); 
            while (rs.next()) {
                int orderId = rs.getInt("id");
                int productId = rs.getInt("product_id");
                String userName = rs.getString("user_name");
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("quantity");
                double totalPrice = rs.getDouble("total_price");
                String status = rs.getString("status");

                ordersTableModel.addRow(new Object[]{orderId, productId, userName, productName, quantity, totalPrice, status});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOrderStatus(ButtonGroup statusGroup) {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) ordersTable.getValueAt(selectedRow, 0);
            ButtonModel selectedStatus = statusGroup.getSelection();

            if (selectedStatus != null) {
                String newStatus = selectedStatus.getActionCommand();
                try (Connection conn = DatabaseOperations.connect()) {
                    String query = "UPDATE orders SET status = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, newStatus);
                    stmt.setInt(2, orderId);

                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        ordersTableModel.setValueAt(newStatus, selectedRow, 6);
                        JOptionPane.showMessageDialog(this, "Order status updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Error updating order status.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error updating order: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a status to update.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to update.");
        }
    }

    private void approveSelectedOrder() {
        int orderId = getSelectedOrderId();
        int productId = getSelectedProductId();
        int quantity = getSelectedQuantity();

        if (orderId != -1 && productId != -1 && quantity > 0) {
            approveOrderAndUpdateStock(orderId, productId, quantity);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a valid order to approve.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void approveOrderAndUpdateStock(int orderId, int productId, int quantity) {
        try (Connection conn = DatabaseOperations.connect()) {
            conn.setAutoCommit(false);

            String updateOrderQuery = "UPDATE orders SET status = 'approved' WHERE id = ?";
            PreparedStatement orderStmt = conn.prepareStatement(updateOrderQuery);
            orderStmt.setInt(1, orderId);
            orderStmt.executeUpdate();

            String updateStockQuery = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
            PreparedStatement stockStmt = conn.prepareStatement(updateStockQuery);
            stockStmt.setInt(1, quantity);
            stockStmt.setInt(2, productId);
            stockStmt.setInt(3, quantity);

            int rowsAffected = stockStmt.executeUpdate();
            if (rowsAffected > 0) {
                conn.commit();
                JOptionPane.showMessageDialog(this, "Order approved and stock updated successfully.");
            } else {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Insufficient stock or invalid product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSelectedOrderId() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (int) ordersTable.getValueAt(selectedRow, 0);
        }
        return -1;
    }

    private int getSelectedProductId() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (int) ordersTable.getValueAt(selectedRow, 1); 
        }
        return -1;
    }

    private int getSelectedQuantity() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (int) ordersTable.getValueAt(selectedRow, 4); 
        }
        return 0;
    }

    private void exportOrdersToPDF() {
        try {
            String fileName = "Orders.pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            document.add(new Paragraph("Orders", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" ")); // Empty line

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
