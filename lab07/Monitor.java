package lab07;

import communication.ICenter;
import communication.IMonitor;
import support.CustomException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Monitor implements IMonitor {
    private JPanel panel;
    private JTable table;
    private DefaultTableModel tableModel;

    private ICenter iCenter;
    private int id;

    public static void main(String[] args) {
        new Monitor().startGUI();
    }

    public Monitor() {
        try {
            Registry reg = LocateRegistry.getRegistry("localhost", 4000);
            iCenter = (ICenter) reg.lookup("Center");
            IMonitor im = (IMonitor) UnicastRemoteObject.exportObject(this, 0);
            System.out.println("Monitor is ready");
            id = iCenter.connect(im);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setScore(String userName, int percentageScore) throws RemoteException {

    }

    private void startGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Monitor");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(300, 100));
    }
}
