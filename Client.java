/**
  * Names: Luka Pendo, Ivo Masar, Josip Peter Lozancic
  * Course: ISTE-121-700
  * Group Project: Team #2
  * Description: Client GUI
  *
  * @version 04/01/2020
  */

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.stage.FileChooser.*;
import javafx.geometry.*;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.net.*;
import java.io.*;

public class Client extends Application implements EventHandler<ActionEvent> {
   private Stage stage;
   private Scene scene;
   private VBox root;
   
   private Label lblServerIP = new Label("Server Name or IP: ");
   private TextField tfServerIP = new TextField();
   private Button btnConnect = new Button("Connect");
   
   private TextField tfSelect = new TextField();
   private Button btnSelect = new Button("Select");
   
   private Button btnGrayscale = new Button("Grayscale");
   private Button btnNegative = new Button("Negative");
   private Button btnSepia = new Button("Sepia");
   
   private Button btnSubmit = new Button("Submit");
   
   private ObjectOutputStream out = null;
   private ObjectInputStream in = null;
   
   public static final int SERVER_PORT = 49153;
   private Socket socket = null;
   
   private int choice = 0;
   private File selectedFile = null;
   private BufferedImage image = null;
   
   public static void main(String[] args) {
      launch(args);
   }
   
   public void start(Stage _stage) {
      stage = _stage;
      stage.setTitle("Image Processor");
      stage.setResizable(false);
      stage.setOnCloseRequest(
         new EventHandler<WindowEvent>() {
            public void handle(WindowEvent evt) { 
               if(btnConnect.getText().equals("Disconnect")) {
                  doDisconnect();
               }
               
               System.exit(0);
            }
         });
      
      tfSelect.setEditable(false);
      tfSelect.setDisable(true);
      btnSelect.setDisable(true);
      btnGrayscale.setDisable(true);
      btnNegative.setDisable(true);
      btnSepia.setDisable(true);
      btnSubmit.setDisable(true);
      
      root = new VBox(8);
      root.setAlignment(Pos.CENTER);
      
      FlowPane fpConnect = new FlowPane(8,8);
      fpConnect.setAlignment(Pos.CENTER);
      fpConnect.getChildren().addAll(lblServerIP, tfServerIP, btnConnect);
      root.getChildren().add(fpConnect);
      
      root.getChildren().add(new Label());
      
      FlowPane fpSelect = new FlowPane(8,8);
      fpSelect.setAlignment(Pos.CENTER);
      fpSelect.getChildren().addAll(tfSelect, btnSelect);
      root.getChildren().add(fpSelect);
      
      FlowPane fpChoice = new FlowPane(8,8);
      fpChoice.setAlignment(Pos.CENTER);
      fpChoice.getChildren().addAll(btnGrayscale, btnNegative, btnSepia);
      root.getChildren().add(fpChoice);
      
      root.getChildren().add(new Label());
      
      root.getChildren().add(btnSubmit);
      
      btnConnect.setOnAction(this);
      btnSelect.setOnAction(this);
      btnGrayscale.setOnAction(this);
      btnNegative.setOnAction(this);
      btnSepia.setOnAction(this);
      btnSubmit.setOnAction(this);
   
      scene = new Scene(root, 400, 200);
      stage.setScene(scene);
      stage.show();
   }
   
   public void handle(ActionEvent ae) {
      String label = ((Button)ae.getSource()).getText();
      
      switch(label) {
         case "Connect":
            doConnect();
            break;
         case "Disconnect":
            doDisconnect();
            break;
         case "Select":
            doSelect();
            break;
         case "Grayscale":
            doGrayscale();
            break;
         case "Negative":
            doNegative();
            break;
         case "Sepia":
            doSepia();
            break;
         case "Submit":
            doSubmit();
            break;
      }
   }
   
   private void doConnect() {
      try {
         socket = new Socket(tfServerIP.getText(), SERVER_PORT);
         socket.setSoTimeout(10000);
         out = new ObjectOutputStream(socket.getOutputStream());
         in = new ObjectInputStream(socket.getInputStream());
      } catch(Exception e) {
         try {
            socket.close();
         } catch(Exception ee) {}
         
         alert(AlertType.ERROR, "Could not connect to server...", "Problem connecting!");
            
         return;
      }
      
      btnConnect.setText("Disconnect");
      
      tfServerIP.setEditable(false);
      tfSelect.setDisable(false);
      btnSelect.setDisable(false);
   }
   
   private void doDisconnect() {
      try {
         socket.close();
         in.close();
         out.close();
      } catch(IOException ioe) {
         alert(AlertType.ERROR, "Could not disconnect from server...", "Problem disconnecting!");
         
         return;
      }
      
      btnConnect.setText("Connect");
      tfSelect.setText("");
      
      selectedFile = null;
      image = null;
      choice = 0;
      
      tfServerIP.setEditable(true);
      tfSelect.setDisable(true);
      btnSelect.setDisable(true);
      btnGrayscale.setDisable(true);
      btnNegative.setDisable(true);
      btnSepia.setDisable(true);
      btnSubmit.setDisable(true);
   }
   
   private void doSelect() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialDirectory(new File("."));
      fileChooser.setTitle("Select Image");
      fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.jpg"));
      
      selectedFile = fileChooser.showOpenDialog(stage);
      
      if(selectedFile == null) {
         return;
      }
      
      try {
         image = ImageIO.read(selectedFile);
      } catch(IOException ioe) {
         alert(AlertType.ERROR, "Image file could not be read...", "Problem reading image!");
         
         image = null;
         selectedFile = null;
         return;
      }
      
      tfSelect.setText(selectedFile.getName());
      btnGrayscale.setDisable(false);
      btnNegative.setDisable(false);
      btnSepia.setDisable(false);
   }
   
   private void doGrayscale() {
      choice = 1;
      btnGrayscale.setDisable(true);
      btnSubmit.setDisable(false);
      btnSubmit.requestFocus();
      btnNegative.setDisable(false);
      btnSepia.setDisable(false);
   }
   
   private void doNegative() {
      choice = 2;
      btnNegative.setDisable(true);
      btnSubmit.setDisable(false);
      btnSubmit.requestFocus();
      btnGrayscale.setDisable(false);
      btnSepia.setDisable(false);
   }
   
   private void doSepia() {
      choice = 3;
      btnSepia.setDisable(true);
      btnSubmit.setDisable(false);
      btnSubmit.requestFocus();
      btnGrayscale.setDisable(false);
      btnNegative.setDisable(false);
   }
   
   private void doSubmit() {
      try {
         Object imageFile = new ImageFile(choice, selectedFile.getName(), image);
         
         out.writeObject(imageFile);
         out.flush();
         
         Object object = in.readObject();
         
         try {
            ((ImageFile)object).write();
            
            alert(AlertType.INFORMATION, "Image saved successfully...", "Done!");
         } catch(Exception e) {
            alert(AlertType.ERROR, "Problem while saving image...", "Couldn't save image!");
         }
      } catch(Exception e) {
         alert(AlertType.ERROR, "Problem during transmition...", "Couldn't submit image!");
      }
      
      doDisconnect();
   }
   
   public void alert(AlertType type, String msg, String header) {
      Platform.runLater(new Runnable() {
         public void run() {
            Alert alert = new Alert(type, msg);
            alert.setHeaderText(header);
            alert.showAndWait();
         }
      });
   }
}