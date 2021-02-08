package lab07;

import communication.ICenter;
import support.CustomException;
import support.Question;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class Visitor {
    private JPanel panel;
    private JTextArea userName;
    private JButton signInButton;
    private JTable questionAndInputsTable;
    private JButton signOutButton;
    private ICenter iCenter;

    private boolean amISigned = false;
    private int id;

    public Visitor() {
        try {
            Registry reg = LocateRegistry.getRegistry("localhost", 3000);
            iCenter = (ICenter) reg.lookup("Center");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Visitor().startGUI();
    }

    private void signOut() throws RemoteException, CustomException {
        iCenter.signOut(id);
        switchCard("signedOut");
        amISigned = false;
    }

    private void switchCard(String cardName) {
        CardLayout layout = (CardLayout) panel.getLayout();
        layout.show(panel, cardName);
    }

    private void signIn() throws RemoteException {
        String name = userName.getText();
        if(name.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Brak wpisanej nazwy użytkownika", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }
        id = iCenter.signIn(name);
        amISigned = true;

        Question[] questions = iCenter.getQuestions();

        var model = new DefaultTableModel(new Object[]{"Pytanie", "Odpowiedź"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        Arrays.stream(questions)
                .map(question -> new Object[]{question.question, ""})
                .forEachOrdered(model::addRow);

        questionAndInputsTable.setModel(model);

        switchCard("panelAnswersAndQuestions");
    }

    private void startGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        signInButton.addActionListener(actionEvent -> {
            try {
                signIn();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        signOutButton.addActionListener(actionEvent -> {
            try {
                signOut();
            } catch (RemoteException | CustomException e) {
                e.printStackTrace();
            }
        });

        JFrame frame = new JFrame("Visitor");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(300, 100));
    }


}
