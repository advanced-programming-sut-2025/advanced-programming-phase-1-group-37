package game.view;

public interface MenuView {
    void DisplayMenu(String MenuName, Iterable<String> Commands);
    String Prompt(String PromptText);
    void ShowMessage(String Message);
}
