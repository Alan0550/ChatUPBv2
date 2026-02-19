/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.bl.server.ChatServer;

/**
 * @author rlaredo
 */
public class ChatUPB_V2 {

    public static void main(String[] args) {
               /* Create and display the form */
        final ChatUI chatUI = new ChatUI();
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
