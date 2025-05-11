package src;
import javax.swing.*;

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
    private static final String NOTES_DIR = "notes";  // Directory to store note files

    /**
     * Constructor initializes the application and sets up the UI
     */
    public NoteApp() {
        notes = new ArrayList<>();
        setupUI();
        loadNotes();  // Load existing notes from the notes directory
    }

    /**
     * Sets up the graphical user interface components and layout
     */
    private void setupUI() {
        // Basic window setup
        setTitle("Note Taking App");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize UI components
        titleField = new JTextField();
        noteArea = new JTextArea();
        searchField = new JTextField();
        dateLabel = new JLabel();
        listModel = new DefaultListModel<>();
        noteList = new JList<>(listModel);
        JScrollPane noteScrollPane = new JScrollPane(noteArea);  // Scrollable note area
        JScrollPane listScrollPane = new JScrollPane(noteList);  // Scrollable note list

        // Create control buttons
        JButton saveButton = new JButton("Save");
        JButton newButton = new JButton("New");
        JButton deleteButton = new JButton("Delete");
        JButton searchButton = new JButton("Search");

        // Setup title panel at the top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Title: "), BorderLayout.WEST);
        topPanel.add(titleField, BorderLayout.CENTER);

        // Setup search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Setup button panel at the bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(newButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);

        // Add all panels to the main window
        add(topPanel, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.SOUTH);
        add(noteScrollPane, BorderLayout.CENTER);
        add(listScrollPane, BorderLayout.WEST);
        add(buttonPanel, BorderLayout.EAST);
        add(dateLabel, BorderLayout.SOUTH);

        // Add event listeners for buttons and note selection
        saveButton.addActionListener(e -> saveNote());
        newButton.addActionListener(e -> newNote());
        deleteButton.addActionListener(e -> deleteNote());
        searchButton.addActionListener(e -> searchNotes());
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchNotes();
            }
        });
        noteList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedNote();
            }
        });

        // Create the notes directory if it doesn't exist
        new File(NOTES_DIR).mkdirs();
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