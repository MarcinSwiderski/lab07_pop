package lab07;

import communication.ICenter;
import support.Answer;
import support.CustomException;
import support.Question;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.IntStream;

public class Visitor {
    private JPanel panel;
    private JTextArea userName;
    private JButton joinButton;
    private JTable questionsAnswersTable;
    private JButton signOutButton;
    private JButton checkAnswersButton;
    private JButton resultsSignOutButton;
    private JTable resultsTable;
    private ICenter iCenter;

    private boolean signedIn = false;
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
        new Visitor().runUi();
    }

    private void signOut() throws RemoteException, CustomException {
        if(iCenter == null || !signedIn)
            return;

        iCenter.signOut(id);

        switchCard("signedOut");
        signedIn = false;
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
        signedIn = true;

        Question[] questions = iCenter.getQuestions();

        var model = new DefaultTableModel(new Object[]{"Pytanie", "Odpowiedź"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        Arrays.stream(questions)
                .map(question -> new Object[]{question.question, ""})
                .forEachOrdered(model::addRow);

        questionsAnswersTable.setModel(model);

        switchCard("questionsCard");
    }

    private void checkAnswers() throws RemoteException, CustomException {
        var dataVector = ((DefaultTableModel) questionsAnswersTable.getModel())
                .getDataVector();

        Object[] questionsText = dataVector.stream().map(v -> v.get(0)).toArray();
        Object[] answersText = dataVector.stream().map(v -> v.get(1)).toArray();

        Answer[] answers = new Answer[answersText.length];
        for(int i = 0; i < answersText.length; i++) {
            Answer ans = new Answer();
            ans.answer = (String) answersText[i];
            answers[i] = ans;
        }

        boolean[] corrects = iCenter.checkAnswers(id, answers);
        Object[] correctsText = IntStream.range(0, corrects.length)
                .mapToObj(i -> corrects[i] ? "tak" : "nie")
                .toArray();

        var model = new DefaultTableModel(new Object[]{"Pytanie", "Odpowiedź", "Czy poprawna"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        IntStream.range(0, corrects.length)
                .mapToObj(i -> new Object[]{questionsText[i], answersText[i], corrects[i] ? "tak" : "nie"})
                .forEachOrdered(model::addRow);

        resultsTable.setModel(model);
        switchCard("resultsCard");
    }

    private void runUi() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        joinButton.addActionListener(actionEvent -> {
            try {
                signIn();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        checkAnswersButton.addActionListener(actionEvent -> {
            try {
                checkAnswers();
            } catch (RemoteException | CustomException e) {
                e.printStackTrace();
            }
        });

        resultsSignOutButton.addActionListener(actionEvent -> {
            try {
                signOut();
            } catch (RemoteException | CustomException e) {
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
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                try {
                    signOut();
                } catch (RemoteException | CustomException remoteException) {
                    remoteException.printStackTrace();
                }
                frame.dispose();
            }
        });
        frame.setMinimumSize(new Dimension(300, 100));
    }


}
