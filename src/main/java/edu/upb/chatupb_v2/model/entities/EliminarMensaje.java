package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.network.ClientMediator;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.DaoHelper;

import java.util.regex.Pattern;

public class EliminarMensaje extends Message {
    private String idMensaje;

    public EliminarMensaje() {
        super("009");
    }

    public EliminarMensaje(String idMensaje) {
        super("009");
        this.idMensaje = idMensaje;
    }

    public static EliminarMensaje parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"), 2);
        if (split.length != 2) {
            throw new IllegalArgumentException("Formato de trama no valido para 009");
        }
        return new EliminarMensaje(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + idMensaje + System.lineSeparator();
    }

    @Override
    public void execute(ClientMediator mediator, SocketClient sender, DaoHelper<?> daoHelper) {
        mediator.onEliminarMensajeRecibido(this, sender);
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(String idMensaje) {
        this.idMensaje = idMensaje;
    }
}
