package lab07;

import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.List;

import communication.ICenter;
import communication.IDesigner;
import communication.IStand;
import support.Answer;
import support.CustomException;
import support.Description;
import support.Question;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;


public class Designer implements IDesigner, ListModel<String> {
	private JTabbedPane tabbedPane;

	private JList<String> standsList;
	private JButton editStandTextButton;
	private JTable questionsAnswersTable;
	private JButton addNewQuestionButton;
	private JPanel panel;

	private List<ListDataListener> standsListDataListener = new ArrayList<>();
	private List<IStand> stands = new ArrayList<>();
	private Map<IStand, String> descriptions = new HashMap<>();
	private ICenter ic;
	private int id;

	public Designer() {
		try {
			Registry reg = LocateRegistry.getRegistry("localhost",4000);
			ic = (ICenter) reg.lookup("Center");
			IDesigner iDesigner = (IDesigner) UnicastRemoteObject.exportObject(this,0);
			System.out.println("Designer is ready");
			id = ic.connect(iDesigner);

			askForStands();
		} catch (RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Designer().startGUI();
	}

	private void startGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		questionsAnswersTable.setModel(new DefaultTableModel(new Object[]{"Pytania", "Odpowiedzi"}, 0) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		standsList.setModel(this);
		editStandTextButton.addActionListener(actionEvent -> editStandText());
		addNewQuestionButton.addActionListener(actionEvent -> addNewQuestion());

		JFrame frame = new JFrame("Designer");
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		frame.setMinimumSize(new Dimension(300, 200));
	}

	private void editStandText() {
		int selectedIndex = standsList.getSelectedIndex();

		IStand stand = stands.get(selectedIndex);
		String oldText = descriptions.getOrDefault(stand, "");
		String newText = JOptionPane.showInputDialog(panel, "Wprowadź nową treść", oldText);

		descriptions.put(stand, newText);
		notifyStandListListeners();

		Description description = new Description();
		description.description = newText;
		try {
			stand.setContent(description);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void addNewQuestion() {
		String questionText = JOptionPane.showInputDialog(panel, "Question:");
		Question question = new Question();
		question.question = questionText;

		String answerText = JOptionPane.showInputDialog(panel, "Answer:");
		Answer answer = new Answer();
		answer.answer = answerText;

		try {
			ic.addQA(new Question[] { question }, new Answer[] { answer });

			((DefaultTableModel) questionsAnswersTable.getModel()).addRow(new Object[]{ questionText, answerText });
		} catch (RemoteException | CustomException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notify(int standId, boolean isConnected) throws RemoteException {
		System.out.println("standId=" + standId + " isConnected=" + isConnected);

		askForStands();
	}

	private void askForStands() throws RemoteException {
	    stands = Arrays.asList(ic.getStands());
		notifyStandListListeners();
	}

	private void notifyStandListListeners() {
		// tell standsList something's changed
		ListDataEvent listDataEvent = new ListDataEvent(
				this,
				ListDataEvent.CONTENTS_CHANGED,
				0,
				stands.size() - 1);

		standsListDataListener.forEach(listDataListener ->
				listDataListener.contentsChanged(listDataEvent));
	}

	// For stand list
	@Override
	public int getSize() {
		return stands.size();
	}

	// For stand list
	@Override
	public String getElementAt(int i) {
	    IStand stand = stands.get(i);
	    return descriptions.getOrDefault(stand, "------------");
	}

	@Override
	public void addListDataListener(ListDataListener listDataListener) {
		standsListDataListener.add(listDataListener);
	}

	@Override
	public void removeListDataListener(ListDataListener listDataListener) {
		standsListDataListener.remove(listDataListener);
	}
}
