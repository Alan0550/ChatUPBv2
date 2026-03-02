package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.AceptarHello;
import edu.upb.chatupb_v2.model.entities.AceptacionInvitacion;
import edu.upb.chatupb_v2.model.entities.Hello;
import edu.upb.chatupb_v2.model.entities.Invitacion;
import edu.upb.chatupb_v2.model.entities.Offline;
import edu.upb.chatupb_v2.model.entities.RechazarHello;
import edu.upb.chatupb_v2.model.entities.RechazoConexion;
import edu.upb.chatupb_v2.model.network.ClientMediator;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.view.IChatView;

public class ConnectionController {
    private final IChatView chatView;
    private SocketClient currentClient;

    public ConnectionController(IChatView chatView) {
        this.chatView = chatView;
    }

    public void conectarPorIp(String ip, String miId, String miNombre) {
        String ipFinal = (ip == null || ip.isBlank()) ? "127.0.0.1" : ip.trim();
        try {
            SocketClient socketClient = new SocketClient(ipFinal);
            socketClient.addListener(ClientMediator.getInstance());
            socketClient.start();

            Invitacion invitacion = new Invitacion(miId, miNombre);
            socketClient.send(invitacion.generarTrama());

            currentClient = socketClient;
            chatView.mostrarMensajeSistema("Invitacion 001 enviada a " + ipFinal);
        } catch (Exception e) {
            throw new OperationException("No se pudo conectar: " + e.getMessage(), e);
        }
    }

    public void conectarAutomaticoPorIp(String ip, String miId) {
        String ipFinal = (ip == null || ip.isBlank()) ? "127.0.0.1" : ip.trim();
        try {
            SocketClient socketClient = new SocketClient(ipFinal);
            socketClient.addListener(ClientMediator.getInstance());
            socketClient.start();

            Hello hello = new Hello(miId);
            socketClient.send(hello.generarTrama());

            currentClient = socketClient;
            chatView.mostrarMensajeSistema("Hello 004 enviado a " + ipFinal);
        } catch (Exception e) {
            throw new OperationException("No se pudo conectar automaticamente: " + e.getMessage(), e);
        }
    }

    public void aceptarInvitacion(SocketClient sender, String miId, String miNombre) {
        try {
            AceptacionInvitacion aceptacion = new AceptacionInvitacion(miId, miNombre);
            sender.send(aceptacion.generarTrama());
            currentClient = sender;
        } catch (Exception e) {
            throw new OperationException("Error al aceptar invitacion.", e);
        }
    }

    public void aceptarHello(SocketClient sender, String miId) {
        try {
            AceptarHello aceptarHello = new AceptarHello(miId);
            sender.send(aceptarHello.generarTrama());
            currentClient = sender;
        } catch (Exception e) {
            throw new OperationException("Error al aceptar conexion automatica.", e);
        }
    }

    public void rechazarInvitacion(SocketClient sender, boolean cerrarConexion) {
        try {
            RechazoConexion rechazo = new RechazoConexion();
            sender.send(rechazo.generarTrama());
            if (cerrarConexion) {
                sender.close();
            }
        } catch (Exception e) {
            throw new OperationException("Error al rechazar invitacion.", e);
        }
    }

    public void rechazarHello(SocketClient sender, boolean cerrarConexion) {
        try {
            RechazarHello rechazo = new RechazarHello();
            sender.send(rechazo.generarTrama());
            if (cerrarConexion) {
                sender.close();
            }
        } catch (Exception e) {
            throw new OperationException("Error al rechazar conexion automatica.", e);
        }
    }

    public void usarConexion(SocketClient sender) {
        currentClient = sender;
    }

    public void ponermeOffline(String miId, String idContactoActual) {
        if (currentClient == null) {
            throw new OperationException("No hay conexion activa.");
        }
        try {
            Offline offline = new Offline(miId);
            currentClient.send(offline.generarTrama());
        } catch (Exception e) {
            throw new OperationException("No se pudo enviar offline (0018).", e);
        } finally {
            cerrarConexionActual();
            if (idContactoActual != null) {
                ClientMediator.getInstance().removerCliente(idContactoActual);
            }
        }
    }

    public void cerrarConexionActual() {
        if (currentClient == null) {
            return;
        }
        try {
            currentClient.close();
        } catch (Exception ignored) {
        } finally {
            currentClient = null;
        }
    }
}
