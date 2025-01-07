package data;

import model.User;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import java.util.HashMap;

public class UserManager {
    private static final String USERS_FILE = "users.csv";
    private static HashMap<String, String> userIndex = new HashMap<>();

    public static List<User> loadUsers() {
        List<User> userList = new ArrayList<>();
        List<String> lines = FileUtilities.readAllLines(USERS_FILE);
        for (String line : lines) {
            // Format: username,email,password
            String[] parts = line.split(",");
            if (parts.length == 3) {
                userIndex.put(parts[0], parts[2]);
                userIndex.put(parts[1], parts[2]);
               }
            }
        return userList;
    }

    public static void saveUsers(List<User> userList) {
        List<String> lines = new ArrayList<>();
        for (User user : userList) {
            lines.add(user.getUsername() + "," + user.getEmail() + "," + BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            userIndex.put(user.getUsername(),user.getPassword());
            userIndex.put(user.getEmail(),user.getPassword());
        }
        FileUtilities.writeAllLines(USERS_FILE, lines);
    }

    public static boolean registerUser(String username, String email, String password) {
        if (userIndex.containsKey(username) || userIndex.containsKey(email)) {
            return false; // Username or email already exists
        }

        userIndex.put(username, password);
        userIndex.put(email, password);

        saveUsers(List.of(new User(username, email, password))); // Update CSV
        return true;
    }
    public static User loginUser(String usernameOrEmail, String password) {
        String hashedPassword = userIndex.get(usernameOrEmail);
        if (hashedPassword != null && BCrypt.checkpw(password, hashedPassword)) {
            return new User(usernameOrEmail, usernameOrEmail, password);
        }
        return null; // Login failed
    }

}
