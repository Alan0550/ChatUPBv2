package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.AceptarHello;
import edu.upb.chatupb_v2.model.entities.AceptacionInvitacion;
import edu.upb.chatupb_v2.model.entities.ConfirmacionLectura;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.EliminarMensaje;
import edu.upb.chatupb_v2.model.entities.EnviarContacto;
import edu.upb.chatupb_v2.model.entities.Hello;
import edu.upb.chatupb_v2.model.entities.ImagenChat;
import edu.upb.chatupb_v2.model.entities.Invitacion;
import edu.upb.chatupb_v2.model.entities.MensajeChat;
import edu.upb.chatupb_v2.model.entities.RechazarHello;
import edu.upb.chatupb_v2.model.entities.RechazoConexion;
import edu.upb.chatupb_v2.model.network.ChatEventListener;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.BlackListDao;
import edu.upb.chatupb_v2.view.ChatUI;

import javax.swing.*;

public class NetworkEventController implements ChatEventListener {
    private final ChatUI chatUI;
    private final ConnectionController connectionController;
    private final ContactController contactController;
    private final MessageController messageController;
    private final BlackListDao blackListDao;

    public NetworkEventController(
            ChatUI chatUI,
            ConnectionController connectionController,
            ContactController contactController,
            MessageController messageController
    ) {
        this.chatUI = chatUI;
        this.connectionController = connectionController;
        this.contactController = contactController;
        this.messageController = messageController;
        this.blackListDao = new BlackListDao();
    }

    @Override
    public void onInvitacionRecibida(Invitacion inv, SocketClient sender) {
        String ipRemota = sender.getIp();
        contactController.guardarOActualizarContacto(inv.getIdUsuario(), inv.getNombre(), ipRemota, false);

        if (blackListDao.isBlacklisted(ipRemota)) {
            try {
                connectionController.rechazarInvitacion(sender, true);
                chatUI.mostrarMensajeSistema("Solicitud rechazada (IP en lista negra).");
            } catch (OperationException e) {
                chatUI.mostrarMensajeSistema("Error al rechazar automaticamente.");
            }
            return;
        }

        SwingUtilities.invokeLater(() -> {
            int respuesta = chatUI.pedirDecisionInvitacion(inv.getNombre());
            try {
                if (respuesta == JOptionPane.YES_OPTION) {
                    connectionController.aceptarInvitacion(sender, chatUI.getMiId(), chatUI.getMiNombre());
                    contactController.guardarOActualizarContacto(inv.getIdUsuario(), inv.getNombre(), sender.getIp(), true);
                    chatUI.activarContactoEnVista(inv.getIdUsuario(), inv.getNombre(), sender.getIp(), true);
                    chatUI.mostrarMensajeSistema("Conexion aceptada. Ya pueden chatear.");
                } else {
                    connectionController.rechazarInvitacion(sender, false);
                    blackListDao.addToBlacklist(sender.getIp());
                    chatUI.mostrarMensajeSistema("Conexion rechazada y agregada a lista negra.");
                }
            } catch (OperationException e) {
                chatUI.mostrarMensajeSistema(e.getMessage());
            }
        });
    }

    @Override
    public void onAceptacionRecibida(AceptacionInvitacion acc, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            connectionController.usarConexion(sender);
            contactController.guardarOActualizarContacto(acc.getIdUsuario(), acc.getNombre(), sender.getIp(), true);
            chatUI.activarContactoEnVista(acc.getIdUsuario(), acc.getNombre(), sender.getIp(), true);
            chatUI.mostrarMensajeSistema(acc.getNombre() + " acepto tu invitacion 002.");
        });
    }

    @Override
    public void onRechazoRecibido(RechazoConexion rechazo, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            chatUI.mostrarEstadoRechazado();
            chatUI.mostrarMensajeSistema("La solicitud fue rechazada (003).");
        });
    }

    @Override
    public void onHelloRecibido(Hello hello, SocketClient sender) {
        if (hello == null) {
            return;
        }

        boolean existe = contactController.existeContactoPorCodigo(hello.getIdUsuario());
        if (!existe) {
            try {
                connectionController.rechazarHello(sender, true);
                SwingUtilities.invokeLater(() -> {
                    chatUI.activarContactoEnVista(hello.getIdUsuario(), hello.getIdUsuario(), sender.getIp(), false);
                    chatUI.mostrarMensajeSistema("Conexion automatica rechazada (006).");
                });
            } catch (OperationException e) {
                chatUI.mostrarMensajeSistema(e.getMessage());
            }
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                connectionController.aceptarHello(sender, chatUI.getMiId());
                Contact contacto = chatUI.obtenerContactoPorId(hello.getIdUsuario());
                String nombre = contacto != null ? contacto.getName() : hello.getIdUsuario();
                contactController.guardarOActualizarContacto(hello.getIdUsuario(), nombre, sender.getIp(), true);
                chatUI.activarContactoEnVista(hello.getIdUsuario(), nombre, sender.getIp(), true);
                chatUI.mostrarMensajeSistema("Conexion automatica aceptada (005).");
            } catch (OperationException e) {
                chatUI.mostrarMensajeSistema(e.getMessage());
            }
        });
    }

    @Override
    public void onAceptarHelloRecibido(AceptarHello aceptarHello, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            connectionController.usarConexion(sender);
            Contact contacto = chatUI.obtenerContactoPorId(aceptarHello.getIdUsuario());
            String nombre = contacto != null ? contacto.getName() : aceptarHello.getIdUsuario();
            contactController.guardarOActualizarContacto(aceptarHello.getIdUsuario(), nombre, sender.getIp(), true);
            chatUI.activarContactoEnVista(aceptarHello.getIdUsuario(), nombre, sender.getIp(), true);
            chatUI.mostrarMensajeSistema("Conexion automatica establecida (005).");
        });
    }

    @Override
    public void onRechazarHelloRecibido(RechazarHello rechazoHello, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            connectionController.cerrarConexionActual();
            chatUI.mostrarEstadoRechazado();
            chatUI.mostrarMensajeSistema("La conexion automatica fue rechazada (006).");
        });
    }

    @Override
    public void onMensajeRecibido(MensajeChat mensaje, SocketClient sender) {
        SwingUtilities.invokeLater(() -> chatUI.manejarMensajeEntrante(mensaje, sender.getIp()));
    }

    @Override
    public void onImagenRecibida(ImagenChat imagen, SocketClient sender) {
        SwingUtilities.invokeLater(() -> chatUI.manejarImagenEntrante(imagen, sender.getIp()));
    }

    @Override
    public void onEliminarMensajeRecibido(EliminarMensaje eliminarMensaje, SocketClient sender) {
        SwingUtilities.invokeLater(() -> chatUI.manejarEliminacionMensaje(eliminarMensaje));
    }

    @Override
    public void onEnviarContactoRecibido(EnviarContacto contacto, SocketClient sender) {
        SwingUtilities.invokeLater(() -> chatUI.manejarContactoCompartidoRecibido(contacto, sender.getIp()));
    }

    @Override
    public void onConfirmacionLecturaRecibida(ConfirmacionLectura confirmacion, SocketClient sender) {
        SwingUtilities.invokeLater(() -> chatUI.manejarConfirmacionLectura(confirmacion));
    }

    @Override
    public void onClienteOffline(String idUsuario, SocketClient sender) {
        SwingUtilities.invokeLater(() -> chatUI.manejarContactoOffline(idUsuario));
    }
}
