package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class FileEncryptionApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("File Encryption App");

        PasswordManager passwordManager = new PasswordManager();
        FileManager fileManager = new FileManager(passwordManager);

        primaryStage.setScene(createScene(fileManager, passwordManager));
        primaryStage.show();
    }

    private Scene createScene(FileManager fileManager, PasswordManager passwordManager) {
    	GridPane grid = new GridPane();
    	grid.setAlignment(Pos.CENTER);
    	grid.setHgap(10);
    	grid.setVgap(10); 
    	grid.setPadding(new Insets(25, 25, 25, 25)); 

    	
    	grid.add(fileManager.getFilePathLabel(), 0, 0, 2, 1); 
    	grid.add(fileManager.getChooseFileButton(), 0, 1); 
    	grid.add(fileManager.getChooseDirectoryButton(), 1, 1); 
    	grid.add(passwordManager.getPasswordField(), 0, 2, 2, 1); 
    	grid.add(passwordManager.getPasswordStrengthLabel(), 0, 3, 2, 1); 
    	grid.add(fileManager.getEncryptButton(), 0, 4);
    	grid.add(fileManager.getDecryptButton(), 1, 4); 

    	grid.add(fileManager.getOutputArea(), 0, 5, 2, 1);

    	
    	return new Scene(grid, 600, 400); 
    }
}
