package data;

import model.User;

import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class UserManager {
    private static final String USERS_FILE = "users.csv";

    public static List<User> loadUsers() {
        List<User> userList = new ArrayList<>();
        List<String> lines = FileUtilities.readAllLines(USERS_FILE);
        for (String line : lines) {
            // Format: username,email,password
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
            lines.add(user.getUsername() + "," + user.getEmail() + "," + BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }
        FileUtilities.writeAllLines(USERS_FILE, lines);
    }

    public static boolean registerUser(String username, String email, String password) {
        // Load existing users
        List<User> allUsers = loadUsers();

        // Check if username or email is taken
        for (User u : allUsers) {
            if (u.getUsername().equalsIgnoreCase(username) ||
                    u.getEmail().equalsIgnoreCase(email)) {
                return false; // Registration fails
            }
        }

        // Otherwise, create
        allUsers.add(new User(username, email, password));
        saveUsers(allUsers);
        return true;
    }

    public static User loginUser(String usernameOrEmail, String password) {
        List<User> allUsers = loadUsers();
        for (User u : allUsers) {
            // Let them login with either username or email
            boolean matchUsername = u.getUsername().equalsIgnoreCase(usernameOrEmail);
            boolean matchEmail = u.getEmail().equalsIgnoreCase(usernameOrEmail);
            boolean isPasswordValid = BCrypt.checkpw(password, u.getPassword());
            if ((matchUsername || matchEmail) && isPasswordValid) {
                return u; // success
            }
        }
        return null; // login failure
    }
}
