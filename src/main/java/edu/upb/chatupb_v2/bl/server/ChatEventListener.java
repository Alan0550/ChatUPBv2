package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.bl.message.AceptacionInvitacion;
import edu.upb.chatupb_v2.bl.message.Invitacion;
import edu.upb.chatupb_v2.bl.message.MensajeChat;
import edu.upb.chatupb_v2.bl.message.RechazoConexion;

public interface ChatEventListener {
    void onInvitacionRecibida(Invitacion inv, SocketClient sender);
    void onAceptacionRecibida(AceptacionInvitacion acc, SocketClient sender);
    void onRechazoRecibido(RechazoConexion rechazo, SocketClient sender);
    void onMensajeRecibido(MensajeChat mensaje, SocketClient sender);
    void onClienteOffline(String idUsuario, SocketClient sender);
}
