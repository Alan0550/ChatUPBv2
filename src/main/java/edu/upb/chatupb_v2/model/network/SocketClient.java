/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.model.network;

import edu.upb.chatupb_v2.model.entities.Message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author rlaredo
 */
public class SocketClient extends Thread {
    private final Socket socket;
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;
    private String remoteClientId;
    private ChatEventListener listener;
    private volatile boolean closedByLocal = false;
    private volatile boolean remoteOfflineAnnounced = false;

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public String getIp() {
        return ip;
    }

    public SocketClient(String ip) throws IOException {
        this.socket = new Socket(ip, 1900);
        this.ip = ip;
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public void addListener(ChatEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                System.out.println(message);
                try {
                    Message incomingMessage = Message.parseMessage(message);
                    String clientId = incomingMessage.extractClientId();
                    if ((this.remoteClientId == null || this.remoteClientId.isBlank())
                            && clientId != null
                            && !clientId.isBlank()) {
                        this.remoteClientId = clientId;
                    }
                    if ("0018".equals(incomingMessage.getCodigo())) {
                        remoteOfflineAnnounced = true;
                    }
                    System.out.println("Llego");
                    if (listener instanceof ClientMediator mediator) {
                        mediator.ejecutar(incomingMessage, this);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Trama no soportada: " + message);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (remoteClientId != null) {
                ClientMediator.getInstance().removerCliente(remoteClientId);
            }
            if (!closedByLocal && !remoteOfflineAnnounced && remoteClientId != null && listener != null) {
                listener.onClienteOffline(remoteClientId, this);
            }
        }
    }

    public void send(String message) throws IOException {
        if (!message.endsWith(System.lineSeparator())) {
            message = message + System.lineSeparator();
        }
        try {
            dout.write(message.getBytes(StandardCharsets.UTF_8));
            dout.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            closedByLocal = true;
            if (remoteClientId != null) {
                ClientMediator.getInstance().removerCliente(remoteClientId);
            }
            this.socket.close();
            this.br.close();
            this.dout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

