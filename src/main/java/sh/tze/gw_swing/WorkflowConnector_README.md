# WorkflowConnector - SwingWorker-based Web Scraping Connector

## Overview

The `WorkflowConnector` class extends `SwingWorker<String, Void>` to handle long-running Wikipedia scraping operations off the Event Dispatch Thread (EDT). This ensures the UI remains responsive while performing I/O operations.

## Key Features

1. **Non-blocking I/O**: Scraping operations run on a background thread
2. **Progress Tracking**: Built-in progress reporting (0-100%)
3. **State Management**: Clear workflow states (IDLE, VALIDATING, SCRAPING, etc.)
4. **Property Change Support**: UI components can bind to state and progress changes
5. **URL Validation**: Built-in validation via `WebScrapingInterface`
6. **Clean Architecture**: Maintains separation between UI and worker threads

## Usage Example

### Basic Usage

```java
// Create a connector for a Wikipedia URL
WorkflowConnector connector = new WorkflowConnector("https://en.wikipedia.org/wiki/Java_(programming_language)");

// Add listeners for progress updates
connector.addPropertyChangeListener("progress", evt -> {
    int progress = (Integer) evt.getNewValue();
    progressBar.setValue(progress);
});

// Add listeners for workflow state changes
connector.addWorkflowListener(evt -> {
    if (WorkflowConnector.STATE_PROPERTY.equals(evt.getPropertyName())) {
        WorkflowState state = (WorkflowState) evt.getNewValue();
        statusLabel.setText(state.getDescription());
    }
});

// Execute the scraping task
connector.execute();

// Get results when done
try {
    String scrapedContent = connector.get(); // Blocks until complete
} catch (Exception e) {
    // Handle errors
}
```

### Advanced Usage with Options

```java
// Create connector with additional options
boolean includeMetadata = true;
boolean cleanText = true;
WorkflowConnector connector = new WorkflowConnector(url, includeMetadata, cleanText);
```

## Workflow States

The connector progresses through the following states:

- `IDLE`: Initial state, ready to start
- `VALIDATING`: Validating the Wikipedia URL
- `SCRAPING`: Fetching content from Wikipedia
- `PROCESSING`: Processing and cleaning scraped content
- `COMPLETED`: Successfully finished
- `FAILED`: An error occurred

## Property Change Events

The connector fires the following property change events:

1. **Standard SwingWorker Properties**:
   - `"progress"`: Integer value 0-100
   - `"state"`: SwingWorker.StateValue (PENDING, STARTED, DONE)

2. **Custom Workflow Properties** (via `addWorkflowListener`):
   - `WorkflowConnector.STATE_PROPERTY`: WorkflowState enum value
   - `WorkflowConnector.MESSAGE_PROPERTY`: Status message string

## Integration with UI Components

### Progress Bar Binding

```java
connector.addPropertyChangeListener("progress", evt -> {
    SwingUtilities.invokeLater(() -> 
        progressBar.setValue((Integer) evt.getNewValue())
    );
});
```

### Status Label Binding

```java
connector.addWorkflowListener(WorkflowConnector.STATE_PROPERTY, evt -> {
    WorkflowState state = (WorkflowState) evt.getNewValue();
    SwingUtilities.invokeLater(() -> 
        statusLabel.setText(state.getDescription())
    );
});
```

### Enable/Disable UI During Operation

```java
connector.addWorkflowListener(WorkflowConnector.STATE_PROPERTY, evt -> {
    WorkflowState state = (WorkflowState) evt.getNewValue();
    SwingUtilities.invokeLater(() -> {
        boolean isRunning = state != WorkflowState.IDLE && 
                           state != WorkflowState.COMPLETED && 
                           state != WorkflowState.FAILED;
        submitButton.setEnabled(!isRunning);
        urlField.setEditable(!isRunning);
    });
});
```

## Error Handling

The connector provides robust error handling:

```java
connector.addPropertyChangeListener("state", evt -> {
    if (SwingWorker.StateValue.DONE == evt.getNewValue()) {
        try {
            String result = connector.get();
            // Process successful result
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                // Invalid URL
                JOptionPane.showMessageDialog(null, cause.getMessage());
            } else if (cause instanceof RuntimeException) {
                // Scraping failed
                JOptionPane.showMessageDialog(null, "Scraping failed: " + cause.getMessage());
            }
        } catch (InterruptedException e) {
            // Task was cancelled
        }
    }
});
```

## Cancellation Support

```java
// Cancel a running operation
if (connector != null && !connector.isDone()) {
    connector.cancel(true);
}

// Check if cancelled in completion handler
if (connector.isCancelled()) {
    statusLabel.setText("Operation cancelled");
}
```

## Future Extensions

The WorkflowConnector is designed to be extensible. Future options that can be added:

- Proxy configuration
- Custom user agents
- Timeout settings
- Multiple URL batch processing
- Different output formats (JSON, XML, etc.)
- Caching support

## Thread Safety

- All UI updates must be performed on the EDT using `SwingUtilities.invokeLater()`
- The `doInBackground()` method runs on a worker thread
- Property change events are fired on the EDT for SwingWorker properties
- Custom workflow events may be fired from the worker thread

## Complete Example

See `WorkflowConnectorExample.java` for a complete working example with a GUI that demonstrates all features of the WorkflowConnector.
