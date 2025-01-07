package ui;

import java.util.List;
import java.util.Random;

//use two arrays to randomize the list
public class MotivationalQuotes {
    private static final String[] moods = {"sad", "happy", "excited", "neutral", "angry"};
    private static final List<String>[] quotes = new List[]{
            List.of(
                    "It's okay to feel sad, it's okay to feel lost, but remember, this too will pass.",
                    "Turn your wounds into wisdom."
            ),
            List.of(
                    "Finally, the kind of happiness that makes you jump like a kid again.",
                    "Keep smiling, because life is beautiful."
            ),
            List.of(
                    "The future belongs to YOU.",
                    "Stay excited, it's what makes us human after all.",
                    "Life is a lot like jazz; it's the best when you improvise."
            ),
            List.of(
                    "A calm mind brings inner strength.",
                    "In the middle of everything happening, life is still good.",
                    "Remember to enjoy the small things; they matter the most."
            ),
            List.of(
                    "Letting your anger out is letting the worst of you hurt your loved ones.",
                    "Breaking down won’t solve anything; breaking things down will.",
                    "Control your anger; don’t let it control you."
            )
    };

    public static String getMotivationalQuote(String mood) {
        mood = mood.toLowerCase().trim();
        for (int i = 0; i < moods.length; i++) {
            if (moods[i].equals(mood)) {
                List<String> selectedQuotes = quotes[i];
                Random random = new Random();
                return selectedQuotes.get(random.nextInt(selectedQuotes.size()));
            }
        }
        return "Sorry, I don't recognize that mood. Please try again.";
    }
}
