package application;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.*;
import java.io.File;
import java.nio.file.Files;
import javafx.stage.DirectoryChooser;

abstract class FileProcessor {
    protected abstract void processFile(File inputFile, File outputFile, String password) throws Exception;

    public void process(File inputFile, File outputFile, String password) throws Exception {
        processFile(inputFile, outputFile, password);
    }
}

class Encryptor extends FileProcessor {
    @Override
    protected void processFile(File inputFile, File outputFile, String password) throws Exception {
        AESUtils.encryptFile(inputFile, outputFile, password);
    }
}

class Decryptor extends FileProcessor {
    @Override
    protected void processFile(File inputFile, File outputFile, String password) throws Exception {
        AESUtils.decryptFile(inputFile, outputFile, password);
    }
}

public class FileManager {
    private TextArea outputArea;
    private Label filePathLabel;
    private Button encryptButton, decryptButton, chooseFileButton,chooseDirectoryButton;
    private PasswordManager passwordManager;
    private FileProcessor fileProcessor;
    private Set<File> processedFiles = new HashSet<>();
    private Map<File, File> fileMap = new HashMap<>();
    private List<File> selectedFiles;

    public FileManager(PasswordManager passwordManager) {
        this.passwordManager = passwordManager;
        this.fileProcessor = new Encryptor(); 
        this.filePathLabel = new Label("Drag and drop a file here or choose one below:");
        this.filePathLabel.setMinHeight(50);
        this.filePathLabel.setMaxWidth(Double.MAX_VALUE);
        this.filePathLabel.setAlignment(Pos.CENTER);
        this.filePathLabel.setStyle("-fx-border-color: blue; -fx-border-width: 2px; -fx-border-style: dashed;");

        this.filePathLabel.setOnDragOver(this::handleDragOver);
        this.filePathLabel.setOnDragDropped(this::handleDragDropped);

        this.chooseFileButton = new Button("Choose File");
        this.chooseFileButton.setOnAction(e -> handleChooseFile(new Stage()));
        
        chooseDirectoryButton = new Button("Choose Directory");
        chooseDirectoryButton.setOnAction(e -> handleChooseDirectory(new Stage()));


        this.encryptButton = new Button("Encrypt");
        this.decryptButton = new Button("Decrypt");
        this.encryptButton.setDisable(true);
        this.decryptButton.setDisable(true);
        this.encryptButton.setOnAction(e -> handleProcessAction(new Stage(), new Encryptor()));
        this.decryptButton.setOnAction(e -> handleProcessAction(new Stage(), new Decryptor()));

        this.outputArea = new TextArea();
        this.outputArea.setEditable(false);
    }

    public Label getFilePathLabel() {
        return filePathLabel;
    }

    public Button getChooseFileButton() {
        return chooseFileButton;
    }
    
    public Button getChooseDirectoryButton() {
        return chooseDirectoryButton;
    }
    
    public Button getEncryptButton() {
        return encryptButton;
    }

    public Button getDecryptButton() {
        return decryptButton;
    }

    public TextArea getOutputArea() {
        return outputArea;
    }

