package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.ChatMessageRecord;
import edu.upb.chatupb_v2.model.entities.ConfirmacionLectura;
import edu.upb.chatupb_v2.model.entities.EliminarMensaje;
import edu.upb.chatupb_v2.model.entities.ImagenChat;
import edu.upb.chatupb_v2.model.entities.MensajeChat;
import edu.upb.chatupb_v2.model.network.ClientMediator;
import edu.upb.chatupb_v2.model.repository.MessageDao;
import edu.upb.chatupb_v2.view.IChatView;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
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

    public void enviarImagen(String miId, String idContactoActual, File archivoImagen) {
        if (archivoImagen == null) {
            return;
        }
        if (idContactoActual == null || idContactoActual.isBlank()) {
            throw new OperationException("Selecciona un contacto para enviar.");
        }

        try {
            byte[] bytes = Files.readAllBytes(archivoImagen.toPath());
            if (bytes.length == 0) {
                throw new OperationException("La imagen esta vacia.");
            }
            if (bytes.length > 1024 * 1024) {
                throw new OperationException("La imagen supera el limite de 1 MB.");
            }

            String base64 = Base64.getEncoder().encodeToString(bytes);
            ImagenChat imagenChat = new ImagenChat(miId, UUID.randomUUID().toString(), base64);
            boolean enviado = ClientMediator.getInstance().enviarMensaje(idContactoActual, imagenChat.generarTrama());
            if (!enviado) {
                throw new OperationException("No hay conexion activa.");
            }

            chatView.mostrarImagenPropia(imagenChat.getIdMensaje(), base64);
        } catch (Exception e) {
            if (e instanceof OperationException) {
                throw (OperationException) e;
            }
            throw new OperationException("No se pudo enviar imagen.", e);
        }
    }

    public void recibirImagen(ImagenChat imagen) {
        if (imagen == null) {
            return;
        }
        chatView.mostrarImagenContacto(imagen.getIdMensaje(), imagen.getImagenBase64());
        confirmarLectura(imagen.getIdUser(), imagen.getIdMensaje());
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

    public void confirmarLectura(String idContacto, String idMensaje) {
        if (idContacto == null || idContacto.isBlank()) {
            return;
        }
        if (idMensaje == null || idMensaje.isBlank()) {
            return;
        }
        try {
            ConfirmacionLectura confirmacion = new ConfirmacionLectura(idMensaje);
            ClientMediator.getInstance().enviarMensaje(idContacto, confirmacion.generarTrama());
        } catch (Exception e) {
            throw new OperationException("No se pudo confirmar lectura.", e);
        }
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
                    } else if (ImagenChat.isStoredContent(mensaje.getContenido())) {
                        chatView.mostrarImagenPropia(
                                mensaje.getMessageId(),
                                ImagenChat.extractBase64(mensaje.getContenido())
                        );
                    } else {
                        chatView.mostrarMensajePropio(mensaje.getMessageId(), mensaje.getContenido());
                    }
                } else {
                    if (mensaje.isEliminado()) {
                        chatView.mostrarMensajeContactoEliminado(mensaje.getMessageId());
                    } else if (ImagenChat.isStoredContent(mensaje.getContenido())) {
                        chatView.mostrarImagenContacto(
                                mensaje.getMessageId(),
                                ImagenChat.extractBase64(mensaje.getContenido())
                        );
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
