package sh.tze.gw_swing;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Example demonstrating how to use WorkflowConnector with Swing UI components.
 * Shows how to bind UI widgets to the connector's progress and state changes.
 */
public class WorkflowConnectorExample extends JFrame {
    
    private JTextField urlField;
    private JButton scrapeButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTextArea resultArea;
    private WorkflowConnector currentConnector;
    
    public WorkflowConnectorExample() {
        setTitle("WorkflowConnector Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initializeUI();
        pack();
        setLocationRelativeTo(null);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Top panel with URL input
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        urlField = new JTextField("https://en.wikipedia.org/wiki/Java_(programming_language)");
        scrapeButton = new JButton("Scrape");
        
        topPanel.add(new JLabel("Wikipedia URL:"), BorderLayout.WEST);
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(scrapeButton, BorderLayout.EAST);
        
        // Middle panel with progress
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        statusLabel = new JLabel("Ready");
        
        progressPanel.add(statusLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        // Bottom panel with results
        resultArea = new JTextArea(20, 60);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Results"));
        
        // Add all panels
        add(topPanel, BorderLayout.NORTH);
        add(progressPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
        
        // Button action
        scrapeButton.addActionListener(e -> startScraping());
    }
    
    private void startScraping() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a URL", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Cancel any existing connector
        if (currentConnector != null && !currentConnector.isDone()) {
            currentConnector.cancel(true);
        }
        
        // Create new connector with options
        currentConnector = new WorkflowConnector(url, true, true);
        
        // Add property change listeners
        // Listen to standard SwingWorker properties (progress, state)
        currentConnector.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                
                if ("progress".equals(propertyName)) {
                    // Update progress bar
                    int progress = (Integer) evt.getNewValue();
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                    
                } else if ("state".equals(propertyName) && 
                           SwingWorker.StateValue.DONE == evt.getNewValue()) {
                    // Task completed - get results
                    handleCompletion();
                }
            }
        });
        
        // Listen to custom workflow properties
        currentConnector.addWorkflowListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                
                if (WorkflowConnector.STATE_PROPERTY.equals(propertyName)) {
                    // Update status based on state
                    WorkflowConnector.WorkflowState newState = 
                        (WorkflowConnector.WorkflowState) evt.getNewValue();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText(newState.getDescription());
                        
                        // Update UI based on state
                        switch (newState) {
                            case IDLE:
                            case COMPLETED:
                            case FAILED:
                                scrapeButton.setEnabled(true);
                                urlField.setEditable(true);
                                break;
                            default:
                                scrapeButton.setEnabled(false);
                                urlField.setEditable(false);
                                break;
                        }
                    });
                    
                } else if (WorkflowConnector.MESSAGE_PROPERTY.equals(propertyName)) {
                    // Update detailed status message
                    String message = (String) evt.getNewValue();
                    SwingUtilities.invokeLater(() -> {
                        resultArea.append(message + "\n");
                        resultArea.setCaretPosition(resultArea.getDocument().getLength());
                    });
                }
            }
        });
        
        // Clear previous results
        resultArea.setText("");
        
        // Start the worker
        currentConnector.execute();
        scrapeButton.setEnabled(false);
        urlField.setEditable(false);
    }
    
    private void handleCompletion() {
        try {
            if (currentConnector.isCancelled()) {
                resultArea.append("\n\nOperation cancelled by user.\n");
            } else {
                String result = currentConnector.get();
                resultArea.append("\n" + result + "\n");
                
                // Show scraped title in status
                String title = currentConnector.getScrapedTitle();
                if (!title.isEmpty()) {
                    statusLabel.setText("Completed: " + title);
                }
            }
        } catch (Exception e) {
            resultArea.append("\n\nError: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        
        // Re-enable UI
        scrapeButton.setEnabled(true);
        urlField.setEditable(true);
        progressBar.setValue(0);
    }
    
    /**
     * Example main method to demonstrate the WorkflowConnector.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WorkflowConnectorExample example = new WorkflowConnectorExample();
            example.setVisible(true);
        });
    }
}
