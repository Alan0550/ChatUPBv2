package edu.upb.chatupb_v2.model.entities;

import java.util.regex.Pattern;

public class AceptarHello extends Message {

    private String idUsuario;

    public AceptarHello() {
        super("005");
    }

    public AceptarHello(String idUsuario) {
        super("005");
        this.idUsuario = idUsuario;
    }

    public static AceptarHello parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if (split.length != 2) {
            throw new IllegalArgumentException("Formato de trama no valido para 005");
        }
        return new AceptarHello(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + idUsuario + System.lineSeparator();
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
}
