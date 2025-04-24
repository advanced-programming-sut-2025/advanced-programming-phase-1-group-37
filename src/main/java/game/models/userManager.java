package game.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class userManager {
    private static final String USER_FILE = "users.json";
    private final Map<String, user> Users = new HashMap<>();
    private final Gson gson = new Gson();

    public userManager() {
        LoadUsers();
    }

    public boolean RegisterUser(String Username,
                                String PlainPassword,
                                String Email,
                                String Gender,
                                String Nickname) {
        if (Users.containsKey(Username)) return false;
        String hash = HashPassword(PlainPassword);
        Users.put(Username, new user(Username, hash, Email, Gender, Nickname));
        SaveUsers();
        return true;
    }

    public boolean Authenticate(String Username, String PlainPassword) {
        user User = Users.get(Username);
        if (User == null) return false;
        return User.GetPasswordHash().equals(HashPassword(PlainPassword));
    }

    private void LoadUsers() {
        File f = new File(USER_FILE);
        if (!f.exists()) return;
        try (Reader r = new FileReader(f)) {
            Type type = new TypeToken<Map<String, user>>(){}.getType();
            Map<String, user> loaded = gson.fromJson(r, type);
            if (loaded != null) Users.putAll(loaded);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users.json", e);
        }
    }

    private void SaveUsers() {
        try (Writer w = new FileWriter(USER_FILE)) {
            gson.toJson(Users, w);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save users.json", e);
        }
    }

    private String HashPassword(String Password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(Password.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
