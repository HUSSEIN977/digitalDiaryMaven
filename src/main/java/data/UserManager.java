package data;

import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class UserManager {
    private static final String USERS_FILE = "users.csv";
    private static HashMap<String, String> userIndex = new HashMap<>();
    public static void loadUsers() {
        List<String> lines = FileUtilities.readAllLines(USERS_FILE);
        userIndex.clear();

        for (String line : lines) {
            // Format: username,email,hashed password
            String[] parts = line.split(",");
            if (parts.length == 3) {
                userIndex.put(parts[0], parts[2]); // username and hashed password
                userIndex.put(parts[1], parts[2]); // email and hashed password
            }
        }
    }
    public static void saveUsers() {
        List<String> lines = new ArrayList<>();
        HashSet<String> processedPasswords = new HashSet<>();

        for (String key : userIndex.keySet()) {
            String hashedPassword = userIndex.get(key);
            // Save only one row per unique hashed password
            if (!processedPasswords.contains(hashedPassword)) {
                lines.add(key + "," + key + "," + hashedPassword); // Assume key as username or email for simplicity
                processedPasswords.add(hashedPassword); // Mark this password as processed
            }
        }

        FileUtilities.writeAllLines(USERS_FILE, lines);
    }

    public static boolean registerUser(String username, String email, String password) {
        if (userIndex.containsKey(username) || userIndex.containsKey(email)) {
            return false; // Username or email already exists
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        userIndex.put(username, hashedPassword);
        userIndex.put(email, hashedPassword);

        saveUsers(); // Register the new user to the CSV file
        return true;
    }

    public static User loginUser(String usernameOrEmail, String password) {
        String hashedPassword = userIndex.get(usernameOrEmail);
        if (hashedPassword != null && BCrypt.checkpw(password, hashedPassword)) {
            return new User(usernameOrEmail, usernameOrEmail, null); // Does not return the plain password
        }
        return null; // Login failed
    }
}
