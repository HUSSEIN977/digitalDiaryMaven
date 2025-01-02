package ui;

import data.DiaryManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DiaryEntry;
import model.User;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DigitalDiaryUI {

    private final Stage primaryStage;
    private final User loggedInUser;

    // UI Controls
    private TextField titleField;
    private TextArea contentArea;
    private ListView<DiaryEntry> diaryListView;
    private ImageView coverImageView;
    private TextField searchField;
    private ComboBox<String> moodComboBox;
    private Label motivationalQuoteLabel;
    private Button saveEditButton;

    // Keep track of the chosen image file (to store path)
    private File chosenImageFile = null;

    // In-memory list of diaries for the logged-in user
    private List<DiaryEntry> diaryEntries;
    private DiaryEntry currentEditingEntry = null; // Track the entry being edited

    public DigitalDiaryUI(Stage primaryStage, User user) {
        this.primaryStage = primaryStage;
        this.loggedInUser = user;
    }

    public void showDiaryScene() {
        // Load diaries for the user
        diaryEntries = DiaryManager.loadDiariesForUser(loggedInUser.getUsername());

        titleField = new TextField();
        titleField.setPromptText("Enter title");

        contentArea = new TextArea();
        contentArea.setPromptText("Enter content");

        coverImageView = new ImageView();
        coverImageView.setFitHeight(100);
        coverImageView.setFitWidth(100);

        diaryListView = new ListView<>();
        diaryListView.setCellFactory(param -> new DiaryEntryCell());
        diaryListView.getItems().addAll(diaryEntries);

        searchField = new TextField();
        searchField.setPromptText("Search by title or content");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterDiaryList(newVal));

        moodComboBox = new ComboBox<>();
        moodComboBox.getItems().addAll("Happy", "Sad", "Excited", "Angry", "Neutral");
        moodComboBox.setValue("Neutral");

        // Label to display motivational quotes
        motivationalQuoteLabel = new Label();
        motivationalQuoteLabel.setWrapText(true); // Allows multi-line text

        // Update quote when mood changes
        moodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            String quote = MotivationalQuotes.getMotivationalQuote(newVal);
            motivationalQuoteLabel.setText(quote);
        });

        HBox moodAndQuoteBox = new HBox(10, new Label("Mood:"), moodComboBox, motivationalQuoteLabel);
        moodAndQuoteBox.setSpacing(10);

        Button saveButton = new Button("Save Diary Entry");
        Button editButton = new Button("Edit Diary Entry");
        saveEditButton = new Button("Save Changes");
        saveEditButton.setDisable(true); // Initially disabled

        Button deleteButton = new Button("Delete Diary Entry");
        Button uploadButton = new Button("Upload Cover Image");
        Button sortButton = new Button("Sort by Date");
        Button pdfExportButton = new Button("Export to PDF");
        Button logoutButton = new Button("Logout");

        saveButton.setOnAction(e -> saveDiaryEntry());
        editButton.setOnAction(e -> editDiaryEntry());
        saveEditButton.setOnAction(e -> saveEditedDiaryEntry());
        deleteButton.setOnAction(e -> deleteDiaryEntry());
        uploadButton.setOnAction(e -> uploadCoverImage());
        sortButton.setOnAction(e -> sortDiariesByDate());
        pdfExportButton.setOnAction(e -> exportToPDF());
        logoutButton.setOnAction(e -> logout());

        HBox editControls = new HBox(10, editButton, saveEditButton);

        VBox root = new VBox(10,
                new Label("Title:"), titleField,
                new Label("Content:"), contentArea,
                moodAndQuoteBox,
                uploadButton, coverImageView,
                new Label("Diary Entries:"), searchField, diaryListView,
                saveButton, editControls, deleteButton, sortButton, pdfExportButton,
                logoutButton
        );
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 600, 650);
        primaryStage.setTitle("Digital Diary - Logged in as " + loggedInUser.getUsername());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void saveDiaryEntry() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String mood = moodComboBox.getValue();
        Image coverImg = coverImageView.getImage();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter both title and content.");
            return;
        }

        // Create new entry
        String imagePath = (chosenImageFile != null) ? chosenImageFile.getAbsolutePath() : "";

        DiaryEntry newEntry = new DiaryEntry(
                loggedInUser.getUsername(),
                title,
                content,
                coverImg,
                mood,
                LocalDateTime.now(),
                imagePath
        );

        // Update in-memory + CSV
        diaryEntries.add(newEntry);
        diaryListView.getItems().add(newEntry);
        DiaryManager.addDiaryEntry(newEntry);

        clearInputs();
    }

    private void editDiaryEntry() {
        DiaryEntry selectedEntry = diaryListView.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a diary entry to edit.");
            return;
        }

        // Populate the fields with the selected entry's data
        titleField.setText(selectedEntry.getTitle());
        contentArea.setText(selectedEntry.getContent());
        moodComboBox.setValue(selectedEntry.getMood());
        coverImageView.setImage(selectedEntry.getCoverImage());

        // Keep the entry in the list while editing
        currentEditingEntry = selectedEntry;

        // Enable the save changes button
        saveEditButton.setDisable(false);
    }

    private void saveEditedDiaryEntry() {
        if (currentEditingEntry == null) {
            showAlert(Alert.AlertType.WARNING, "No Editing", "No diary entry is currently being edited.");
            return;
        }

        String newTitle = titleField.getText().trim();
        String newContent = contentArea.getText().trim();
        String newMood = moodComboBox.getValue();
        Image newCoverImg = coverImageView.getImage();

        if (newTitle.isEmpty() || newContent.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter both title and content.");
            return;
        }

        // Update the selected entry's properties
        currentEditingEntry.setTitle(newTitle);
        currentEditingEntry.setContent(newContent);
        currentEditingEntry.setMood(newMood);
        currentEditingEntry.setCoverImage(newCoverImg);

        if (chosenImageFile != null) {
            currentEditingEntry.setImagePath(chosenImageFile.getAbsolutePath());
        }

        // Refresh the ListView to reflect changes
        diaryListView.refresh();

        // Update the entry in the CSV file
        DiaryManager.updateDiaryEntry(currentEditingEntry, currentEditingEntry);

        // Clear inputs and disable save button
        clearInputs();
        saveEditButton.setDisable(true);
        currentEditingEntry = null;
    }

    private void deleteDiaryEntry() {
        DiaryEntry selectedEntry = diaryListView.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a diary entry to delete.");
            return;
        }

        diaryEntries.remove(selectedEntry);
        diaryListView.getItems().remove(selectedEntry);
        DiaryManager.deleteDiaryEntry(selectedEntry);
    }

    private void uploadCoverImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            Image chosenImg = new Image(selectedFile.toURI().toString());
            coverImageView.setImage(chosenImg);
            chosenImageFile = selectedFile; // Store the file for saving later
        }
    }

    private void sortDiariesByDate() {
        diaryEntries.sort(Comparator.comparing(DiaryEntry::getCreatedDate).reversed());
        diaryListView.getItems().setAll(diaryEntries);
    }

    private void filterDiaryList(String query) {
        String lowerQuery = query.toLowerCase();

        List<DiaryEntry> filtered = diaryEntries.stream()
                .filter(e ->
                        e.getTitle().toLowerCase().contains(lowerQuery) ||
                                e.getContent().toLowerCase().contains(lowerQuery)
                )
                .collect(Collectors.toList());

        diaryListView.getItems().setAll(filtered);
    }

    private void exportToPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File outFile = fileChooser.showSaveDialog(primaryStage);

        if (outFile != null) {
            PdfExporter.exportEntriesToPDF(diaryListView.getItems(), outFile);
            showAlert(Alert.AlertType.INFORMATION, "PDF Exported", "Diaries exported to PDF successfully!");
        }
    }

    private void logout() {
        LoginUI loginUI = new LoginUI(primaryStage);
        loginUI.showLoginScene();
    }

    private void clearInputs() {
        titleField.clear();
        contentArea.clear();
        moodComboBox.setValue("Neutral");
        coverImageView.setImage(null);
        chosenImageFile = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ListCell to display some details about each entry
    private static class DiaryEntryCell extends ListCell<DiaryEntry> {
        @Override
        protected void updateItem(DiaryEntry entry, boolean empty) {
            super.updateItem(entry, empty);
            if (empty || entry == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(entry.getTitle() + " (" + entry.getCreatedDate().toLocalDate() + ") - " + entry.getMood());
            }
        }
    }
}
