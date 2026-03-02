/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package edu.upb.chatupb_v2.app;

import edu.upb.chatupb_v2.model.network.ChatServer;
import edu.upb.chatupb_v2.controller.ConnectionController;
import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.view.ChatUI;

/**
 * @author rlaredo
 */
public class ChatUPB_V2 {

    public static void main(String[] args) {
               /* Create and display the form */
        final ChatUI chatUI = new ChatUI();
        ConnectionController connectionController = new ConnectionController(chatUI);
        ContactController contactController = new ContactController(chatUI);
        MessageController messageController = new MessageController(chatUI);
        chatUI.setConnectionController(connectionController);
        chatUI.setContactController(contactController);
        chatUI.setMessageController(messageController);
        java.awt.EventQueue.invokeLater(() -> chatUI.setVisible(true));

        try {
            ChatServer chatServer = new ChatServer();
            chatServer.addListener(chatUI);
            chatServer.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

