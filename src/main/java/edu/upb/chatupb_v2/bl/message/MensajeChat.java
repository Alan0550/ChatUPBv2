package edu.upb.chatupb_v2.bl.message;

import java.util.regex.Pattern;

public class MensajeChat extends Message {

    private String idUser;
    private String idMensaje;
    private String mensaje;

    public MensajeChat() {
        super("007");
    }

    public MensajeChat(String idUser, String idMensaje, String mensaje) {
        super("007");
        this.idUser = idUser;
        this.idMensaje = idMensaje;
        this.mensaje = mensaje;
    }

    public static MensajeChat parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"), 4);
        if (split.length != 4) {
            throw new IllegalArgumentException("Formato de trama no valido para 007");
        }
        return new MensajeChat(split[1], split[2], split[3]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + idUser + "|" + idMensaje + "|" + mensaje + System.lineSeparator();
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(String idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
