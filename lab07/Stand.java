package lab07;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import communication.ICenter;
import communication.IStand;
import support.CustomException;
import support.Description;

import javax.swing.*;

public class Stand implements IStand{

	private int id;
	private JPanel panel;
	private JLabel standName;
	private ICenter ic;

	private Stand() {
		try {
			Registry reg = LocateRegistry.getRegistry("localhost",4000);
			ic = (ICenter) reg.lookup("Center");
			IStand is = (IStand) UnicastRemoteObject.exportObject(this,0);
			System.out.println("Stand is ready");
			id = ic.connect(is);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
	    new Stand().startGUI();
	}

	private void startGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		JFrame frame = new JFrame("Stand");
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		frame.addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				try {
					ic.disconnect(id);
				} catch (RemoteException | CustomException remoteException) {
					remoteException.printStackTrace();
				}
				frame.dispose();
			}
		});
		frame.setMinimumSize(new Dimension(300, 100));
	}


	@Override
	public void setContent(Description d) throws RemoteException {
		System.out.printf("New description: %s\n", d.description);
		standName.setText(d.description);
	}

	@Override
	public int getId() throws RemoteException {
		return id;
	}

}
