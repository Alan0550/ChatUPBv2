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
    private ChatEventListener chatEventListener;
    public ChatServer() throws IOException {
        this.server = new ServerSocket(port);
    }

    public void addListener(ChatEventListener listener) {
        this.chatEventListener = listener;
    }

    @Override
    public void run() {
        while (true) {
            try {
                SocketClient socketClient = new SocketClient(this.server.accept());
                socketClient.setChatEventListener(this.chatEventListener);
                socketClient.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
