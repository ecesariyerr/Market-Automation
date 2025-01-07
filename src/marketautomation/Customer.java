package marketautomation;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.sql.*;

public class Customer extends JFrame {
    private JTextField searchField;
    private JTable productTable;
    private static DefaultTableModel customerTableModel; 
    private int userId;

    public Customer(int userId) {
        this.userId = userId;
        setTitle("Customer Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(null);

        if (customerTableModel == null) {
            String[] columnNames = {"Product Name", "Description", "Price", "Unit", "Stock", "Add"};
            customerTableModel = new DefaultTableModel(columnNames, 0);
        }

        
        productTable = new JTable(customerTableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBounds(20, 100, 550, 150);
        add(scrollPane);

        
        searchField = new JTextField();
        searchField.setBounds(20, 60, 310, 35);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText().trim();
                if (query.isEmpty()) {
                    loadProductsFromDatabase();
                } else {
                    filterProducts(query);
                }
            }
        });
        add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setBounds(350, 60, 100, 35);
        searchButton.addActionListener(e -> filterProducts(searchField.getText()));
        add(searchButton);

        
        JButton cartButton = new JButton("My Cart");
        cartButton.setBounds(20, 300, 130, 35);
        cartButton.addActionListener(e -> {
            dispose();
            new Cart(userId);
            
        });
        add(cartButton);

        
        JButton profileButton = new JButton("My Profile");
        profileButton.setBounds(160, 300, 130, 35);
        profileButton.addActionListener(e -> {
            dispose();
            new Profile(userId);
        });
        add(profileButton);

        
        JButton ordersButton = new JButton("My Orders");
        ordersButton.setBounds(300, 300, 130, 35);
        ordersButton.addActionListener(e -> {
            dispose();
            new MyOrders(userId);
        });
        add(ordersButton);

        
        JButton exportButton = new JButton("Export to PDF");
        exportButton.setBounds(440, 300, 130, 35);
        exportButton.addActionListener(e -> exportToPDF()); 
        add(exportButton);

        loadProductsFromDatabase();
        setVisible(true);
    }

    private void loadProductsFromDatabase() {
        try (Connection conn = DatabaseOperations.connect()) {
            String query = "SELECT * FROM products";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            customerTableModel.setRowCount(0);

            while (rs.next()) {
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                String unit = rs.getString("unit");
                int stock = rs.getInt("stock");

                customerTableModel.addRow(new Object[]{name, description, price, unit, stock, "+"});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products from database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        addButtonFunctionality();
    }

    private void filterProducts(String query) {
        try (Connection conn = DatabaseOperations.connect()) {
            String querySQL = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(querySQL);
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");

            ResultSet rs = pstmt.executeQuery();

            customerTableModel.setRowCount(0); 

            while (rs.next()) {
                String productName = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                String unit = rs.getString("unit");
                int stock = rs.getInt("stock");

                customerTableModel.addRow(new Object[]{productName, description, price, unit, stock, "+"});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error filtering products.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        addButtonFunctionality();
    }

    private void addButtonFunctionality() {
        productTable.getColumn("Add").setCellRenderer(new ButtonRenderer());
        productTable.getColumn("Add").setCellEditor(new ButtonEditor(new JCheckBox()));
    }

    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setText("+");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("+");
            button.addActionListener(e -> {
                selectedRow = productTable.getSelectedRow();
                String productName = productTable.getValueAt(selectedRow, 0).toString();
                String priceStr = productTable.getValueAt(selectedRow, 2).toString();

                try {
                    double price = Double.parseDouble(priceStr);
                    
                    String quantityInput = JOptionPane.showInputDialog("Enter quantity for " + productName + ":");

                    if (quantityInput != null && !quantityInput.isEmpty()) {
                        try {
                            int quantity = Integer.parseInt(quantityInput);
                            double totalPrice= price*quantity;
                            try (Connection conn = DatabaseOperations.connect()) {
                                String query = "INSERT INTO orders (user_id, product_id, quantity, total_price, status) VALUES (?, ?, ?, ?, ?)";
                                PreparedStatement stmt = conn.prepareStatement(query);

                                    
                                    String productQuery = "SELECT id FROM products WHERE name = ?";
                                    PreparedStatement productStmt = conn.prepareStatement(productQuery);
                                    productStmt.setString(1, productName);
                                    ResultSet rs = productStmt.executeQuery();

                                    if (rs.next()) {
                                        int productId = rs.getInt("id");
                                        stmt.setInt(1, userId);
                                        stmt.setInt(2, productId);
                                        stmt.setInt(3, quantity);
                                        stmt.setDouble(4, totalPrice);
                                        stmt.setString(5, "user-pending");
                                        stmt.addBatch();
                                    }

                                stmt.executeBatch();
                            } catch (SQLException se) {
                                JOptionPane.showMessageDialog(null, "Error confirming order: " + se.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                            }
                            JOptionPane.showMessageDialog(null, "Product added to cart!");
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Invalid quantity entered!");
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid price format!");
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return button;
        }
    }

    
    private void exportToPDF() {
        try {
            String fileName="";
            if(searchField.getText().isEmpty()){
                fileName = "ProductList.pdf";
                
            }else{
                fileName = "Filtered_Products.pdf";
            }
            //String fileName = "Filtered_Products.pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            document.add(new Paragraph("Product List", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" "));

            PdfPTable pdfTable = new PdfPTable(productTable.getColumnCount());
            pdfTable.setWidthPercentage(100);

            
            for (int i = 0; i < productTable.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(productTable.getColumnName(i)));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfTable.addCell(cell);
            }

            
            for (int rows = 0; rows < productTable.getRowCount(); rows++) {
                for (int cols = 0; cols < productTable.getColumnCount(); cols++) {
                    pdfTable.addCell(productTable.getValueAt(rows, cols).toString());
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
