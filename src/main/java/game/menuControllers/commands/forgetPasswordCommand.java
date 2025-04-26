// src/main/java/game/menuControllers/commands/forgetPasswordCommand.java
package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.models.userManager;
import game.models.user;
import game.view.MenuView;

import java.util.List;
import java.util.regex.Pattern;
import java.security.SecureRandom;
import java.util.Random;

public class forgetPasswordCommand implements command {
    private final userManager Users;
    private final MenuView View;
    private final Random Rand = new SecureRandom();

    // same questions as in registration
    private static final List<String> QUESTIONS = List.of(
            "What is your mother's maiden name?",
            "What was the name of your first pet?",
            "What city were you born in?",
            "What was your first school?",
            "What is your favorite food?"
    );

    // reuse patterns/specials from registerCommand
    private static final Pattern EMAIL_PATTERN  = Pattern.compile("^[A-Za-z0-9_\\-]+(\\.[A-Za-z0-9_\\-]+)*@[A-Za-z0-9_\\-]+(\\.[A-Za-z0-9_\\-]+)*\\.[A-Za-z]{2,}$");
    private static final String SPECIALS = "?!><,\"':\\\\/\\|\\]\\[\\}\\{\\+=\\)\\(\\*&\\^%\\$#\\!";
    private static final int MIN_LENGTH = 8;

    public forgetPasswordCommand(userManager users, MenuView view) {
        this.Users = users;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        String u = View.Prompt("Username: ");
        if ("exit".equalsIgnoreCase(u)) return true;

        user usr = Users.GetUser(u);
        if (usr == null) {
            View.ShowMessage("Username not found.");
            return true;
        }

        String question = QUESTIONS.get(usr.GetSecurityQuestionIndex());
        String ans = View.Prompt(question + " ");
        if ("exit".equalsIgnoreCase(ans)) return true;

        if (!ans.equals(usr.GetSecurityAnswer())) {
            View.ShowMessage("Incorrect answer.");
            return true;
        }

        // choose new password
        String choice = View.Prompt("Generate random password? (y/n): ").trim();
        String newPw;
        if ("y".equalsIgnoreCase(choice)) {
            newPw = generatePassword(12);
            View.ShowMessage("Generated: " + newPw);
        } else {
            while (true) {
                newPw = View.Prompt("New password: ");
                if ("exit".equalsIgnoreCase(newPw)) return true;
                List<String> errs = new registerCommand(null, null).validatePassword(newPw);
                if (errs.isEmpty()) break;
                errs.forEach(View::ShowMessage);
            }
        }

        // confirm if manual
        if (!choice.equalsIgnoreCase("y")) {
            String c = View.Prompt("Confirm password: ");
            if (!newPw.equals(c)) {
                View.ShowMessage("passwords don't match!");
                return true;
            }
        }

        Users.ResetPassword(u, newPw);
        View.ShowMessage("Password has been reset.");
        return true;
    }

    private String generatePassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = upper.toLowerCase();
        String digits = "0123456789";
        String all = upper + lower + digits + SPECIALS;
        char[] pwd = new char[length];
        pwd[0] = upper.charAt(Rand.nextInt(upper.length()));
        pwd[1] = lower.charAt(Rand.nextInt(lower.length()));
        pwd[2] = digits.charAt(Rand.nextInt(digits.length()));
        pwd[3] = SPECIALS.charAt(Rand.nextInt(SPECIALS.length()));
        for (int i = 4; i < length; i++) {
            pwd[i] = all.charAt(Rand.nextInt(all.length()));
        }
        for (int i = length - 1; i > 0; i--) {
            int j = Rand.nextInt(i + 1);
            char tmp = pwd[i];
            pwd[i] = pwd[j];
            pwd[j] = tmp;
        }
        return new String(pwd);
    }
}
