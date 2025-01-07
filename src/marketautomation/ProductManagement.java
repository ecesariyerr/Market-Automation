package marketautomation;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.sql.*;

public class ProductManagement extends JFrame {
    private static DefaultTableModel productTableModel;

    static DefaultTableModel getProductTableModel() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public ProductManagement() {
        setTitle("Product Management");
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

        String[] columnNames = {"Product Name", "Description", "Price", "Unit", "Stock"};
        productTableModel = new DefaultTableModel(columnNames, 0);
        JTable productTable = new JTable(productTableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        
        JButton addButton = new JButton("Add Product");
        addButton.addActionListener(e -> addProduct());
        buttonPanel.add(addButton);

        
        JButton updateButton = new JButton("Update Selected");
        updateButton.addActionListener(e -> updateProduct(productTable));
        buttonPanel.add(updateButton);

        
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteProduct(productTable));
        buttonPanel.add(deleteButton);
        
        JButton exportPdfBtn = new JButton("Export to PDF");
        exportPdfBtn.addActionListener(e -> exportTableToPDF(productTable, "ProductList.pdf"));
        buttonPanel.add(exportPdfBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        loadProductsFromDatabase();

        setVisible(true);
    }

    private void loadProductsFromDatabase() {
        productTableModel.setRowCount(0);
        try (Connection conn = DatabaseOperations.connect()) {
            String query = "SELECT * FROM products";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                String unit = rs.getString("unit");
                int stock = rs.getInt("stock");
                
                productTableModel.addRow(new Object[]{name, description, price, unit, stock});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products from database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

private void addProduct() {
    String name = JOptionPane.showInputDialog("Enter product name:");
    String description = JOptionPane.showInputDialog("Enter description:");
    String price = JOptionPane.showInputDialog("Enter price:");
    String unit = JOptionPane.showInputDialog("Enter unit:");
    String stock = JOptionPane.showInputDialog("Enter stock:");

    // Girişlerin boş olup olmadığını kontrol edin
    if (name == null || name.trim().isEmpty() ||
        description == null || description.trim().isEmpty() ||
        price == null || price.trim().isEmpty() ||
        unit == null || unit.trim().isEmpty() ||
        stock == null || stock.trim().isEmpty()) {

        JOptionPane.showMessageDialog(this, "All fields must be filled!", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        double parsedPrice = Double.parseDouble(price.trim());
        int parsedStock = Integer.parseInt(stock.trim());

        try (Connection conn = DatabaseOperations.connect()) {
            String query = "INSERT INTO products (name, description, price, unit, stock) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name.trim());
            stmt.setString(2, description.trim());
            stmt.setDouble(3, parsedPrice);
            stmt.setString(4, unit.trim());
            stmt.setInt(5, parsedStock);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product added successfully!");
            loadProductsFromDatabase();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Price and stock must be valid numbers!", "Input Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private void updateProduct(JTable productTable) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            String name = (String) productTable.getValueAt(selectedRow, 0);
            String description = (String) productTable.getValueAt(selectedRow, 1);
            String price = productTable.getValueAt(selectedRow, 2).toString();
            String unit = (String) productTable.getValueAt(selectedRow, 3);
            String stock = productTable.getValueAt(selectedRow, 4).toString();

            String updatedName = JOptionPane.showInputDialog("Update product name:", name);
            String updatedDescription = JOptionPane.showInputDialog("Update description:", description);
            String updatedPrice = JOptionPane.showInputDialog("Update price:", price);
            String updatedUnit = JOptionPane.showInputDialog("Update unit:", unit);
            String updatedStock = JOptionPane.showInputDialog("Update stock:", stock);

            try (Connection conn = DatabaseOperations.connect()) {
                String query = "UPDATE products SET name = ?, description = ?, price = ?, unit = ?, stock = ? WHERE name = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, updatedName);
                stmt.setString(2, updatedDescription);
                stmt.setDouble(3, Double.parseDouble(updatedPrice));
                stmt.setString(4, updatedUnit);
                stmt.setInt(5, Integer.parseInt(updatedStock));
                stmt.setString(6, name);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
                loadProductsFromDatabase();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to update.");
        }
    }

    private void deleteProduct(JTable productTable) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            String name = (String) productTable.getValueAt(selectedRow, 0);

            try (Connection conn = DatabaseOperations.connect()) {
                String query = "DELETE FROM products WHERE name = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, name);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                loadProductsFromDatabase();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
        }
    }
    
        private void exportTableToPDF(JTable table, String fileName) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Add Title
            document.add(new Paragraph("Product List", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" ")); // Empty line

            // Create Table
            PdfPTable pdfTable = new PdfPTable(table.getColumnCount());
            pdfTable.setWidthPercentage(100);

            // Add Table Headers
            for (int i = 0; i < table.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(table.getColumnName(i)));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfTable.addCell(cell);
            }

            // Add Table Data
            for (int rows = 0; rows < table.getRowCount(); rows++) {
                for (int cols = 0; cols < table.getColumnCount(); cols++) {
                    pdfTable.addCell(table.getValueAt(rows, cols).toString());
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
