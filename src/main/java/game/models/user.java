package game.models;

import java.util.Objects;

public class user {
    private String Username;
    private String PasswordHash;
    private String Email;
    private String Gender;
    private String Nickname;
    private int SecurityQuestionIndex;
    private String SecurityAnswer;

    public user() {}

    public user(String Username,
                String PasswordHash,
                String Email,
                String Gender,
                String Nickname,
                int SecurityQuestionIndex,
                String SecurityAnswer) {
        this.Username = Objects.requireNonNull(Username);
        this.PasswordHash = Objects.requireNonNull(PasswordHash);
        this.Email = Objects.requireNonNull(Email);
        this.Gender = Objects.requireNonNull(Gender);
        this.Nickname = Objects.requireNonNull(Nickname);
        this.SecurityQuestionIndex = SecurityQuestionIndex;
        this.SecurityAnswer = Objects.requireNonNull(SecurityAnswer);
    }

    public String   GetUsername()             { return Username; }
    public String   GetPasswordHash()         { return PasswordHash; }
    public String   GetEmail()                { return Email; }
    public String   GetGender()               { return Gender; }
    public String   GetNickname()             { return Nickname; }
    public int      GetSecurityQuestionIndex(){ return SecurityQuestionIndex; }
    public String   GetSecurityAnswer()       { return SecurityAnswer; }
}
