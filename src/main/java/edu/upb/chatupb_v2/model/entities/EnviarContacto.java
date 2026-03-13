package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.network.ClientMediator;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.DaoHelper;

import java.util.regex.Pattern;

public class EnviarContacto extends Message {

    private String idUser;
    private String nombre;
    private String ip;

    public EnviarContacto() {
        super("020");
    }

    public EnviarContacto(String idUser, String nombre, String ip) {
        super("020");
        this.idUser = idUser;
        this.nombre = nombre;
        this.ip = ip;
    }

    public static EnviarContacto parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"), 4);
        if (split.length != 4) {
            throw new IllegalArgumentException("Formato de trama no valido para 020");
        }
        return new EnviarContacto(split[1], split[2], split[3]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + idUser + "|" + nombre + "|" + ip + System.lineSeparator();
    }

    @Override
    public void execute(ClientMediator mediator, SocketClient sender, DaoHelper<?> daoHelper) {
        mediator.onEnviarContactoRecibido(this, sender);
    }

    @Override
    public String extractClientId() {
        return idUser;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
