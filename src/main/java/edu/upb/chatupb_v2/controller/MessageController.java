package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.ChatMessageRecord;
import edu.upb.chatupb_v2.model.entities.ConfirmacionLectura;
import edu.upb.chatupb_v2.model.entities.EliminarMensaje;
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

            chatView.mostrarMensajePropio(mensajeChat.getIdMensaje(), texto.trim());
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
        chatView.mostrarMensajeContacto(mensaje.getIdMensaje(), mensaje.getMensaje());
        confirmarLectura(mensaje);
    }

    public void confirmarLecturaRecibida(ConfirmacionLectura confirmacion) {
        if (confirmacion == null) {
            return;
        }
        if (confirmacion.getIdMensaje() == null || confirmacion.getIdMensaje().isBlank()) {
            return;
        }
        chatView.marcarMensajeLeido(confirmacion.getIdMensaje());
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
                    if (mensaje.isEliminado()) {
                        chatView.mostrarMensajePropioEliminado(mensaje.getMessageId());
                    } else {
                        chatView.mostrarMensajePropio(mensaje.getMessageId(), mensaje.getContenido());
                    }
                } else {
                    if (mensaje.isEliminado()) {
                        chatView.mostrarMensajeContactoEliminado(mensaje.getMessageId());
                    } else {
                        chatView.mostrarMensajeContacto(mensaje.getMessageId(), mensaje.getContenido());
                    }
                }
            }
        } catch (Exception e) {
            throw new OperationException("No se pudo cargar el historial del chat.", e);
        }
    }

    public void eliminarMensaje(String miId, String idContactoActual, String idMensaje) {
        if (miId == null || miId.isBlank()) {
            throw new OperationException("No se encontro tu id local.");
        }
        if (idContactoActual == null || idContactoActual.isBlank()) {
            throw new OperationException("No hay contacto activo.");
        }
        if (idMensaje == null || idMensaje.isBlank()) {
            throw new OperationException("No se pudo identificar el mensaje a eliminar.");
        }

        try {
            EliminarMensaje eliminar = new EliminarMensaje(idMensaje);
            boolean enviado = ClientMediator.getInstance().enviarMensaje(idContactoActual, eliminar.generarTrama());
            if (!enviado) {
                throw new OperationException("No hay conexion activa para eliminar el mensaje.");
            }

            messageDao.markDeleted(miId, idMensaje);
            chatView.marcarMensajeEliminado(idMensaje);
        } catch (Exception e) {
            if (e instanceof OperationException) {
                throw (OperationException) e;
            }
            throw new OperationException("No se pudo eliminar el mensaje.", e);
        }
    }

    public void recibirEliminacion(String miId, EliminarMensaje eliminarMensaje) {
        if (eliminarMensaje == null || eliminarMensaje.getIdMensaje() == null || eliminarMensaje.getIdMensaje().isBlank()) {
            return;
        }
        if (miId == null || miId.isBlank()) {
            return;
        }

        messageDao.markDeleted(miId, eliminarMensaje.getIdMensaje());
        chatView.marcarMensajeEliminado(eliminarMensaje.getIdMensaje());
    }

    private void confirmarLectura(MensajeChat mensaje) {
        if (mensaje.getIdUser() == null || mensaje.getIdUser().isBlank()) {
            return;
        }
        if (mensaje.getIdMensaje() == null || mensaje.getIdMensaje().isBlank()) {
            return;
        }
        try {
            ConfirmacionLectura confirmacion = new ConfirmacionLectura(mensaje.getIdMensaje());
            ClientMediator.getInstance().enviarMensaje(mensaje.getIdUser(), confirmacion.generarTrama());
        } catch (Exception ignored) {
        }
    }
}
