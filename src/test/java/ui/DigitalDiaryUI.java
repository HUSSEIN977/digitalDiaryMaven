package ui;

import data.DiaryManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    // Keep track of the chosen image file (to store path)
    private File chosenImageFile = null;

    // In-memory list of diaries for the logged-in user
    private List<DiaryEntry> diaryEntries;

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

        Button saveButton = new Button("Save Diary Entry");
        Button editButton = new Button("Edit Diary Entry");
        Button deleteButton = new Button("Delete Diary Entry");
        Button uploadButton = new Button("Upload Cover Image");
        Button sortButton = new Button("Sort by Date");
        Button pdfExportButton = new Button("Export to PDF");
        Button logoutButton = new Button("Logout");


        //THEY ARE EVENT HANDLERS, NOT ACTIVE METHODS SO STOP ASKING WHY THEY ARE NEVER USED. RUN THE PROGRAM FOR ONCE, JESUS
        saveButton.setOnAction(e -> saveDiaryEntry());
        editButton.setOnAction(e -> editDiaryEntry());
        deleteButton.setOnAction(e -> deleteDiaryEntry());
        uploadButton.setOnAction(e -> uploadCoverImage());
        sortButton.setOnAction(e -> sortDiariesByDate());
        pdfExportButton.setOnAction(e -> exportToPDF());
        logoutButton.setOnAction(e -> logout());

        VBox root = new VBox(10,
                new Label("Title:"), titleField,
                new Label("Content:"), contentArea,
                new Label("Mood:"), moodComboBox,
                uploadButton, coverImageView,
                new Label("Diary Entries:"), searchField, diaryListView,
                saveButton, editButton, deleteButton, sortButton, pdfExportButton,
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

        // Remove the old entry from CSV so the next "Save" effectively creates a new one.
        diaryEntries.remove(selectedEntry);
        diaryListView.getItems().remove(selectedEntry);
        DiaryManager.deleteDiaryEntry(selectedEntry);

        // Populate fields
        titleField.setText(selectedEntry.getTitle());
        contentArea.setText(selectedEntry.getContent());
        moodComboBox.setValue(selectedEntry.getMood());
        coverImageView.setImage(selectedEntry.getCoverImage());

        // If it had an image path, store that file
        if (selectedEntry.getImagePath() != null && !selectedEntry.getImagePath().isEmpty()) {
            chosenImageFile = new File(selectedEntry.getImagePath());
        }
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
            chosenImageFile = selectedFile;  // so we can store path in CSV
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
        // Ask the user where to save the PDF
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File outFile = fileChooser.showSaveDialog(primaryStage);

        if (outFile != null) {
            // Use PdfExporter to write all current diaries to PDF
            PdfExporter.exportEntriesToPDF(diaryListView.getItems(), outFile);
            showAlert(Alert.AlertType.INFORMATION, "PDF Exported", "Diaries exported to PDF successfully!");
        }
    }

    private void logout() {
        // Return to login screen
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
                // Show title, date, and mood
                setText(entry.getTitle() + " (" + entry.getCreatedDate().toLocalDate()
                        + ") - " + entry.getMood());
            }
        }
    }
}
