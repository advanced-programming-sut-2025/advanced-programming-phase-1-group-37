package game.view;

import java.util.Scanner;

public class consoleMenuView implements MenuView {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void DisplayMenu(String MenuName, Iterable<String> Commands) {
        System.out.println("=== " + MenuName + " ===");
        System.out.println("Commands: " + Commands);
    }

    @Override
    public String Prompt(String PromptText) {
        System.out.print(PromptText);
        return scanner.nextLine().trim();
    }

    @Override
    public void ShowMessage(String Message) {
        System.out.println(Message);
    }
}
