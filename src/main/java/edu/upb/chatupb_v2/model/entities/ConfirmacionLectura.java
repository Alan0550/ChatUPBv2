package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.network.ClientMediator;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.DaoHelper;

import java.util.regex.Pattern;

public class ConfirmacionLectura extends Message {

    private String idMensaje;

    public ConfirmacionLectura() {
        super("008");
    }

    public ConfirmacionLectura(String idMensaje) {
        super("008");
        this.idMensaje = idMensaje;
    }

    public static ConfirmacionLectura parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if (split.length != 2) {
            throw new IllegalArgumentException("Formato de trama no valido para 008");
        }
        return new ConfirmacionLectura(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + idMensaje + System.lineSeparator();
    }

    @Override
    public void execute(ClientMediator mediator, SocketClient sender, DaoHelper<?> daoHelper) {
        mediator.onConfirmacionLecturaRecibida(this, sender);
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(String idMensaje) {
        this.idMensaje = idMensaje;
    }
}
