package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.ChatMessageRecord;
import edu.upb.chatupb_v2.model.entities.MensajeChat;
import edu.upb.chatupb_v2.model.network.ClientMediator;
import edu.upb.chatupb_v2.model.repository.MessageDao;
import edu.upb.chatupb_v2.view.IChatView;

import java.util.List;
import java.util.UUID;

public class MessageController {
    private final IChatView chatView;
    private final MessageDao messageDao;

    public MessageController(IChatView chatView) {
        this.chatView = chatView;
        this.messageDao = new MessageDao();
    }

    public void onload() {
    }

    public void enviarMensaje(String miId, String idContactoActual, String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return;
        }
        if (idContactoActual == null || idContactoActual.isBlank()) {
            throw new OperationException("Selecciona un contacto para enviar.");
        }

        try {
            MensajeChat mensajeChat = new MensajeChat(miId, UUID.randomUUID().toString(), texto.trim());
            String trama = mensajeChat.generarTrama();

            boolean enviado = ClientMediator.getInstance().enviarMensaje(idContactoActual, trama);
            if (!enviado) {
                throw new OperationException("No hay conexion activa.");
            }

            chatView.mostrarMensajePropio(texto.trim());
            chatView.limpiarInputMensaje();
        } catch (Exception e) {
            if (e instanceof OperationException) {
                throw (OperationException) e;
            }
            throw new OperationException("No se pudo enviar mensaje", e);
        }
    }

    public void recibirMensaje(MensajeChat mensaje) {
        if (mensaje == null) {
            return;
        }
        chatView.mostrarMensajeContacto(mensaje.getMensaje());
    }

    public void cargarHistorial(String miId, String idContactoActual) {
        if (idContactoActual == null || idContactoActual.isBlank()) {
            return;
        }

        try {
            List<ChatMessageRecord> mensajes = messageDao.findByOwnerAndContact(miId, idContactoActual);
            chatView.limpiarMensajes();
            for (ChatMessageRecord mensaje : mensajes) {
                if (miId.equals(mensaje.getSenderId())) {
                    chatView.mostrarMensajePropio(mensaje.getContenido());
                } else {
                    chatView.mostrarMensajeContacto(mensaje.getContenido());
                }
            }
        } catch (Exception e) {
            throw new OperationException("No se pudo cargar el historial del chat.", e);
        }
    }
}
