import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

public class BarcodeTable {
    public static void main(String args[]) {

        JFrame frame = new JFrame("TPOM Barcode Print");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ///Query TPOM Database - Products Table
        ArrayList columnNames = new ArrayList();
        ArrayList data = new ArrayList();

        //  Connect to an MySQL Database, run query, get result set
        String url = "jdbc:mysql://localhost:3306/unicentaopos";
        String userid = "tpom";
        String password = "tpom@";
        String sql = "SELECT * FROM PRODUCTS";

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

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        final JTable table = new JTable(model);
        final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        table.setRowSorter(sorter);

        //For the purposes of this example, better to have a single
        //selection.
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane pane = new JScrollPane(table);
        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        frame.add(pane);

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));

        JLabel lblFilter= new JLabel("Filter:");
        final JTextField filterText = new JTextField("");

        lblFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        p1.add(lblFilter);

        filterText.setAlignmentX(Component.LEFT_ALIGNMENT);
        p1.add(filterText);

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

//        filterText.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent arg0) {
//                if (table.isEditing()) {
//                    table.removeEditor();
//                }
//                table.getSelectionModel().clearSelection();
//            }
//        });


        JLabel lblBarcode = new JLabel("Barcode:");
        JLabel lblBarcodePrint = new JLabel("<Select a Barcode>");
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
                            final String barcodeNum = table.getModel().getValueAt(viewRow, 0).toString();
                            lblBarcodePrint.setText(barcodeNum);
                        }
                    }
                }
        );



        p1.add(lblBarcode);
        p1.add(lblBarcodePrint);
        p1.add(button);

        frame.add(p1);

        frame.pack();
        frame.setSize(800, 575);
        frame.setVisible(true);
    }
}