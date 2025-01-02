package data;

import javafx.scene.image.Image;
import model.DiaryEntry;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DiaryManager {
    private static final String DIARIES_FILE = "diaries.csv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static List<DiaryEntry> loadDiariesForUser(String username) {
        List<DiaryEntry> result = new ArrayList<>();
        List<String> lines = FileUtilities.readAllLines(DIARIES_FILE);

        for (String line : lines) {
            // Format: ownerUsername,title,content,mood,createdDate,imagePath
            String[] parts = line.split(",", -1);
            if (parts.length == 6) {
                String owner = parts[0];
                String title = parts[1];
                String content = parts[2];
                String mood = parts[3];
                String dateStr = parts[4];
                String imagePath = parts[5];

                if (owner.equalsIgnoreCase(username)) {
                    LocalDateTime dateTime = LocalDateTime.parse(dateStr, FORMATTER);

                    Image coverImage = null;
                    if (!imagePath.isEmpty()) {
                        File imgFile = new File(imagePath);
                        if (imgFile.exists()) {
                            coverImage = new Image(imgFile.toURI().toString());
                        }
                    }

                    DiaryEntry entry = new DiaryEntry(
                            owner,
                            title,
                            content,
                            coverImage,
                            mood,
                            dateTime,
                            imagePath
                    );
                    result.add(entry);
                }
            }
        }
        return result;
    }

    public static List<DiaryEntry> loadAllDiaries() {
        List<DiaryEntry> allEntries = new ArrayList<>();
        List<String> lines = FileUtilities.readAllLines(DIARIES_FILE);

        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length == 6) {
                String owner = parts[0];
                String title = parts[1];
                String content = parts[2];
                String mood = parts[3];
                String dateStr = parts[4];
                String imagePath = parts[5];

                LocalDateTime dateTime = LocalDateTime.parse(dateStr, FORMATTER);
                Image coverImage = null;
                if (!imagePath.isEmpty()) {
                    File imgFile = new File(imagePath);
                    if (imgFile.exists()) {
                        coverImage = new Image(imgFile.toURI().toString());
                    }
                }

                DiaryEntry entry = new DiaryEntry(
                        owner,
                        title,
                        content,
                        coverImage,
                        mood,
                        dateTime,
                        imagePath
                );
                allEntries.add(entry);
            }
        }
        return allEntries;
    }

    public static void addDiaryEntry(DiaryEntry entry) {
        // Load all diaries, add the new one, save everything
        List<DiaryEntry> all = loadAllDiaries();
        all.add(entry);
        saveAllDiaries(all);
    }

    public static void saveAllDiaries(List<DiaryEntry> allEntries) {
        List<String> lines = new ArrayList<>();
        for (DiaryEntry e : allEntries) {
            String owner = e.getOwnerUsername();
            String title = e.getTitle().replace(",", " ");   // CSV-safe
            String content = e.getContent().replace(",", " ");
            String mood = e.getMood();
            String dateStr = e.getCreatedDate().format(FORMATTER);
            String imagePath = (e.getImagePath() != null) ? e.getImagePath() : "";

            lines.add(owner + "," + title + "," + content + "," +
                    mood + "," + dateStr + "," + imagePath);
        }
        FileUtilities.writeAllLines(DIARIES_FILE, lines);
    }

    public static void deleteDiaryEntry(DiaryEntry entry) {
        List<DiaryEntry> all = loadAllDiaries();
        // We find this exact entry by matching (owner, title, date)
        all.removeIf(e ->
                e.getOwnerUsername().equals(entry.getOwnerUsername()) &&
                        e.getTitle().equals(entry.getTitle()) &&
                        e.getCreatedDate().equals(entry.getCreatedDate())
        );
        saveAllDiaries(all);
    }

    // A basic “update” approach: find and remove old entry, then add the new one
    public static void updateDiaryEntry(DiaryEntry oldEntry, DiaryEntry updatedEntry) {
        deleteDiaryEntry(oldEntry);
        addDiaryEntry(updatedEntry);
    }
}