    public void handleChooseFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Files");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
        this.selectedFiles = new ArrayList<>(fileChooser.showOpenMultipleDialog(primaryStage));
if (this.selectedFiles != null && !this.selectedFiles.isEmpty()) {
        filePathLabel.setText(this.selectedFiles.size() + " files selected.");
        outputArea.setText("Files ready for encryption or decryption. Please enter a password and select an action.");
        enableButtons();
    }
    }
    public void handleChooseDirectory(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File directory = directoryChooser.showDialog(primaryStage);
        if (directory != null) {
            filePathLabel.setText("Directory selected for processing: " + directory.getPath());
            outputArea.setText("Ready to encrypt or decrypt. Please select an action.");
            selectedFiles = new ArrayList<>();
            addFilesInDirectory(directory); 
            enableButtons();
        } else {
            filePathLabel.setText("No directory selected.");
            outputArea.setText("Directory selection cancelled.");
            disableButtons();
        }
    }

    private void addFilesInDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addFilesInDirectory(file); 
                } else {
                    selectedFiles.add(file);
                }
            }
        }
    }

 
    public void handleProcessAction(Stage primaryStage, FileProcessor processor) {
        this.fileProcessor = processor;

        if (passwordManager.getPasswordField().getText().isEmpty()) {
            outputArea.setText("Please enter a password before proceeding.");
            return;
        }

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            outputArea.setText("Please select files before proceeding.");
            return;
        }

        
        File destinationDirectory = getDestinationDirectory(primaryStage);
        if (destinationDirectory != null) {
            for (File file : selectedFiles) {
                processFileRecursively(file, destinationDirectory, processor);
            }
            selectedFiles.clear();
            outputArea.setText("All selected files have been processed.");
            disableButtons();
        }
    }

    private void processFileRecursively(File file, File destinationDirectory, FileProcessor processor) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    processFileRecursively(subFile, destinationDirectory, processor);
                }
            } else {
                outputArea.setText(outputArea.getText() + "\nFailed to list directory contents: " + file.getAbsolutePath());
            }
        } else {
            if (processedFiles.add(file)) {
                try {
                    String fileName = file.getName();
                    String outputFileName = constructOutputFileName(fileName, processor);
                    File outputFile = new File(destinationDirectory, outputFileName);
                    processor.process(file, outputFile, passwordManager.getPasswordField().getText());

                    fileMap.put(file, outputFile); 
                    
                    if (processor instanceof Encryptor) { 
                    	Files.deleteIfExists(file.toPath());
                        outputArea.setText(outputArea.getText() + "\nEncrypted and deleted original file: " + file.getName());
                    } else {
                    	Files.deleteIfExists(file.toPath());
                        outputArea.setText(outputArea.getText() + "\nDecryption successful for: " + file.getName());
                    }
                } catch (Exception e) {
                    outputArea.setText(outputArea.getText() + "\nError processing " + file.getName() + ": " + e.getMessage());
                }
            } else {
                outputArea.setText(outputArea.getText() + "\nSkipped processing already handled file: " + file.getName());
            }
        }
    }

    private String constructOutputFileName(String fileName, FileProcessor processor) {
        if (processor instanceof Decryptor && fileName.endsWith(".enc")) {
            return fileName.substring(0, fileName.length() - 4); 
        } else if (processor instanceof Encryptor && !fileName.endsWith(".enc")) {
            return fileName + ".enc"; 
        }
        return fileName; 
    }

    private File getDestinationDirectory(Stage primaryStage) {
    	DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Destination Directory");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));  
        return directoryChooser.showDialog(primaryStage);
    }

    public void enableButtons() {
        boolean hasPassword = !passwordManager.getPasswordField().getText().isEmpty();
        boolean hasFiles = selectedFiles != null && !selectedFiles.isEmpty();
        encryptButton.setDisable(false);
        decryptButton.setDisable(false);
    }

    public void disableButtons() {
        encryptButton.setDisable(false);
        decryptButton.setDisable(false);
    }
    private void clearFilePath() {
        filePathLabel.setText("Drag and drop a file here or choose one below:");
    }

    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != filePathLabel && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            File firstItem = db.getFiles().get(0);
            if (firstItem.isDirectory()) {
                
                filePathLabel.setText("Directory selected: " + firstItem.getAbsolutePath());
                outputArea.setText("Directory ready for encryption or decryption.");
                selectedFiles = new ArrayList<>();
                addFilesInDirectory(firstItem);
            } else {
                
                filePathLabel.setText("File selected: " + firstItem.getAbsolutePath());
                outputArea.setText("File ready for encryption or decryption.");
                selectedFiles = new ArrayList<>(db.getFiles()); 
            }
            enableButtons();
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }
}

