package data;

import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserManager {
    private static final String USERS_FILE = "users.csv";
    private static HashMap<String, String> userIndex = new HashMap<>();
    public static void loadUsers() {
        List<String> lines = FileUtilities.readAllLines(USERS_FILE);
        userIndex.clear(); // Clear existing data to avoid duplication

        for (String line : lines) {
            // Format: username,email,passwordHash
            String[] parts = line.split(",");
            if (parts.length == 3) {
                userIndex.put(parts[0], parts[2]); // username → hashedPassword
                userIndex.put(parts[1], parts[2]); // email → hashedPassword
            }
        }
    }

    public static void saveUsers() {
        List<String> lines = new ArrayList<>();
        HashMap<String, String> uniqueUsers = new HashMap<>();

        // Ensure no duplicate entries (e.g., username and email pointing to the same hash)
        for (String key : userIndex.keySet()) {
            String hashedPassword = userIndex.get(key);
            if (!uniqueUsers.containsValue(hashedPassword)) {
                uniqueUsers.put(key, hashedPassword);
                lines.add(key + "," + key + "," + hashedPassword); // Simplified for unique usernames/emails
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

        saveUsers(); // Persist to CSV
        return true;
    }

    public static User loginUser(String usernameOrEmail, String password) {
        String hashedPassword = userIndex.get(usernameOrEmail);
        if (hashedPassword != null && BCrypt.checkpw(password, hashedPassword)) {
            return new User(usernameOrEmail, usernameOrEmail, null); // Do not return the plain password
        }
        return null; // Login failed
    }
}
