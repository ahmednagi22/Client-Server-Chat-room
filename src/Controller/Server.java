package Controller;

import View.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JList;

public class Server {

    public static int port = 1268;
    private ServerSocket serverSocket;
    private static Map<String, Socket> Users = new ConcurrentHashMap<>();//dictionary
    public static Set<String> activeUsers = new HashSet<>();
    public static ServerView serverWindow;

    public Server() throws IOException {
//        executors =Executors.newFixedThreadPool(3);
        serverWindow = new ServerView();
        serverWindow.setVisible(true);
        serverSocket = new ServerSocket(port);
        serverWindow.ServerMessages.append("Server started on port: " + port + "\n");
        serverWindow.ServerMessages.append("Waiting for the clients...\n");
        new ClientAccept().start();
    }
//    public static void ThreadBool(String UserName,Socket s){
//     Client c=new Client(UserName, s);
//     executors.execute(c);
//    
//    
//    }
    class ClientAccept extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientName = new DataInputStream(clientSocket.getInputStream()).readUTF();
                    DataOutputStream client_output_stream = new DataOutputStream(clientSocket.getOutputStream());
                    if (activeUsers != null && activeUsers.contains(clientName)) {
                        client_output_stream.writeUTF("Username Already Exist");
                    } else {
                        Users.put(clientName, clientSocket);
                        activeUsers.add(clientName);
                        client_output_stream.writeUTF("");
                        serverWindow.activeDlm.addElement(clientName);
                        serverWindow.Users.setModel(serverWindow.activeDlm); // show the active and allUser List to the swing app in JList
                        serverWindow.ServerMessages.append("Client " + clientName + " Connected...\n");
                        new MsgRead(clientSocket, clientName).start();
                        new PrepareCLientList().start();
                    }
                } catch (IOException e) {
                    // throw any exception occurs
                }

            }
        }
    }

    public static void Broadcast(String Message) {

        for (String usr : activeUsers) {
            try {
                new DataOutputStream(((Socket) Users.get(usr)).getOutputStream()).writeUTF("<Server>" + Message);
            } catch (Exception e) {
                // throw exceptions
            }
        }

    }

    class MsgRead extends Thread {

        Socket s;
        String UserName;

        private MsgRead(Socket s, String uname) {
            this.s = s;
            this.UserName = uname;
        }

        @Override
        public void run() {
            while (serverWindow.Users != null && !Users.isEmpty()) {
                try {

                    String message = new DataInputStream(s.getInputStream()).readUTF(); // read message from client

                    System.out.println("message read ==> " + message); // just print the message for testing
                    String[] msgList = message.split(":"); // I have used my own identifier to identify what action to take on the received message from client

                    if (msgList[0].equalsIgnoreCase("sendTo")) { // if message type toSend so send for selected users
                        String[] sendToList = msgList[1].split(",");
                        for (String usr : sendToList) {
                            try {
                                if (activeUsers.contains(usr)) { // check again if user is active then send the message
                                    new DataOutputStream(((Socket) Users.get(usr)).getOutputStream())
                                            .writeUTF("< " + UserName + " >" + msgList[2]);
                                    serverWindow.ServerMessages.append(UserName +" send "+ msgList[2]+" to "+usr+"\n");
                                }
                            } catch (Exception e) {
                                // throw exceptions
                            }
                        }
                    }
                    else if (msgList[0].equalsIgnoreCase("exit")) {
                        activeUsers.remove(UserName);
                        serverWindow.ServerMessages.append(UserName + " disconnected....\n");

                        new PrepareCLientList().start();

                        Iterator<String> itr = activeUsers.iterator();
                        while (itr.hasNext()) {
                            String usrName2 = (String) itr.next();
                            if (!usrName2.equalsIgnoreCase(UserName)) {//send to all except me
                                try {
                                    new DataOutputStream(((Socket) Users.get(usrName2)).getOutputStream())
                                            .writeUTF(UserName + " disconnected..."); // notify all other active user for disconnection of a user
                                } catch (Exception e) {
                                    // throw errors
                                }
                                new PrepareCLientList().start(); // update the active user list for every client after a user is disconnected
                            }
                        }
                        serverWindow.activeDlm.removeElement(UserName); // remove client from Gui for server
                        serverWindow.activeClientList.setModel(serverWindow.activeDlm); //update the active user list
                    }
                    
                } catch (IOException e) {
                }
            }
        }

    }

    class PrepareCLientList extends Thread { // it prepares the list of active user to be displayed on the UI

        @Override
        public void run() {
            try {
                String UserNames = "";
                Iterator itr = activeUsers.iterator(); // iterate over all active users
                while (itr.hasNext()) { // prepare string of all the users
                    String UserName = (String) itr.next();
                    UserNames += UserName + ",";
                }
                if (UserNames.length() != 0) { // just trimming the list for the safe side.
                    UserNames = UserNames.substring(0, UserNames.length() - 1);
                }
                itr = activeUsers.iterator();
                while (itr.hasNext()) { // iterate over all active users
                    String key = (String) itr.next();
                    try {
                        new DataOutputStream(((Socket) Users.get(key)).getOutputStream())
                                .writeUTF(":;.,/=" + UserNames); // set output stream and send the list of active users with identifier prefix :;.,/=
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
