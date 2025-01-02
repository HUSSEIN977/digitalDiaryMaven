package data;

import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String USERS_FILE = "users.csv";

    public static List<User> loadUsers() {
        List<User> userList = new ArrayList<>();
        List<String> lines = FileUtilities.readAllLines(USERS_FILE);
        for (String line : lines) {
            // Format: username,email,hashedPassword
            String[] parts = line.split(",");
            if (parts.length == 3) {
                userList.add(new User(parts[0], parts[1], parts[2]));
            }
        }
        return userList;
    }

    public static void saveUsers(List<User> userList) {
        List<String> lines = new ArrayList<>();
        for (User user : userList) {
            // CSV format: username,email,hashedPassword
            lines.add(user.getUsername() + "," + user.getEmail() + "," + user.getPassword());
        }
        FileUtilities.writeAllLines(USERS_FILE, lines);
    }

    /**
     * Registers a new user by hashing the raw password.
     *
     * @param username The desired username.
     * @param email The user’s email.
     * @param rawPassword The raw password (will be hashed).
     * @return true if registration is successful, false if username/email is taken.
     */
    public static boolean registerUser(String username, String email, String rawPassword) {
        // Load existing users
        List<User> allUsers = loadUsers();

        // Check if username or email is taken
        for (User u : allUsers) {
            if (u.getUsername().equalsIgnoreCase(username) ||
                    u.getEmail().equalsIgnoreCase(email)) {
                return false; // Registration fails
            }
        }

        // Hash the password
        String hashedPassword = hashPassword(rawPassword);

        // Create and save
        allUsers.add(new User(username, email, hashedPassword));
        saveUsers(allUsers);
        return true;
    }

    /**
     * Attempts to log in using a username OR email and a raw password (to be hashed).
     *
     * @param usernameOrEmail The username or email.
     * @param rawPassword The raw password the user typed in.
     * @return The User if login is successful, null otherwise.
     */
    public static User loginUser(String usernameOrEmail, String rawPassword) {
        List<User> allUsers = loadUsers();
        // Hash the incoming raw password for comparison
        String hashedInput = hashPassword(rawPassword);

        for (User u : allUsers) {
            boolean matchUsername = u.getUsername().equalsIgnoreCase(usernameOrEmail);
            boolean matchEmail = u.getEmail().equalsIgnoreCase(usernameOrEmail);

            // Compare the hashed input password with the stored hashed password
            if ((matchUsername || matchEmail) && u.getPassword().equals(hashedInput)) {
                return u; // success
            }
        }
        return null; // login failure
    }

    /**
     * Hashes a password using SHA-256.
     *
     * @param rawPassword The plaintext password.
     * @return The hex-encoded SHA-256 hash of the password.
     */
    private static String hashPassword(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Fallback or rethrow as runtime exception
            throw new RuntimeException("SHA-256 algorithm not found!");
        }
    }
}
