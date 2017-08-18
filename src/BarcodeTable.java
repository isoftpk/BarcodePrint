import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeTable extends JPanel {

    private static final byte[] NEW_LINE = {0x0A}; // Print and carriage return

    private ExecutorService exec;

    private String BC_Num = "";
    private String BC_Price = "";
    private String BC_Qty = "";

    PrinterWritterArgox bpw;

    public BarcodeTable() {
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
                row.add( rs.getObject(6) );

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

        //For the purposes of this example, better to have a single
        //selection.
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane pane = new JScrollPane(table);
        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(pane);

//        JPanel p1 = new JPanel();
//        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));

        JLabel lblFilter= new JLabel("Filter:");
        final JTextField filterText = new JTextField("");

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
                            lblBarcodePrint.setText("");
                        } else {
                            int rowIndex = table.convertRowIndexToModel(viewRow);
                            final String barcodeNum = table.getModel().getValueAt(rowIndex, 0).toString();
                            final String barcodePrice = table.getModel().getValueAt(rowIndex, 2).toString();
                            lblBarcodePrint.setText(barcodeNum);
                            lblBarcodePricePrint.setText(barcodePrice);
                        }
                    }
                });



        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                BC_Num = lblBarcodePrint.getText();
                float iBC_Price = Float.parseFloat(lblBarcodePricePrint.getText());
                BC_Price = String.format("%.0f", iBC_Price);
//                BC_Price = lblBarcodePricePrint.getText();
                BC_Qty = "1";

                jbtnNewActionPerformed(evt);
            }
        });

        add(lblBarcode);
        add(lblBarcodePrint);
        add(button);

//        add(p1);
    }





    public void jbtnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnNewActionPerformed
        // Add your handling code here:
        exec = Executors.newSingleThreadExecutor();
        String sComport = "/dev/ttyUSB0";
        try {
            printBarcode(sComport);

        } catch (Exception eD) {
//                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, "not possible to access barcode printer", eD);
            JOptionPane.showMessageDialog(null, "not possible to access barcode printer");
        }
    }//GEN-LAST:event_jbtnNewActionPerformed


    public void printBarcode(String sComport) {

        exec = Executors.newSingleThreadExecutor();

        try {

            bpw = new PrinterWritterArgox(sComport);

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

            bpw.write("LO280,10,260,2");
	    	bpw.write(NEW_LINE);
	    	bpw.write("LO280,300,260,2");
	    	bpw.write(NEW_LINE);
	    	bpw.write("A260,30,0,c,1,1,N,\"That Place on Main\"");
	    	bpw.write(NEW_LINE);
	    	bpw.write("A300,90,0,3,2,2,N,\"" + BC_Num + "\"");
	    	bpw.write(NEW_LINE);
	    	bpw.write("B300,160,0,1,2,1,90,B,\""+ BC_Price + "\"");
	    	bpw.write(NEW_LINE);
	    	bpw.write("P" + BC_Qty);
	    	bpw.write(NEW_LINE);

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
        BarcodeTable newContentPane = new BarcodeTable();
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.setPreferredSize(new Dimension(800,575));
        frame.pack();
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