package src;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NoteApp - A simple note-taking application with a graphical user interface.
 * This application allows users to create, save, edit, and delete notes.
 * Notes are stored as text files in a 'notes' directory.
 */
public class NoteApp extends JFrame {
    // GUI Components
    private JTextArea noteArea;        // Main text area for note content
    private JList<String> noteList;    // List showing all note titles
    private DefaultListModel<String> listModel;  // Model for the note list
    private List<Note> notes;          // List to store Note objects in memory
    private JTextField titleField;     // Text field for note title
    private JTextField searchField;    // Text field for searching notes
    private JLabel dateLabel;          // Label to show note dates
    private JLabel statusLabel;        // Label to show application status
    private JComboBox<String> categoryComboBox;  // Combo box for selecting note category
    private JButton boldButton, italicButton, underlineButton;  // Formatting buttons
    private JColorChooser colorChooser;  // Color chooser for text formatting
    private JPanel toolbarPanel;        // Panel for toolbar buttons
    private static final String NOTES_DIR = "notes";  // Directory to store note files
    private static final String[] CATEGORIES = {"All", "Work", "Personal", "Ideas", "Tasks", "Other"};
    
    // Modern color scheme - non-final for theme switching
    private static Color BACKGROUND_COLOR = new Color(250, 250, 250);
    private static Color ACCENT_COLOR = new Color(41, 128, 185);
    private static Color TEXT_COLOR = new Color(44, 62, 80);
    private static Color BORDER_COLOR = new Color(236, 240, 241);
    private static Color HOVER_COLOR = new Color(236, 240, 241);
    
    // Modern fonts
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // Modern dimensions
    private static final int CORNER_RADIUS = 8;
    private static final int PADDING = 12;
    private static final int COMPONENT_HEIGHT = 36;

    /**
     * Constructor initializes the application and sets up the UI
     */
    public NoteApp() {
        notes = new ArrayList<>();
        setupUI();
        loadNotes();  // Load existing notes from the notes directory
        setupKeyboardShortcuts();
        setupModernLookAndFeel();
    }

