package llm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class App {

    private JFrame frame;
    private JTextField inputField;
    private JButton translateButton;
    private JTextField cqlOutputField;
    private JTextArea explanationArea;
    private JLabel statusLabel;
    private JLabel validationLabel;
    private LancsBoxTranslator translator;

    public App() {
        // Initialize the translator engine
        translator = new LancsBoxTranslator();
        initializeUI();
    }

    private void initializeUI() {
        // Set native system look and feel for a modern, premium appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback silently to default look and feel
        }

        frame = new JFrame("CQL Translator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 550);
        frame.setMinimumSize(new Dimension(600, 450));
        frame.setLocationRelativeTo(null); // Center window on screen

        // Root container with generous margins
        JPanel rootPanel = new JPanel(new BorderLayout(15, 15));
        rootPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        rootPanel.setBackground(new Color(245, 246, 248)); // Light neutral gray background

        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("CQL Translator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));     
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centralPanel = new JPanel();
        centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));
        centralPanel.setOpaque(false);

        // Input Wrapper Panel
        JPanel inputWrapper = new JPanel(new BorderLayout(10, 10));
        inputWrapper.setOpaque(false);
        inputWrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 233), 1, true), 
                " Natural Language Search Query ", 
                0, 0, 
                new Font("Segoe UI", Font.BOLD, 12), 
                new Color(73, 80, 87)
        ));

        inputField = new JTextField("How frequent are nouns?");
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        translateButton = new JButton("Compile to CQL");
        translateButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        translateButton.setBackground(new Color(13, 110, 253)); // Royal blue brand color
        translateButton.setForeground(Color.BLACK);
        translateButton.setFocusPainted(false);
        translateButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        inputWrapper.add(inputField, BorderLayout.CENTER);
        inputWrapper.add(translateButton, BorderLayout.EAST);
        centralPanel.add(inputWrapper);
        centralPanel.add(Box.createVerticalStrut(15));

        JPanel outputWrapper = new JPanel(new GridBagLayout());
        outputWrapper.setOpaque(false);
        outputWrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(218, 224, 233), 1, true), 
                " Result ", 
                0, 0, 
                new Font("Segoe UI", Font.BOLD, 12), 
                new Color(73, 80, 87)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 12, 8, 12);

        // CQL output label and field
        JLabel cqlLabel = new JLabel("Generated CQL Expression:");
        cqlLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.0;
        outputWrapper.add(cqlLabel, gbc);

        cqlOutputField = new JTextField();
        cqlOutputField.setFont(new Font("Consolas", Font.BOLD, 15));
        cqlOutputField.setEditable(false);
        cqlOutputField.setBackground(new Color(233, 236, 239)); // Gray output state
        cqlOutputField.setForeground(new Color(15, 23, 42));
        cqlOutputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridy = 1;
        outputWrapper.add(cqlOutputField, gbc);

        // Explanation field label
        JLabel explanationLabel = new JLabel("Explanation:");
        explanationLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 2;
        outputWrapper.add(explanationLabel, gbc);

        explanationArea = new JTextArea();
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setBackground(new Color(248, 249, 250));
        explanationArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane scrollPane = new JScrollPane(explanationArea);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        gbc.gridy = 3; gbc.weighty = 1.0;
        outputWrapper.add(scrollPane, gbc);

        centralPanel.add(outputWrapper);
        rootPanel.add(centralPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        validationLabel = new JLabel("Syntax Valid: N/A ");
        validationLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        validationLabel.setForeground(new Color(108, 117, 125));
        validationLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        bottomPanel.add(validationLabel, BorderLayout.EAST);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(rootPanel);

        translateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                triggerTranslation();
            }
        });

        // Enable translating by pressing Enter in the query input field
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                triggerTranslation();
            }
        });

        frame.setVisible(true);
    }

    private void triggerTranslation() {
        String queryText = inputField.getText().trim();
        if (queryText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please type an English query first.", "Empty Query", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lock interface state during LLM pipeline execution
        translateButton.setEnabled(false);
        inputField.setEnabled(false);
        translateButton.setText("Compiling...");
        cqlOutputField.setText("");
        explanationArea.setText("");
        
        validationLabel.setText("Syntax Valid: CHECKING ");
        validationLabel.setForeground(new Color(108, 117, 125));

        SwingWorker<TranslationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected TranslationResult doInBackground() {
                // Sends request down our modular langchain4j backend pipeline
                return translator.translateToCql(queryText);
            }

            @Override
            protected void done() {
                try {
                    TranslationResult result = get();
                    cqlOutputField.setText(result.getCql());
                    explanationArea.setText(result.getExplanation());

                    boolean isValid = CqlValidator.isValid(result.getCql());
                    if (isValid) {
                        validationLabel.setText("Syntax Valid: PASSED ");
                        validationLabel.setForeground(new Color(40, 167, 69)); // Clean green
                    } else {
                        validationLabel.setText("Syntax Valid: FAILED ");
                        validationLabel.setForeground(new Color(220, 53, 69)); // Danger red
                    }


                } catch (Exception ex) {
                    cqlOutputField.setText("Error connecting to local engine.");
                    explanationArea.setText("Error detail: " + ex.getMessage() + "\n\nIs Ollama running? Try starting 'ollama run phi3' in your terminal.");
                    validationLabel.setText("Syntax Valid: ERROR ");
                    validationLabel.setForeground(new Color(220, 53, 69));
                } finally {
                    // Unlock interface components
                    translateButton.setEnabled(true);
                    inputField.setEnabled(true);
                    translateButton.setText("Compile to CQL");
                }
            }
        };

        worker.execute();
    }

    public static void main(String[] args) {
        // Run graphical desktop layout on AWT event dispatch thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new App();
            }
        });
    }
}