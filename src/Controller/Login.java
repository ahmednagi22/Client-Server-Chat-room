package Controller;

import View.LoginView;
import java.awt.HeadlessException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JOptionPane;

public class Login {

    public static int port;
    public static LoginView window;

    public static void main(String[] args) {

        window = new LoginView();
        window.setVisible(true);
        port = 1268;

    }
    //called by Login Gui
    public static void startLogin(String UserName) {
        try {
            Socket s = new Socket("localhost", port); // create a socket
            DataInputStream inputStream = new DataInputStream(s.getInputStream()); 
            DataOutputStream outStream = new DataOutputStream(s.getOutputStream());
            outStream.writeUTF(UserName); 

            String msgFromServer = inputStream.readUTF();

            if (msgFromServer.equals("Username Already Exist")) {
                JOptionPane.showMessageDialog(window, "Username already taken\n");//print in gui Username already taken
            } else {
                new Client(UserName, s); 
                window.dispose();
               
                
                //new Client(UserName, s); 
                
//                Server.ThreadBool(UserName,s);
                
            }
        } catch (HeadlessException | IOException ex) {
        }

    }

}
