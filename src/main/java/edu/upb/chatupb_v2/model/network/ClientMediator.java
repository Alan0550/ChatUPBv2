package edu.upb.chatupb_v2.model.network;

import edu.upb.chatupb_v2.model.entities.AceptarHello;
import edu.upb.chatupb_v2.model.entities.AceptacionInvitacion;
import edu.upb.chatupb_v2.model.entities.ChatMessageRecord;
import edu.upb.chatupb_v2.model.entities.ConfirmacionLectura;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.EnviarContacto;
import edu.upb.chatupb_v2.model.entities.Hello;
import edu.upb.chatupb_v2.model.entities.Invitacion;
import edu.upb.chatupb_v2.model.entities.MensajeChat;
import edu.upb.chatupb_v2.model.entities.RechazarHello;
import edu.upb.chatupb_v2.model.entities.RechazoConexion;
import edu.upb.chatupb_v2.model.repository.MessageDao;

import java.util.HashMap;
import java.util.Map;

public class ClientMediator implements ChatEventListener {

    private static final ClientMediator instance = new ClientMediator();
    private final HashMap<String, SocketClient> clientes;
    private final MessageDao messageDao;
    private ChatEventListener uiListener;

    private ClientMediator() {
        this.clientes = new HashMap<>();
        this.messageDao = new MessageDao();
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
            guardarMensajeEnviado(idCliente, mensaje);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized Map<String, SocketClient> getClientes() {
        return new HashMap<>(clientes);
    }

    public synchronized void notificarInvitacion(Invitacion invitacion, SocketClient sender) {
        onInvitacionRecibida(invitacion, sender);
    }

    public synchronized void notificarAceptacion(AceptacionInvitacion aceptacion, SocketClient sender) {
        onAceptacionRecibida(aceptacion, sender);
    }

    public synchronized void notificarRechazo(RechazoConexion rechazo, SocketClient sender) {
        onRechazoRecibido(rechazo, sender);
    }

    public synchronized void notificarHello(Hello hello, SocketClient sender) {
        onHelloRecibido(hello, sender);
    }

    public synchronized void notificarAceptarHello(AceptarHello aceptarHello, SocketClient sender) {
        onAceptarHelloRecibido(aceptarHello, sender);
    }

    public synchronized void notificarRechazarHello(RechazarHello rechazoHello, SocketClient sender) {
        onRechazarHelloRecibido(rechazoHello, sender);
    }

    public synchronized void notificarMensaje(MensajeChat mensaje, SocketClient sender) {
        onMensajeRecibido(mensaje, sender);
    }

    public synchronized void notificarEnviarContacto(EnviarContacto contacto, SocketClient sender) {
        onEnviarContactoRecibido(contacto, sender);
    }

    public synchronized void notificarConfirmacionLectura(ConfirmacionLectura confirmacion, SocketClient sender) {
        onConfirmacionLecturaRecibida(confirmacion, sender);
    }

    public synchronized void notificarOffline(String idUsuario, SocketClient sender) {
        onClienteOffline(idUsuario, sender);
    }

    @Override
    public synchronized void onInvitacionRecibida(Invitacion invitacion, SocketClient sender) {
        if (invitacion != null && invitacion.getIdUsuario() != null) {
            agregarCliente(invitacion.getIdUsuario(), sender);
        }
        if (uiListener != null) {
            uiListener.onInvitacionRecibida(invitacion, sender);
        }
    }

    @Override
    public synchronized void onAceptacionRecibida(AceptacionInvitacion aceptacion, SocketClient sender) {
        if (aceptacion != null && aceptacion.getIdUsuario() != null) {
            agregarCliente(aceptacion.getIdUsuario(), sender);
        }
        if (uiListener != null) {
            uiListener.onAceptacionRecibida(aceptacion, sender);
        }
    }

    @Override
    public synchronized void onRechazoRecibido(RechazoConexion rechazo, SocketClient sender) {
        if (uiListener != null) {
            uiListener.onRechazoRecibido(rechazo, sender);
        }
    }

    @Override
    public synchronized void onHelloRecibido(Hello hello, SocketClient sender) {
        if (hello != null && hello.getIdUsuario() != null) {
            agregarCliente(hello.getIdUsuario(), sender);
        }
        if (uiListener != null) {
            uiListener.onHelloRecibido(hello, sender);
        }
    }

    @Override
    public synchronized void onAceptarHelloRecibido(AceptarHello aceptarHello, SocketClient sender) {
        if (aceptarHello != null && aceptarHello.getIdUsuario() != null) {
            agregarCliente(aceptarHello.getIdUsuario(), sender);
        }
        if (uiListener != null) {
            uiListener.onAceptarHelloRecibido(aceptarHello, sender);
        }
    }

    @Override
    public synchronized void onRechazarHelloRecibido(RechazarHello rechazoHello, SocketClient sender) {
        if (uiListener != null) {
            uiListener.onRechazarHelloRecibido(rechazoHello, sender);
        }
    }

    @Override
    public synchronized void onMensajeRecibido(MensajeChat mensaje, SocketClient sender) {
        if (mensaje != null && mensaje.getIdUser() != null) {
            agregarCliente(mensaje.getIdUser(), sender);
            guardarMensajeRecibido(mensaje);
        }
        if (uiListener != null) {
            uiListener.onMensajeRecibido(mensaje, sender);
        }
    }

    @Override
    public synchronized void onEnviarContactoRecibido(EnviarContacto contacto, SocketClient sender) {
        if (contacto != null && contacto.getIdUser() != null) {
            agregarCliente(contacto.getIdUser(), sender);
        }
        if (uiListener != null) {
            uiListener.onEnviarContactoRecibido(contacto, sender);
        }
    }

    @Override
    public synchronized void onConfirmacionLecturaRecibida(ConfirmacionLectura confirmacion, SocketClient sender) {
        if (uiListener != null) {
            uiListener.onConfirmacionLecturaRecibida(confirmacion, sender);
        }
    }

    @Override
    public synchronized void onClienteOffline(String idUsuario, SocketClient sender) {
        removerCliente(idUsuario);
        if (uiListener != null) {
            uiListener.onClienteOffline(idUsuario, sender);
        }
    }

    private void guardarMensajeEnviado(String idCliente, String trama) {
        try {
            MensajeChat msg = MensajeChat.parse(trama.trim());
            String contenido = limpiarFinalLinea(msg.getMensaje());
            ChatMessageRecord record = new ChatMessageRecord(
                    msg.getIdMensaje(),
                    Contact.ME_CODE,
                    idCliente,
                    Contact.ME_CODE,
                    contenido
            );
            messageDao.save(record);
        } catch (Exception ignored) {
        }
    }

    private void guardarMensajeRecibido(MensajeChat msg) {
        try {
            String contenido = limpiarFinalLinea(msg.getMensaje());
            ChatMessageRecord record = new ChatMessageRecord(
                    msg.getIdMensaje(),
                    Contact.ME_CODE,
                    msg.getIdUser(),
                    msg.getIdUser(),
                    contenido
            );
            messageDao.save(record);
        } catch (Exception ignored) {
        }
    }

    private String limpiarFinalLinea(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replaceAll("\\R+$", "");
    }
}
