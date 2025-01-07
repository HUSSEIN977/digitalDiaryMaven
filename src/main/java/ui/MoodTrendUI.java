package ui;

import data.DiaryManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.DiaryEntry;
import model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MoodTrendUI {
    private final Stage primaryStage;
    private final User loggedInUser;
    private LineChart<String, Number> lineChart; // Store the LineChart as a member variable

    public MoodTrendUI(Stage primaryStage, User user) {
        this.primaryStage = primaryStage;
        this.loggedInUser = user;
    }

    public void showMoodTrendScene() {
        List<DiaryEntry> diaryEntries = DiaryManager.loadDiariesForUser(loggedInUser.getUsername());

        // Date pickers for filtering period
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");

        Button filterButton = new Button("Filter");
        filterButton.setOnAction(e -> updateChart(diaryEntries, startDatePicker.getValue(), endDatePicker.getValue()));

        // Return button
        Button returnButton = new Button("Return");
        returnButton.setOnAction(e -> {
            DigitalDiaryUI diaryUI = new DigitalDiaryUI(primaryStage, loggedInUser);
            diaryUI.showDiaryScene();
        });

        // Mood trend chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(1, 5, 1); // Mood scale
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number value) {
                switch (value.intValue()) {
                    case 1: return "Angry";
                    case 2: return "Sad";
                    case 3: return "Neutral";
                    case 4: return "Happy";
                    case 5: return "Excited";
                    default: return "";
                }
            }
        });

        // Add padding for y-axis labels to prevent overlap
        yAxis.setTickLabelGap(10);

        lineChart = new LineChart<>(xAxis, yAxis); // Initialize the LineChart
        lineChart.setTitle("Mood Trend Over Time");

        updateChart(diaryEntries, null, null); // Initial chart with all entries

        // Layout setup
        HBox filterBox = new HBox(10, startDatePicker, endDatePicker, filterButton);
        filterBox.setPadding(new Insets(10));
        VBox root = new VBox(15, lineChart, filterBox, returnButton);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Mood Trend Analysis");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateChart(List<DiaryEntry> diaryEntries, LocalDate startDate, LocalDate endDate) {
        // Filter entries based on selected dates
        List<DiaryEntry> filteredEntries = diaryEntries.stream()
                .filter(entry -> {
                    LocalDate entryDate = entry.getCreatedDate().toLocalDate();
                    return (startDate == null || !entryDate.isBefore(startDate)) &&
                            (endDate == null || !entryDate.isAfter(endDate));
                })
                .collect(Collectors.toList());

        // Aggregate moods by date
        Map<LocalDate, String> aggregatedMoods = aggregateMoodsByDate(filteredEntries);

        // Clear and prepare the chart data
        lineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Mood Trend");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Map.Entry<LocalDate, String> entry : aggregatedMoods.entrySet()) {
            String date = entry.getKey().format(dateFormatter);
            int moodValue = mapMoodToValue(entry.getValue());
            series.getData().add(new XYChart.Data<>(date, moodValue));
        }

        lineChart.getData().add(series);
    }

    private Map<LocalDate, String> aggregateMoodsByDate(List<DiaryEntry> entries) {
        Map<LocalDate, List<String>> moodsByDate = new HashMap<>();

        for (DiaryEntry entry : entries) {
            LocalDate date = entry.getCreatedDate().toLocalDate();
            moodsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(entry.getMood());
        }

        Map<LocalDate, String> aggregatedMoods = new HashMap<>();
        for (Map.Entry<LocalDate, List<String>> entry : moodsByDate.entrySet()) {
            List<String> moods = entry.getValue();

            // Count occurrences of each mood
            Map<String, Long> moodCounts = moods.stream()
                    .collect(Collectors.groupingBy(mood -> mood, Collectors.counting()));

            // Find the most common mood
            String mostCommonMood = moodCounts.entrySet().stream()
                    .max((e1, e2) -> {
                        int countComparison = e1.getValue().compareTo(e2.getValue());
                        if (countComparison != 0) return countComparison;

                        // If counts are equal, use mood hierarchy
                        return Integer.compare(mapMoodToValue(e1.getKey()), mapMoodToValue(e2.getKey()));
                    })
                    .get()
                    .getKey();

            aggregatedMoods.put(entry.getKey(), mostCommonMood);
        }

        return aggregatedMoods;
    }

    private int mapMoodToValue(String mood) {
        switch (mood.toLowerCase()) {
            case "angry": return 1;
            case "sad": return 2;
            case "neutral": return 3;
            case "happy": return 4;
            case "excited": return 5;
            default: return 0;
        }
    }
}
