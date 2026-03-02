package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Contact;

import java.util.List;

public interface IChatView {
    void onload(List<Contact> contactos);
    void mostrarMensajeSistema(String mensaje);
    void refrescarContacto(Contact contacto);
    void mostrarMensajePropio(String mensaje);
    void mostrarMensajeContacto(String mensaje);
    void limpiarMensajes();
    void limpiarInputMensaje();
}

