import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodePrint extends JPanel {

    private static final byte[] NEW_LINE = {0x0A}; // Print and carriage return

    private ExecutorService exec;

    private String BC_Num = "";
    private String BC_Price = "";
    private String BC_Qty = "";

    JRadioButton rdLarge;
    JRadioButton rdSmall;

    PrinterWritterArgox bpw;

    public BarcodePrint() {
        super();

        ///Query TPOM Database - Products Table
        ArrayList columnNames = new ArrayList();
        ArrayList data = new ArrayList();

        //  Connect to an MySQL Database, run query, get result set
        String url = "jdbc:mysql://localhost:3306/unicentaopos";
        String userid = "tpom";
        String password = "tpom@";
        String sql = "SELECT * FROM products";

        // Java SE 7 has try-with-resources
        // This will ensure that the sql objects are closed when the program
        // is finished with them
        try (Connection connection = DriverManager.getConnection( url, userid, password );
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery( sql ))
        {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();

            //  Get row data
            while (rs.next())
            {
                ArrayList row = new ArrayList(columns);

                row.add( rs.getObject(2) );
                row.add( rs.getObject(5) );
                row.add( rs.getObject(7) );

                data.add( row );
            }
        }
        catch (SQLException e)
        {
            System.out.println( e.getMessage() );
        }

        // Create Vectors and copy over elements from ArrayLists to them
        // Vector is deprecated but I am using them in this example to keep
        // things simple - the best practice would be to create a custom defined
        // class which inherits from the AbstractTableModel class
        Vector columnNamesVector = new Vector();
        Vector dataVector = new Vector();

        for (int i = 0; i < data.size(); i++)
        {
            ArrayList subArray = (ArrayList)data.get(i);
            Vector subVector = new Vector();
            for (int j = 0; j < subArray.size(); j++)
            {
                subVector.add(subArray.get(j));
            }
            dataVector.add(subVector);
        }

        columnNamesVector.add("Barcode");
        columnNamesVector.add("Description");
        columnNamesVector.add("Price");

        TableModel model = new DefaultTableModel(dataVector, columnNamesVector) {
            public Class getColumnClass(int column) {
                for (int row = 0; row < getRowCount(); row++)
                {
                    Object o = getValueAt(row, column);

                    if (o != null)
                    {
                        return o.getClass();
                    }
                }

                return Object.class;
            }
        };

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        final JTable table = new JTable(model);
        final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        table.setRowSorter(sorter);

        //single row selection.
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane pane = new JScrollPane(table);
        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(pane);

        JLabel lblFilter= new JLabel("Filter:");
        final JTextField filterText = new JTextField("");
        JLabel lblQtyPrint= new JLabel("Qty:");
        final JTextField txtQtyPrint = new JTextField("1");

        lblFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblFilter);

        filterText.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(filterText);

        JButton button = new JButton("Print");

        filterText.getDocument().addDocumentListener(new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) {}

            private void filter() {
                String text = filterText.getText();
                if (text.length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter(text));
                }
            }
        });

        JLabel lblBarcode = new JLabel("Barcode:");
        JLabel lblBarcodePrint = new JLabel("<Select a Barcode>");
        JLabel lblBarcodePricePrint = new JLabel("<Select a Barcode>");
        Font f2 = new Font("Engravers MT", Font.PLAIN, 40);
        lblBarcodePrint.setFont(f2);

        lblBarcode.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBarcodePrint.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        //When selection changes, provide user with row numbers for
        //both view and model.
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent event) {
                        final int viewRow = table.getSelectedRow();
                        if (viewRow < 0) {
                            //Selection got filtered away.
                            lblBarcodePrint.setText("<Select a Barcode>");
                        } else {
                            int rowIndex = table.convertRowIndexToModel(viewRow);
                            final String barcodeNum = table.getModel().getValueAt(rowIndex, 0).toString();
//                            final String barcodePrice = table.getModel().getValueAt(rowIndex, 2).toString();
                            Double barcodePrice = null;
                            if (table.getModel().getValueAt(rowIndex, 2) instanceof Double) {
                                barcodePrice = (Double) table.getModel().getValueAt(rowIndex, 2);
                            }
//                            System.out.println(String.format( "%.2f", barcodePrice ));

                            lblBarcodePrint.setText(barcodeNum);
                            lblBarcodePricePrint.setText(String.format( "%.2f", barcodePrice ).toString());
                        }
                    }
                });


        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                BC_Num = lblBarcodePrint.getText();
//                float iBC_Price = Float.parseFloat(lblBarcodePricePrint.getText());
//                BC_Price = String.format("%.2f", iBC_Price);
                BC_Price = lblBarcodePricePrint.getText();
                BC_Qty = txtQtyPrint.getText();

