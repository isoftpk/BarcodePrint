//import com.openbravo.data.gui.MessageInf;
//import com.openbravo.pos.printer.TicketPrinterException;

import gnu.io.*;

import java.io.IOException;
import java.io.OutputStream;

public class PrinterWritterArgox extends PrinterWritter /* implements SerialPortEventListener */ {
    
    private CommPortIdentifier m_PortIdPrinter;
    private CommPort m_CommPortPrinter;  
    
    private String m_sPortPrinter;
    private OutputStream m_out;
    
    /** Creates a new instance of PrinterWritterComm */
    public PrinterWritterArgox(String sPortPrinter) throws TicketPrinterException {
        m_sPortPrinter = sPortPrinter;
        
        m_out = null; 
    }
    
    @Override
    public void internalWrite(byte[] data) {
        try {  
            if (m_out == null) {
                m_PortIdPrinter = CommPortIdentifier.getPortIdentifier(m_sPortPrinter); // Tomamos el puerto                   
                m_CommPortPrinter = m_PortIdPrinter.open("PORTID", 2000); // Abrimos el puerto       

                m_out = m_CommPortPrinter.getOutputStream(); // Tomamos el chorro de escritura   

                if (m_PortIdPrinter.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    ((SerialPort)m_CommPortPrinter).setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE); // Configuramos el puerto
                    ((SerialPort)m_CommPortPrinter).setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);  // this line prevents the printer tmu220 to stop printing after +-18 lines printed
                    // this line prevents the printer tmu220 to stop printing after +-18 lines printed. Bug 8324
                    // But if added a regression error appears. Bug 9417, Better to keep it commented.
                    // ((SerialPort)m_CommPortPrinter).setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
    // Not needed to set parallel properties
    //                } else if (m_PortIdPrinter.getPortType() == CommPortIdentifier.PORT_PARALLEL) {
    //                    ((ParallelPort)m_CommPortPrinter).setMode(1);

                }
            }
            m_out.write(data);
// JG 16 May 12 use multicatch
        } catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
            System.err.println(e);
        } catch (NoSuchPortException e1) {
            System.out.println("the port called " + m_sPortPrinter + " is invalid or does not exist");
        }
        
    }
    
    @Override
    public void internalFlush() {
        try {  
            if (m_out != null) {
                m_out.flush();
            }
        } catch (IOException e) {
            System.err.println(e);
        }    
    }
    
    @Override
    public void internalClose() {
        try {  
            if (m_out != null) {
            	m_out.flush();
                m_out.close();
                m_out = null;
                m_CommPortPrinter.close();
                //m_CommPortPrinter = null;
                m_PortIdPrinter = null;
            }
        } catch (IOException e) {
            System.err.println(e);
        }    
    }
}
