import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class BarcodeTableBak {
    public static void main(String args[]) {
        JFrame frame = new JFrame("TPOM Barcode Print");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Object rows[][] = { { "A", "About", 44.36 }, { "B", "Boy", 44.84 }, { "C", "Cat", 463.63 },
                { "D", "Day", 27.14 }, { "E", "Eat", 44.57 }, { "F", "Fail", 23.15 },
                { "G", "Good", 4.40 }, { "H", "Hot", 24.96 }, { "I", "Ivey", 5.45 },
                { "J", "Jack", 49.54 }, { "K", "Kids", 280.00 } };
        String columns[] = { "Barcode", "Description", "Price" };
        TableModel model = new DefaultTableModel(rows, columns) {
            public Class getColumnClass(int column) {
                Class returnValue;
                if ((column >= 0) && (column < getColumnCount())) {
                    returnValue = getValueAt(0, column).getClass();
                } else {
                    returnValue = Object.class;
                }
                return returnValue;
            }
        };

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        final JTable table = new JTable(model);
        final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        table.setRowSorter(sorter);

        JScrollPane pane = new JScrollPane(table);
        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        frame.add(pane);

        JPanel p1 = new JPanel();

        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));


        JLabel lblFilter= new JLabel("Filter:");
        final JTextField filterText = new JTextField("");

        filterText.setAlignmentX(Component.LEFT_ALIGNMENT);

        p1.add(lblFilter);
        lblFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
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

        JLabel lblBarcode = new JLabel("Barcode:");
        JLabel lblBarcodePrint = new JLabel("<Select a Barcode>");
        Font f2 = new Font("Engravers MT", Font.PLAIN, 40);
        lblBarcodePrint.setFont(f2);

        lblBarcode.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBarcodePrint.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        p1.add(lblBarcode);
        p1.add(lblBarcodePrint);
        p1.add(button);

//        frame.pack();
        frame.add(p1);

        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}