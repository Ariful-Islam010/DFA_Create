import javax.swing.*;
import java.awt.*;

public class DFAEditor extends JFrame {
    private DFA dfa;
    private DFAPanel dfaPanel;
    private JTextArea descriptionArea;
    private JTextField inputField;
    private JLabel resultLabel;

    public DFAEditor() {
        super("DFA Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create GUI components
        descriptionArea = new JTextArea(10, 30);
        JButton createButton = new JButton("Create DFA");
        dfaPanel = new DFAPanel();
        inputField = new JTextField(20);
        JButton testButton = new JButton("Test String");
        resultLabel = new JLabel("Enter a string to test");

        // Layout
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        topPanel.add(createButton, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(new JLabel("Input:"));
        bottomPanel.add(inputField);
        bottomPanel.add(testButton);
        bottomPanel.add(resultLabel);

        add(topPanel, BorderLayout.NORTH);
        add(dfaPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Add listeners
        createButton.addActionListener(e -> createDFA());
        testButton.addActionListener(e -> testInput());

        // Set default text
        descriptionArea.setText(
                "States: q0, q1\n" +
                        "Alphabet: 0, 1\n" +
                        "Start state: q0\n" +
                        "Accept states: q1\n" +
                        "Transitions:\n" +
                        "q0, 0 -> q0\n" +
                        "q0, 1 -> q1\n" +
                        "q1, 0 -> q1\n" +
                        "q1, 1 -> q1"
        );

        pack();
        setLocationRelativeTo(null);
    }

    private void createDFA() {
        String description = descriptionArea.getText();
        dfa = DFA.fromFormalDescription(description);
        dfaPanel.setDFA(dfa);
        dfaPanel.repaint();
    }

    private void testInput() {
        if (dfa == null) {
            resultLabel.setText("Please create a DFA first");
            return;
        }
        String input = inputField.getText();
        boolean accepts = dfa.accepts(input);
        resultLabel.setText("Result: " + (accepts ? "Accepted" : "Rejected"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DFAEditor().setVisible(true);
        });
    }
}

