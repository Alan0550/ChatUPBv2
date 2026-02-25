/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.bl.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author rlaredo
 */
public class ChatServer extends Thread {
    private static final int port = 1900;

    private final ServerSocket server;
    public ChatServer() throws IOException {
        this.server = new ServerSocket(port);
    }

    public void addListener(ChatEventListener listener) {
        ClientMediator.getInstance().setUiListener(listener);
    }

    @Override
    public void run() {
        while (true) {
            try {
                SocketClient socketClient = new SocketClient(this.server.accept());
                socketClient.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
