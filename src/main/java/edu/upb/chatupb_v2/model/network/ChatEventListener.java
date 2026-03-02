package edu.upb.chatupb_v2.model.network;

import edu.upb.chatupb_v2.model.entities.AceptarHello;
import edu.upb.chatupb_v2.model.entities.AceptacionInvitacion;
import edu.upb.chatupb_v2.model.entities.Hello;
import edu.upb.chatupb_v2.model.entities.Invitacion;
import edu.upb.chatupb_v2.model.entities.MensajeChat;
import edu.upb.chatupb_v2.model.entities.RechazarHello;
import edu.upb.chatupb_v2.model.entities.RechazoConexion;

public interface ChatEventListener {
    void onInvitacionRecibida(Invitacion inv, SocketClient sender);
    void onAceptacionRecibida(AceptacionInvitacion acc, SocketClient sender);
    void onRechazoRecibido(RechazoConexion rechazo, SocketClient sender);
    void onHelloRecibido(Hello hello, SocketClient sender);
    void onAceptarHelloRecibido(AceptarHello aceptarHello, SocketClient sender);
    void onRechazarHelloRecibido(RechazarHello rechazoHello, SocketClient sender);
    void onMensajeRecibido(MensajeChat mensaje, SocketClient sender);
    void onClienteOffline(String idUsuario, SocketClient sender);
}

