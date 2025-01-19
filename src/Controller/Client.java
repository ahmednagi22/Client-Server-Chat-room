package Controller;

import View.ClientView;
import java.awt.HeadlessException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

public class Client {
    
    public static DataInputStream inputStream;
    public static DataOutputStream outStream;
    public static ClientView clientWindow;
    private static String UserName, clientNames = "";
    public DefaultListModel<String> ActiveUserlistGui;

    public Client(String UserName, Socket s) {
        clientWindow = new ClientView();
        clientWindow.setVisible(true);
        this.UserName = UserName;
        try {
            clientWindow.setTitle("Welcome: " + UserName);
            ActiveUserlistGui = new DefaultListModel<>();
            clientWindow.activeUsers.setModel(ActiveUserlistGui);
            inputStream = new DataInputStream(s.getInputStream());
            outStream = new DataOutputStream(s.getOutputStream());
            new Read().start();
        } catch (IOException ex) {
        }
    }

    public static void Logout() {
        try {
            outStream.writeUTF("exit"); 
            clientWindow.dispose();  
        } catch (IOException e1) {
        }
    }

    public static void SendButtonAction() {
        String textAreaMessage = clientWindow.clientMessage.getText(); 
        if (textAreaMessage != null && !textAreaMessage.isEmpty()) {  
            try {
                String messageToBeSentToServer = "";
                String typeOfMessage = "sendTo";//send or exit

                List<String> selectedUsers = clientWindow.activeUsers.getSelectedValuesList();

                for (String user : selectedUsers) { // append all the usernames selected in a variable
                    if (clientNames.isEmpty()) {
                        clientNames += user;
                    } else {
                        clientNames += "," + user;
                    }
                }
                messageToBeSentToServer = typeOfMessage + ":" + clientNames + ":" + textAreaMessage;
                if (selectedUsers.isEmpty()) { 
                    JOptionPane.showMessageDialog(clientWindow, "Select User First!!!");
                }
                else {
                    outStream.writeUTF(messageToBeSentToServer);
                    clientWindow.clientMessage.setText("");
                    clientWindow.Chat.append("< You sent msg to " + clientNames + ">" + textAreaMessage + "\n"); 
                }
                clientNames = "";
            } catch (HeadlessException | IOException ex) {
                JOptionPane.showMessageDialog(clientWindow, "User does not exist anymore.");
            }
        }
    }

   

    class Read extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    String m = inputStream.readUTF(); 
                    System.out.println("inside read thread : " + m); 
                    if (m.contains(":;.,/=")) {
                        m = m.substring(6); 
                        System.out.println("-------------"+m);
                        ActiveUserlistGui.clear(); 
                        StringTokenizer st = new StringTokenizer(m, ",");
                        
                        while (st.hasMoreTokens()) {
                            String u = st.nextToken();
                            if (!UserName.equals(u))
                            {
                                ActiveUserlistGui.addElement(u); 
                            }                                                                           
                        }
                    } else {
                        clientWindow.Chat.append("" + m + "\n");
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

}
