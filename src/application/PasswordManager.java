package application;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;

public class PasswordManager {
    private PasswordField passwordField;
    private Label passwordStrengthLabel;
    private FileManager fileManager;

    public PasswordManager() {
        this.passwordField = new PasswordField();
        this.passwordField.setPromptText("Enter Password");
        this.passwordField.setMaxWidth(Double.MAX_VALUE); 
        this.fileManager = fileManager;
        this.passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
        	updatePasswordStrengthLabel(newValue);
        	System.out.println("Password changed, updating button state.");
        });
        this.passwordStrengthLabel = new Label();
        this.passwordStrengthLabel.setAlignment(Pos.CENTER);
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Label getPasswordStrengthLabel() {
        return passwordStrengthLabel;
    }

    private void updatePasswordStrengthLabel(String password) {
        String strengthText = "Weak";
        int strength = checkPasswordStrength(password);
        if (strength <= 5) {
            strengthText = "Weak";
            passwordStrengthLabel.setTextFill(Color.RED);
        } else if (strength <= 10) {
            strengthText = "Moderate";
            passwordStrengthLabel.setTextFill(Color.ORANGE);
        } else {
            strengthText = "Strong";
            passwordStrengthLabel.setTextFill(Color.GREEN);
        }
        passwordStrengthLabel.setText("Password Strength: " + strengthText);
    }

    private int checkPasswordStrength(String password) {
        int strengthPoints = 0;
        if (password.length() > 5) strengthPoints += 2;
        if (password.length() > 8) strengthPoints += 2;
        if (password.matches("(?=.*[0-9]).*")) strengthPoints += 2;
        if (password.matches("(?=.*[a-z]).*")) strengthPoints += 1;
        if (password.matches("(?=.*[A-Z]).*")) strengthPoints += 2;
        if (password.matches("(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*")) strengthPoints += 3;
        return strengthPoints;
    }
    
    public void clearFields() {
    	passwordField.clear();
        passwordStrengthLabel.setText("");
    }
}