    private void setupKeyboardShortcuts() {
        // Global keyboard shortcuts
        KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlD = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

        getRootPane().registerKeyboardAction(e -> newNote(), ctrlN, JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> saveNote(), ctrlS, JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> deleteNote(), ctrlD, JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> searchField.requestFocus(), ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> clearSearch(), escape, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void clearSearch() {
        searchField.setText("");
        updateNoteList();
        noteArea.requestFocus();
    }

    private void setupModernLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Modern UI defaults
            UIManager.put("Button.arc", CORNER_RADIUS);
            UIManager.put("Component.arc", CORNER_RADIUS);
            UIManager.put("ProgressBar.arc", CORNER_RADIUS);
            UIManager.put("TextComponent.arc", CORNER_RADIUS);
            
            // Modern colors
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.foreground", TEXT_COLOR);
            UIManager.put("Button.select", HOVER_COLOR);
            
            UIManager.put("ComboBox.background", Color.WHITE);
            UIManager.put("ComboBox.foreground", TEXT_COLOR);
            UIManager.put("ComboBox.selectionBackground", ACCENT_COLOR);
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            
            UIManager.put("List.background", Color.WHITE);
            UIManager.put("List.foreground", TEXT_COLOR);
            UIManager.put("List.selectionBackground", ACCENT_COLOR);
            UIManager.put("List.selectionForeground", Color.WHITE);
            
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", TEXT_COLOR);
            UIManager.put("TextField.caretForeground", ACCENT_COLOR);
            
            UIManager.put("TextArea.background", Color.WHITE);
            UIManager.put("TextArea.foreground", TEXT_COLOR);
            UIManager.put("TextArea.caretForeground", ACCENT_COLOR);
            
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JTextField createModernTextField() {
        JTextField field = new JTextField();
        field.setFont(MAIN_FONT);
        field.setForeground(TEXT_COLOR);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(field.getWidth(), COMPONENT_HEIGHT));
        return field;
    }

    private JTextArea createModernTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(MAIN_FONT);
        area.setForeground(TEXT_COLOR);
        area.setBackground(Color.WHITE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return area;
    }

    private JButton createActionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(MAIN_FONT);
        button.setForeground(TEXT_COLOR);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        button.setPreferredSize(new Dimension(button.getWidth(), COMPONENT_HEIGHT));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HOVER_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        
        button.addActionListener(listener);
        return button;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        buttonPanel.add(createActionButton("New Note", e -> newNote()));
        buttonPanel.add(createActionButton("Save", e -> saveNote()));
        buttonPanel.add(createActionButton("Delete", e -> deleteNote()));
        buttonPanel.add(createActionButton("Export", e -> exportNote()));
        buttonPanel.add(createActionButton("Import", e -> importNote()));
        buttonPanel.add(createActionButton("Settings", e -> showSettings()));
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(dateLabel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void showSettings() {
        // Create settings dialog
        JDialog settingsDialog = new JDialog(this, "Settings", true);
        settingsDialog.setLayout(new BorderLayout(10, 10));
        settingsDialog.getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Settings panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Font size setting
        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsPanel.add(new JLabel("Font Size:"), gbc);
        gbc.gridx = 1;
        JSpinner fontSizeSpinner = new JSpinner(new SpinnerNumberModel(14, 8, 24, 1));
        settingsPanel.add(fontSizeSpinner, gbc);
        
        // Theme setting
        gbc.gridx = 0;
        gbc.gridy = 1;
        settingsPanel.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> themeCombo = new JComboBox<>(new String[]{"Light", "Dark"});
        settingsPanel.add(themeCombo, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton applyButton = new JButton("Apply");
        JButton cancelButton = new JButton("Cancel");
        
        applyButton.addActionListener(e -> {
            // Apply settings
            int newSize = (Integer) fontSizeSpinner.getValue();
            String theme = (String) themeCombo.getSelectedItem();
            updateSettings(newSize, theme);
            settingsDialog.dispose();
        });
        
        cancelButton.addActionListener(e -> settingsDialog.dispose());
        
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        
        settingsDialog.add(settingsPanel, BorderLayout.CENTER);
        settingsDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        settingsDialog.pack();
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setVisible(true);
    }

    private void updateSettings(int fontSize, String theme) {
        // Update font size
        Font newFont = new Font(MAIN_FONT.getName(), MAIN_FONT.getStyle(), fontSize);
        noteArea.setFont(newFont);
        titleField.setFont(newFont);
        searchField.setFont(newFont);
        noteList.setFont(newFont);
        
        // Update theme
        if (theme.equals("Dark")) {
            BACKGROUND_COLOR = new Color(30, 30, 30);
            TEXT_COLOR = Color.WHITE;
            BORDER_COLOR = new Color(60, 60, 60);
        } else {
            BACKGROUND_COLOR = new Color(250, 250, 250);
            TEXT_COLOR = new Color(44, 62, 80);
            BORDER_COLOR = new Color(236, 240, 241);
        }
        
        // Update component colors
        updateComponentColors();
    }

    private void updateComponentColors() {
        getContentPane().setBackground(BACKGROUND_COLOR);
        noteArea.setBackground(BACKGROUND_COLOR);
        noteArea.setForeground(TEXT_COLOR);
        titleField.setBackground(BACKGROUND_COLOR);
        titleField.setForeground(TEXT_COLOR);
        searchField.setBackground(BACKGROUND_COLOR);
        searchField.setForeground(TEXT_COLOR);
        noteList.setBackground(BACKGROUND_COLOR);
        noteList.setForeground(TEXT_COLOR);
    }

    /**
     * Sets up the graphical user interface components and layout
     */
    private void setupUI() {
        setTitle("Modern Note Taking App");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND_COLOR);

        initializeComponents();
        
        JPanel mainPanel = createModernPanel();
        mainPanel.setLayout(new BorderLayout(PADDING, PADDING));

        // Left panel with shadow
        JPanel leftPanel = createModernPanel();
        leftPanel.setLayout(new BorderLayout(PADDING, PADDING));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));
        leftPanel.add(createSearchPanel(), BorderLayout.NORTH);
        leftPanel.add(createCategoryPanel(), BorderLayout.CENTER);
        leftPanel.add(createModernScrollPane(noteList), BorderLayout.SOUTH);

        // Center panel with shadow
        JPanel centerPanel = createModernPanel();
        centerPanel.setLayout(new BorderLayout(PADDING, PADDING));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));
        centerPanel.add(createTitlePanel(), BorderLayout.NORTH);
        centerPanel.add(createModernScrollPane(noteArea), BorderLayout.CENTER);

        // Right panel with shadow
        JPanel rightPanel = createModernPanel();
        rightPanel.setLayout(new BorderLayout(PADDING, PADDING));
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));
        rightPanel.add(createRightPanel(), BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        add(mainPanel);
        setJMenuBar(createModernMenuBar());
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 700));
    }

    private void initializeComponents() {
        titleField = createModernTextField();
        noteArea = createModernTextArea();
        searchField = createModernTextField();
        dateLabel = new JLabel();
        statusLabel = new JLabel("Ready");
        listModel = new DefaultListModel<>();
        noteList = new JList<>(listModel);
        categoryComboBox = createModernComboBox(CATEGORIES);
        
        // Initialize formatting buttons
        boldButton = new JButton("B");
        italicButton = new JButton("I");
        underlineButton = new JButton("U");
        
        // Style the buttons
        styleFormatButtons();
    }

    private void styleFormatButtons() {
        Font boldFont = new Font("Arial", Font.BOLD, 12);
        Font italicFont = new Font("Arial", Font.ITALIC, 12);
        Font underlineFont = new Font("Arial", Font.PLAIN, 12);
        
        boldButton.setFont(boldFont);
        italicButton.setFont(italicFont);
        underlineButton.setFont(underlineFont);
        
        Dimension buttonSize = new Dimension(30, 30);
        boldButton.setPreferredSize(buttonSize);
        italicButton.setPreferredSize(buttonSize);
        underlineButton.setPreferredSize(buttonSize);
    }

    private JPanel createSearchPanel() {
        JPanel panel = createModernPanel();
        panel.setLayout(new BorderLayout(PADDING, 0));
        
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(MAIN_FONT);
        searchField = createModernTextField();
        
        panel.add(searchIcon, BorderLayout.WEST);
        panel.add(searchField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCategoryPanel() {
        JPanel panel = createModernPanel();
        panel.setLayout(new BorderLayout(PADDING, 0));
        
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(MAIN_FONT);
        categoryComboBox = createModernComboBox(CATEGORIES);
        
        panel.add(categoryLabel, BorderLayout.WEST);
        panel.add(categoryComboBox, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTitlePanel() {
        JPanel panel = createModernPanel();
        panel.setLayout(new BorderLayout(PADDING, 0));
        
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(MAIN_FONT);
        titleField = createModernTextField();
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(titleField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionButtons() {
        JPanel panel = createModernPanel();
        panel.setLayout(new GridLayout(5, 1, PADDING, PADDING));
        
        panel.add(createModernButton("New Note", "➕", e -> newNote()));
        panel.add(createModernButton("Save", "💾", e -> saveNote()));
        panel.add(createModernButton("Delete", "🗑️", e -> deleteNote()));
        panel.add(createModernButton("Export", "📤", e -> exportNote()));
        panel.add(createModernButton("Import", "📥", e -> importNote()));
        
        return panel;
    }

    private JLabel createDateLabel() {
        dateLabel = new JLabel();
        dateLabel.setFont(MAIN_FONT.deriveFont(Font.ITALIC));
        dateLabel.setForeground(TEXT_COLOR);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return dateLabel;
    }

    private JPanel createStatusBar() {
        JPanel panel = createModernPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, PADDING, 8, PADDING)
        ));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(MAIN_FONT);
        statusLabel.setForeground(TEXT_COLOR);
        panel.add(statusLabel, BorderLayout.WEST);
        
        return panel;
    }

    private JMenuBar createModernMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(BACKGROUND_COLOR);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(MAIN_FONT);
        fileMenu.add(createModernMenuItem("New Note", "Ctrl+N", KeyEvent.VK_N, e -> newNote()));
        fileMenu.add(createModernMenuItem("Save", "Ctrl+S", KeyEvent.VK_S, e -> saveNote()));
        fileMenu.add(createModernMenuItem("Delete", "Ctrl+D", KeyEvent.VK_D, e -> deleteNote()));
        fileMenu.addSeparator();
        fileMenu.add(createModernMenuItem("Exit", "Alt+F4", KeyEvent.VK_F4, e -> System.exit(0)));
        
        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setFont(MAIN_FONT);
        editMenu.add(createModernMenuItem("Cut", "Ctrl+X", KeyEvent.VK_X, e -> noteArea.cut()));
        editMenu.add(createModernMenuItem("Copy", "Ctrl+C", KeyEvent.VK_C, e -> noteArea.copy()));
        editMenu.add(createModernMenuItem("Paste", "Ctrl+V", KeyEvent.VK_V, e -> noteArea.paste()));
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setFont(MAIN_FONT);
        viewMenu.add(createModernMenuItem("Zoom In", "Ctrl++", KeyEvent.VK_PLUS, e -> zoomIn()));
        viewMenu.add(createModernMenuItem("Zoom Out", "Ctrl+-", KeyEvent.VK_MINUS, e -> zoomOut()));
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        
        return menuBar;
    }

    private JMenuItem createModernMenuItem(String text, String accelerator, int keyCode, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(MAIN_FONT);
        item.setAccelerator(KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(listener);
        return item;
    }

    private void zoomIn() {
        Font currentFont = noteArea.getFont();
        noteArea.setFont(currentFont.deriveFont((float) (currentFont.getSize() + 2)));
        statusLabel.setText("Zoom: " + (currentFont.getSize() + 2) + "%");
    }

    private void zoomOut() {
        Font currentFont = noteArea.getFont();
        if (currentFont.getSize() > 8) {
            noteArea.setFont(currentFont.deriveFont((float) (currentFont.getSize() - 2)));
            statusLabel.setText("Zoom: " + (currentFont.getSize() - 2) + "%");
        }
    }

    /**
     * Searches notes based on the search field text
     */
    private void searchNotes() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            updateNoteList();
            return;
        }

        List<Note> filteredNotes = notes.stream()
            .filter(note -> note.getTitle().toLowerCase().contains(searchText) ||
                          note.getContent().toLowerCase().contains(searchText))
            .collect(Collectors.toList());

        listModel.clear();
        for (Note note : filteredNotes) {
            listModel.addElement(note.getTitle());
        }
    }

    /**
     * Creates a new empty note by clearing the title and content fields
     */
    private void newNote() {
        titleField.setText("");
        noteArea.setText("");
        noteList.clearSelection();
    }

    /**
     * Saves the current note to both memory and file system
     * Shows an error message if the title is empty
     */
    private void saveNote() {
        String title = titleField.getText().trim();
        String content = noteArea.getText();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title for the note.");
            return;
        }

        Note note = new Note(title, content);
        int selectedIndex = noteList.getSelectedIndex();
        
        // Update existing note or add new one
        if (selectedIndex != -1) {
            notes.set(selectedIndex, note);
        } else {
            notes.add(note);
        }

        saveToFile(note);
        updateNoteList();
    }

    /**
     * Deletes the currently selected note from both memory and file system
     */
    private void deleteNote() {
        int selectedIndex = noteList.getSelectedIndex();
        if (selectedIndex != -1) {
            Note note = notes.get(selectedIndex);
            File noteFile = new File(NOTES_DIR, note.getTitle() + ".txt");
            noteFile.delete();
            notes.remove(selectedIndex);
            updateNoteList();
            newNote();
        }
    }

    /**
     * Loads the selected note's content into the editor
     */
    private void loadSelectedNote() {
        int selectedIndex = noteList.getSelectedIndex();
        if (selectedIndex != -1) {
            Note note = notes.get(selectedIndex);
            titleField.setText(note.getTitle());
            noteArea.setText(note.getContent());
            dateLabel.setText(String.format("Created: %s | Last Modified: %s",
                Note.formatDate(note.getCreationDate()),
                Note.formatDate(note.getLastModifiedDate())));
        }
    }

    /**
     * Updates the note list display with current notes
     */
    private void updateNoteList() {
        listModel.clear();
        for (Note note : notes) {
            listModel.addElement(note.getTitle());
        }
    }

    /**
     * Loads all existing notes from the notes directory into memory
     */
    private void loadNotes() {
        File notesDir = new File(NOTES_DIR);
        if (notesDir.exists()) {
            File[] files = notesDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String title = file.getName().replace(".txt", "");
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        Note note = new Note(title, content.toString(), file.lastModified(), file.lastModified());
                        notes.add(note);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                updateNoteList();
            }
        }
    }

    /**
     * Saves a note to a text file in the notes directory
     * @param note The note to be saved
     */
    private void saveToFile(Note note) {
        File noteFile = new File(NOTES_DIR, note.getTitle() + ".txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(noteFile))) {
            writer.write(note.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportNote() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.println("Title: " + titleField.getText());
                writer.println("Date: " + dateLabel.getText());
                writer.println("\nContent:\n" + noteArea.getText());
                statusLabel.setText("Note exported successfully");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting note: " + e.getMessage());
            }
        }
    }

    private void importNote() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                noteArea.setText(content.toString());
                statusLabel.setText("Note imported successfully");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error importing note: " + e.getMessage());
            }
        }
    }

    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NoteApp app = new NoteApp();
            app.setVisible(true);
        });
    }
} 