package game.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class userManager {
    private static final String USER_FILE = "users.json";
    private final Map<String, user> Users = new HashMap<>();
    private final Gson gson = new Gson();

    // reuse the same email‐validation pattern from registerCommand
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9_\\-]+(\\.[A-Za-z0-9_\\-]+)*@[A-Za-z0-9_\\-]+(\\.[A-Za-z0-9_\\-]+)*\\.[A-Za-z]{2,}$");

    public userManager() {
        loadUsers();
    }

    public boolean RegisterUser(
            String Username,
            String PlainPassword,
            String Email,
            String Gender,
            String Nickname,
            int SecurityQuestionIndex,
            String SecurityAnswer
    ) {
        if (Users.containsKey(Username)) return false;
        if (!EMAIL_PATTERN.matcher(Email).matches()) return false;
        String hash = hashPassword(PlainPassword);
        Users.put(Username, new user(
                Username, hash, Email, Gender, Nickname,
                SecurityQuestionIndex, SecurityAnswer
        ));
        saveUsers();
        return true;
    }

    public boolean Authenticate(String Username, String PlainPassword) {
        user u = Users.get(Username);
        return u != null && u.GetPasswordHash().equals(hashPassword(PlainPassword));
    }

    public user GetUser(String Username) {
        return Users.get(Username);
    }

    public boolean ResetPassword(String Username, String PlainPassword) {
        user old = Users.get(Username);
        if (old == null) return false;
        String hash = hashPassword(PlainPassword);
        Users.put(Username, new user(
                old.GetUsername(), hash,
                old.GetEmail(), old.GetGender(), old.GetNickname(),
                old.GetSecurityQuestionIndex(), old.GetSecurityAnswer()
        ));
        saveUsers();
        return true;
    }

    public boolean ChangeUsername(String oldUsername, String newUsername) {
        if (!Users.containsKey(oldUsername)) return false;
        if (Users.containsKey(newUsername)) return false;

        user u = Users.remove(oldUsername);
        // update the username field inside the user object
        user updated = new user(
                newUsername,
                u.GetPasswordHash(),
                u.GetEmail(),
                u.GetGender(),
                u.GetNickname(),
                u.GetSecurityQuestionIndex(),
                u.GetSecurityAnswer()
        );
        Users.put(newUsername, updated);
        saveUsers();
        return true;
    }

    public void ChangeNickname(String Username, String newNickname) {
        user u = Users.get(Username);
        if (u == null) return;
        user updated = new user(
                u.GetUsername(),
                u.GetPasswordHash(),
                u.GetEmail(),
                u.GetGender(),
                newNickname,
                u.GetSecurityQuestionIndex(),
                u.GetSecurityAnswer()
        );
        Users.put(Username, updated);
        saveUsers();
    }

    public boolean UpdateEmail(String Username, String newEmail) {
        if (!EMAIL_PATTERN.matcher(newEmail).matches()) return false;
        // ensure no other user uses it
        for (user u : Users.values()) {
            if (u.GetEmail().equalsIgnoreCase(newEmail)) {
                return false;
            }
        }
        user old = Users.get(Username);
        if (old == null) return false;
        user updated = new user(
                old.GetUsername(),
                old.GetPasswordHash(),
                newEmail,
                old.GetGender(),
                old.GetNickname(),
                old.GetSecurityQuestionIndex(),
                old.GetSecurityAnswer()
        );
        Users.put(Username, updated);
        saveUsers();
        return true;
    }

    private void loadUsers() {
        File f = new File(USER_FILE);
        if (!f.exists()) return;
        try (Reader r = new FileReader(f)) {
            Type t = new TypeToken<Map<String, user>>(){}.getType();
            Map<String, user> loaded = gson.fromJson(r, t);
            if (loaded != null) Users.putAll(loaded);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + USER_FILE, e);
        }
    }

    private void saveUsers() {
        try (Writer w = new FileWriter(USER_FILE)) {
            gson.toJson(Users, w);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save " + USER_FILE, e);
        }
    }

    private String hashPassword(String pwd) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(pwd.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}