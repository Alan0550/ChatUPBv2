/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.bl.message.AceptacionInvitacion;
import edu.upb.chatupb_v2.bl.message.Invitacion;
import edu.upb.chatupb_v2.bl.message.MensajeChat;
import edu.upb.chatupb_v2.bl.message.RechazoConexion;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * @author rlaredo
 */
public class SocketClient extends Thread {
    private final Socket socket;
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;
    private ChatEventListener listener;
    private String remoteClientId;

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public SocketClient(String ip) throws IOException {
        this.socket = new Socket(ip, 1900);
        this.ip = ip;
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public void setChatEventListener(ChatEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                System.out.println(message);
                String split[] = message.split(Pattern.quote("|"));
                if (split.length == 0) {
                    continue;
                }
                System.out.println("Llego");
                switch (split[0]) {
                    case "001": {
                        System.out.println("Es invitacion");
                        Invitacion invitacion = Invitacion.parse(message);
                        this.remoteClientId = invitacion.getIdUsuario();
                        ClientMediator.getInstance().agregarCliente(this.remoteClientId, this);
                        if (listener != null) {
                            listener.onInvitacionRecibida(invitacion, this);
                        }
                        break;
                    }
                    case "002": {
                        AceptacionInvitacion aceptacion = AceptacionInvitacion.parse(message);
                        this.remoteClientId = aceptacion.getIdUsuario();
                        ClientMediator.getInstance().agregarCliente(this.remoteClientId, this);
                        if (listener != null) {
                            listener.onAceptacionRecibida(aceptacion, this);
                        }
                        break;
                    }
                    case "003": {
                        RechazoConexion rechazo = RechazoConexion.parse(message);
                        if (listener != null) {
                            listener.onRechazoRecibido(rechazo, this);
                        }
                        break;
                    }
                    case "007": {
                        MensajeChat mensajeChat = MensajeChat.parse(message);
                        if (listener != null) {
                            listener.onMensajeRecibido(mensajeChat, this);
                        }
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (remoteClientId != null) {
                ClientMediator.getInstance().removerCliente(remoteClientId);
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
