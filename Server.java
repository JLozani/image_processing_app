/**
  * Names: Luka Pendo, Ivo Masar, Josip Peter Lozancic
  * Course: ISTE-121-700
  * Group Project: Team #2
  * Description: Server GUI
  *
  * @version 04/01/2020
  */

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;

import java.net.*;
import java.io.*;

public class Server extends Application implements EventHandler<ActionEvent> {
   private Stage stage;
   private Scene scene;
   private VBox root;
   
   private Label lblLog = new Label("Log:");
   private Label lblQueue = new Label("Number of connected clients: ");
   private TextArea taLog = new TextArea();
   private TextField tfQueue = new TextField("0");
   private Button btnStartStop = new Button("Start");
   
   private ServerSocket sSocket = null;
   public static final int SERVER_PORT = 49153;
   
   private ServerThread serverThread = null;
   
   private CustomQueue<ClientThread> clients = new CustomQueue<ClientThread>();
   private int number = 0;
   private int connected = 0;
   
   private Process process = null;
   
   public static void main(String[] args) {
      launch(args);
   }
   
   public void start(Stage _stage) {
      stage = _stage;
      try {
         stage.setTitle("Server IP Address: " + InetAddress.getLocalHost().getHostAddress());
      } catch(Exception e) {}
      stage.setResizable(false);
      stage.setOnCloseRequest(
         new EventHandler<WindowEvent>() {
            public void handle(WindowEvent evt) {
               while(!clients.isEmpty()) {
                  clients.get().finish();
               }
               
               System.exit(0); 
            }
         });
      
      root = new VBox(8);
      root.setAlignment(Pos.CENTER);
      
      FlowPane fpQueue = new FlowPane(8, 8);
      fpQueue.setAlignment(Pos.CENTER);
      tfQueue.setEditable(false);
      tfQueue.setPrefColumnCount(5);
      fpQueue.getChildren().addAll(lblQueue, tfQueue);
      root.getChildren().add(fpQueue);
      
      FlowPane fpLog = new FlowPane(8,8);
      fpLog.setAlignment(Pos.CENTER);
      taLog.setPrefRowCount(10);
      taLog.setPrefColumnCount(35);
      taLog.setWrapText(true);
      fpLog.getChildren().addAll(lblLog, taLog);
      root.getChildren().add(fpLog);

      btnStartStop.setOnAction(this);      
      root.getChildren().add(btnStartStop);
      
      scene = new Scene(root, 500, 250);
      stage.setScene(scene);
      
      stage.show();
      
      btnStartStop.requestFocus();
   }
      
   public void handle(ActionEvent evt) {
      String label = ((Button)evt.getSource()).getText();
      switch(label) {
         case "Start":
            doStart();
            break;
         case "Stop":
            doStop();
            break;
      }
   } 
   
   public void doStart() {
      serverThread = new ServerThread();
      clients = new CustomQueue<ClientThread>();
      
      serverThread.start();
      
      btnStartStop.setText("Stop");
      log("                                ---------- Server Started ----------");
   }
   
   public void doStop() {
      serverThread.stopServer();
      
      btnStartStop.setText("Start");
      log("                                ---------- Server Stopped ----------");
   }
      
   class ServerThread extends Thread {
      public void run() {
         try {
            sSocket = new ServerSocket(SERVER_PORT);
         } catch(Exception e) {
            log(" >>>>>> ERROR while starting server <<<<<<");
            return;
         }
          
         while(true) {
            Socket cSocket = null;
            
            if(connected < 3) {
                try {
                   cSocket = sSocket.accept();
                } catch(Exception e) {
                   return;
                }

                ClientThread ct = new ClientThread(cSocket);
                clients.add(ct);
                ct.start();

                synchronized(tfQueue) {
                   number++;
                   connected++;
                   tfQueue.setText("" + number);
                }
            } else {
               try {
                  wait(500);
               } catch(Exception e) {}
            }
         }
      }
      
      public void stopServer() {
         try {
            sSocket.close();
         } catch(Exception e) {
            log(" >>>>>> ERROR while stopping server <<<<<<");
         }
      }
   }
   
   class ClientThread extends Thread {
      private Socket cSocket;
      private ObjectOutputStream out;
      private ObjectInputStream in;
      private ImageFile image;
      
      public ClientThread(Socket _cSocket) {
         super(_cSocket.getInetAddress().getHostAddress());
         cSocket = _cSocket;
         ImageFile image = null;
         
         try {
            out = new ObjectOutputStream(cSocket.getOutputStream());
            in = new ObjectInputStream(cSocket.getInputStream());
            
            log(" >>> Client [" + getName() + "] has connected <<<");
         } catch(Exception e) {
            log(" ### Error while connecting to client [" + getName() + "] ###");
         }
      }
      
      public void run() {
         try {
            image = (ImageFile)in.readObject();
            
            log(" --- Image " + image.getName() + " recieved from client [" + getName() + "] ---");
            
            log(" --- Processing image " + image.getName() + " to " + (image.getChoice() == 1 ? "GRAYSCALE" : (image.getChoice() == 2 ? "NEGATIVE" : "SEPIA")) + " ---");
            image.process();
            
            out.writeObject(image);
            out.flush();
            
            log(" --- Image " + image.getName() + " was sent to client [" + getName() + "] ---");
         } catch(Exception e) {
            if(image != null) {
               log(" ### ERROR during transmission with client [" + getName() + "] ###");
            }
         }
         
         finish();
         
         log(" >>> Client [" + getName() + "] has disconnected <<<");
         
         synchronized(tfQueue) {
            number--;
            connected--;
            tfQueue.setText("" + number);
         }
      }
      
      private void finish() {
         try {
            cSocket.close();
            in.close();
            out.close();
         } catch(Exception e) {}
      }
   }
   
   private void log(String text) {
      Platform.runLater(new Runnable() {
         public void run() {
            taLog.appendText(text + "\n");
         }
      });
   }
}