package model;

import javafx.scene.image.Image;

import java.time.LocalDateTime;

public class DiaryEntry {
    private String ownerUsername;    // Which user this entry belongs to
    private String title;
    private String content;
    private Image coverImage;
    private String mood;
    private LocalDateTime createdDate;
    private String imagePath;  // Where we store the cover image file path

    public DiaryEntry(
            String ownerUsername,
            String title,
            String content,
            Image coverImage,
            String mood,
            LocalDateTime createdDate,
            String imagePath
    ) {
        this.ownerUsername = ownerUsername;
        this.title = title;
        this.content = content;
        this.coverImage = coverImage;
        this.mood = mood;
        this.createdDate = createdDate;
        this.imagePath = imagePath;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Image getCoverImage() {
        return coverImage;
    }

    public String getMood() {
        return mood;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String toString() {
        // Used in ListView cells
        return title;
    }
}
