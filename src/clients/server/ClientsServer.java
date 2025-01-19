/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package clients.server;
import View.*;
import Controller.*;
import Controller.Client;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ahmed
 */
public class ClientsServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Server server =new Server();
           
        } catch (IOException ex) {
            Logger.getLogger(ClientsServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
