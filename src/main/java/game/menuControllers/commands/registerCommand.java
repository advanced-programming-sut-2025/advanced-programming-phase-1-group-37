// src/main/java/game/menuControllers/commands/registerCommand.java
package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.models.userManager;
import game.view.MenuView;

import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Pattern;

public class registerCommand implements command {
//    private static final Pattern USERNAME_PATTERN =
//            Pattern.compile("^[A-Za-z0-9_-]{1,8}$");
//    private static final Pattern EMAIL_PATTERN =
//            Pattern.compile("^[A-Za-z0-9_\\-]+(\\.[A-Za-z0-9_\\-]+)*@[A-Za-z0-9_\\-]+(\\.[A-Za-z0-9_\\-]+)*\\.[A-Za-z]{2,}$");
private static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[A-Za-z0-9\\-]{1,50}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9_\\-]+(\\.[A-Za-z0-9_\\-]+)*@[A-Za-z0-9_\\-]+(\\.[A-Za-z0-9_\\-]+)*\\.[A-Za-z]{2,}$");
    // special chars allowed:
    private static final String SPECIALS = "?!><,\"':\\\\/\\|\\]\\[\\}\\{\\+=\\)\\(\\*&\\^%\\$#\\!";
    private static final int MIN_LENGTH = 8;

    private final userManager Users;
    private final MenuView View;
    private final Random Rand = new SecureRandom();

    public registerCommand(userManager users, MenuView view) {
        this.Users = users;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        // Username
        String u = promptUsername();
        if (u == null) return true;

        // Password (generate or manual)
        String p = promptPassword();
        if (p == null) return true;

        // Email
        String e = promptEmail();
        if (e == null) return true;

        // Gender
        String g = promptOrExit("Gender: ");
        if (g == null) return true;

        // Nickname
        String n = promptOrExit("Nickname: ");
        if (n == null) return true;

        // Register
        if (Users.RegisterUser(u, p, e, g, n)) {
            View.ShowMessage("✔ Registered \"" + u + "\" successfully.");
        } else {
            View.ShowMessage("✘ Registration failed.");
        }
        return true;
    }

    private String promptUsername() {
        while (true) {
            String u = promptOrExit("Username: ");
            if (u == null) return null;
            if (!USERNAME_PATTERN.matcher(u).matches()) {
//                View.ShowMessage("Invalid username: 1-8 chars; letters, digits, - or _ only.");
                View.ShowMessage("Invalid username: letters, digits, - only.");
                continue;
            }
            if (Users.GetUser(u) == null) {
                return u;
            }
            // suggest
            String suggestion;
            do {
                suggestion = u + String.format("%03d", Rand.nextInt(1000));
            } while (Users.GetUser(suggestion) != null);
            View.ShowMessage("Username taken. Suggestion: " + suggestion);
            String resp = View.Prompt("Accept suggestion? (y/n): ").trim();
            if ("y".equalsIgnoreCase(resp)) {
                return suggestion;
            }
        }
    }

    private String promptPassword() {
        String choice = View.Prompt("Generate random password? (y/n): ").trim();
        if ("exit".equalsIgnoreCase(choice)) {
            View.ShowMessage("Operation cancelled.");
            return null;
        }
        if ("y".equalsIgnoreCase(choice)) {
            String pwd = generatePassword(12);
            View.ShowMessage("Generated password: " + pwd);
            return pwd;
        }
        // manual entry
        while (true) {
            String pwd = promptOrExit("Password: ");
            if (pwd == null) return null;
            List<String> errors = validatePassword(pwd);
            if (!errors.isEmpty()) {
                for (String err : errors) View.ShowMessage(err);
                continue;
            }
            // confirm
            while (true) {
                String c = promptOrExit("Confirm password: ");
                if (c == null) return null;
                if (pwd.equals(c)) {
                    return pwd;
                }
                View.ShowMessage("passwords don't match!");
            }
        }
    }

    private List<String> validatePassword(String pwd) {
        List<String> errs = new ArrayList<>();
        if (pwd.length() < MIN_LENGTH) errs.add("Password too short (min " + MIN_LENGTH + ")");
        if (!pwd.chars().anyMatch(Character::isLowerCase)) errs.add("Missing lowercase letter");
        if (!pwd.chars().anyMatch(Character::isUpperCase)) errs.add("Missing uppercase letter");
        if (!pwd.chars().anyMatch(Character::isDigit))       errs.add("Missing digit");
        if (!pwd.chars().anyMatch(ch -> SPECIALS.indexOf(ch) >= 0))
            errs.add("Missing special character (" + SPECIALS + ")");
        // check no disallowed chars: assume only those four categories allowed
        for (char ch : pwd.toCharArray()) {
            if (!Character.isLetterOrDigit(ch) && SPECIALS.indexOf(ch) < 0) {
                errs.add("Invalid character: " + ch);
                break;
            }
        }
        return errs;
    }

    private String generatePassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = upper.toLowerCase();
        String digits = "0123456789";
        String all = upper + lower + digits + SPECIALS;
        char[] pwd = new char[length];
        // ensure at least one of each
        pwd[0] = upper.charAt(Rand.nextInt(upper.length()));
        pwd[1] = lower.charAt(Rand.nextInt(lower.length()));
        pwd[2] = digits.charAt(Rand.nextInt(digits.length()));
        pwd[3] = SPECIALS.charAt(Rand.nextInt(SPECIALS.length()));
        // fill rest
        for (int i = 4; i < length; i++) {
            pwd[i] = all.charAt(Rand.nextInt(all.length()));
        }
        // shuffle
        for (int i = pwd.length - 1; i > 0; i--) {
            int j = Rand.nextInt(i + 1);
            char tmp = pwd[i];
            pwd[i] = pwd[j];
            pwd[j] = tmp;
        }
        return new String(pwd);
    }

    private String promptEmail() {
        while (true) {
            String e = promptOrExit("Email: ");
            if (e == null) return null;
            if (!EMAIL_PATTERN.matcher(e).matches()) {
                View.ShowMessage("Invalid email format.");
                continue;
            }
            return e;
        }
    }

    private String promptOrExit(String prompt) {
        String input = View.Prompt(prompt);
        if ("exit".equalsIgnoreCase(input)) {
            View.ShowMessage("Operation cancelled.");
            return null;
        }
        return input;
    }
}
