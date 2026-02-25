package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.bl.message.AceptacionInvitacion;
import edu.upb.chatupb_v2.bl.message.Invitacion;
import edu.upb.chatupb_v2.bl.message.MensajeChat;
import edu.upb.chatupb_v2.bl.message.RechazoConexion;

import java.util.HashMap;
import java.util.Map;

public class ClientMediator {

    private static final ClientMediator instance = new ClientMediator();
    private final HashMap<String, SocketClient> clientes;
    private ChatEventListener uiListener;

    private ClientMediator() {
        this.clientes = new HashMap<>();
    }

    public static ClientMediator getInstance() {
        return instance;
    }

    public synchronized void setUiListener(ChatEventListener listener) {
        this.uiListener = listener;
    }

    public synchronized void agregarCliente(String idCliente, SocketClient cliente) {
        if (idCliente == null || cliente == null) {
            return;
        }
        clientes.put(idCliente, cliente);
    }

    public synchronized void removerCliente(String idCliente) {
        if (idCliente == null) {
            return;
        }
        clientes.remove(idCliente);
    }

    public synchronized boolean enviarMensaje(String idCliente, String mensaje) {
        SocketClient cliente = clientes.get(idCliente);
        if (cliente == null) {
            return false;
        }
        try {
            cliente.send(mensaje);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized Map<String, SocketClient> getClientes() {
        return new HashMap<>(clientes);
    }

    public synchronized void notificarInvitacion(Invitacion invitacion, SocketClient sender) {
        if (uiListener != null) {
            uiListener.onInvitacionRecibida(invitacion, sender);
        }
    }

    public synchronized void notificarAceptacion(AceptacionInvitacion aceptacion, SocketClient sender) {
        if (uiListener != null) {
            uiListener.onAceptacionRecibida(aceptacion, sender);
        }
    }

    public synchronized void notificarRechazo(RechazoConexion rechazo, SocketClient sender) {
        if (uiListener != null) {
            uiListener.onRechazoRecibido(rechazo, sender);
        }
    }

    public synchronized void notificarMensaje(MensajeChat mensaje, SocketClient sender) {
        if (uiListener != null) {
            uiListener.onMensajeRecibido(mensaje, sender);
        }
    }

    public synchronized void notificarOffline(String idUsuario, SocketClient sender) {
        if (uiListener != null) {
            uiListener.onClienteOffline(idUsuario, sender);
        }
    }
}
