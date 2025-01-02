package ui;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MotivationalQuotes {
    private static final HashMap<String, List<String>> quotes = new HashMap<>();

    static {
        quotes.put("sad", List.of(
                "When you get what you want, that's God's direction. When you don't get what you wanted, that's God's protection.",
                "It's okay to feel sad, it's okay to feel lost, but remember, this too will pass.",
                "Turn your wounds into wisdom."
        ));
        quotes.put("happy", List.of(
                "Finally, the kind of happiness that makes you jump like a kid again.",
                "Enjoy the little things, for one day you may look back and realize they were the big things.",
                "Keep smiling, because life is beautiful."
        ));
        quotes.put("excited", List.of(
                "The future belongs to YOU.",
                "Stay excited, it's what makes us human after all.",
                "Life is a lot like jazz; it's the best when you improvise."
        ));
        quotes.put("neutral", List.of(
                "A calm mind brings inner strength.",
                "In the middle of everything happening, life is still good.",
                "Remember to enjoy the small things; they matter the most."
        ));
        quotes.put("angry", List.of(
                "Letting your anger out is letting the worst of you hurt your loved ones.",
                "Breaking down won’t solve anything; breaking things down will.",
                "Control your anger; don’t let it control you."
        ));
    }

    public static String getMotivationalQuote(String mood) {
        mood = mood.toLowerCase().trim();
        if (quotes.containsKey(mood)) {
            List<String> selectedQuotes = quotes.get(mood);
            Random random = new Random();
            return selectedQuotes.get(random.nextInt(selectedQuotes.size()));
        } else {
            return "Sorry, I don't recognize that mood. Please try again.";
        }
    }
}