//                System.out.println(BC_Price);
//                System.out.println();

                jbtnNewActionPerformed(evt);
            }
        });

        add(lblBarcode);
        add(lblBarcodePrint);

        lblQtyPrint.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblQtyPrint);

        txtQtyPrint.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(txtQtyPrint);

        //Create the radio buttons.
        rdLarge = new JRadioButton("Large");
        rdSmall = new JRadioButton("Small");
        rdLarge.setSelected(true);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(rdLarge);
        group.add(rdSmall);

        rdLarge.setAlignmentX(Component.LEFT_ALIGNMENT);
        rdSmall.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(rdLarge);
        add(rdSmall);
        add(button);

    }


    public void jbtnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnNewActionPerformed
        // Add your handling code here:
        exec = Executors.newSingleThreadExecutor();
        String sComport = "/dev/ttyUSB0";

        try {
            printBarcode(sComport);

        } catch (Exception eD) {
            JOptionPane.showMessageDialog(null, "Not possible to access barcode printer, usb port may be wrong");
        }
    }//GEN-LAST:event_jbtnNewActionPerformed


    public void printBarcode(String sComport) {

        exec = Executors.newSingleThreadExecutor();

        try {

            bpw = new PrinterWritterArgox(sComport);

            if (rdSmall.isSelected()) {


//            bpw.write("N");
//            bpw.write(NEW_LINE);
//            bpw.write("LO280,10,260,2");
//            bpw.write(NEW_LINE);
//            bpw.write("LO280,300,260,2");
//            bpw.write(NEW_LINE);
//            bpw.write("A90,30,0,c,1,1,N,\"That Place on Main\"");
//            bpw.write(NEW_LINE);
//            bpw.write("A130,90,0,3,2,2,N,\"" + BC_Num + "\"");
//            bpw.write(NEW_LINE);
//            bpw.write("B130,160,0,1,2,1,90,B,\""+ BC_Price + "\"");
//            bpw.write(NEW_LINE);
//            bpw.write("P" + BC_Qty);
//            bpw.write(NEW_LINE);

//	    	bpw.write("A10,10,0,3,2,2,N,\"TPOM\"");
//	    	bpw.write(NEW_LINE);
//	    	bpw.write("B320,200,0,1,2,1,50,B,\""+ m_jBarcode.getText() + "\"");
//	    	bpw.write(NEW_LINE);

//                bpw.write("LO280,10,260,2");
//                bpw.write(NEW_LINE);
//                bpw.write("LO280,100,260,2");
//                bpw.write(NEW_LINE);
//                bpw.write("A260,20,0,c,1,1,N,\"That Place on Main\"");
//                bpw.write(NEW_LINE);
                bpw.write("JF");
                bpw.write(NEW_LINE);
                bpw.write("A300,28,0,1,2,2,N,\"" + "R " + BC_Price + "\"");
                bpw.write(NEW_LINE);
                bpw.write("B300,75,0,1,2,1,40,N,\"" + BC_Num + "\"");
                bpw.write(NEW_LINE);
                bpw.write("P" + BC_Qty);
                bpw.write(NEW_LINE);

            }

            if (rdLarge.isSelected()) {

                bpw.write("JF");
                bpw.write(NEW_LINE);
                bpw.write("LO280,10,260,2");
                bpw.write(NEW_LINE);
                bpw.write("LO280,300,260,2");
                bpw.write(NEW_LINE);
                bpw.write("A260,30,0,c,1,1,N,\"That Place on Main\"");
                bpw.write(NEW_LINE);
                bpw.write("A300,90,0,3,2,2,N,\"" + "R " + BC_Price + "\"");
                bpw.write(NEW_LINE);
                bpw.write("B300,160,0,1,2,1,90,B,\"" + BC_Num + "\"");
                bpw.write(NEW_LINE);
                bpw.write("P" + BC_Qty);
                bpw.write(NEW_LINE);

            }

            bpw.close();



        } catch (TicketPrinterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    public void write(final byte[] data) {
        exec.execute(new Runnable() {
            @Override
            public void run() {
                bpw.internalWrite(data);
            }
        });
    }

    public void flush() {
        exec.execute(new Runnable() {
            @Override
            public void run() {
                bpw.internalFlush();
            }
        });
    }

    public void close() {
        exec.execute(new Runnable() {
            @Override
            public void run() {
                bpw.internalClose();
            }
        });
        exec.shutdown();
    }


    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TPOM Barcode Print");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        BarcodePrint newContentPane = new BarcodePrint();
        frame.setContentPane(newContentPane);

        //icon
        try {
            frame.setIconImage(ImageIO.read( ClassLoader.getSystemResource( "src/icon_barcode.png" )));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Display the window.
        frame.setPreferredSize(new Dimension(800,550));
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }




}