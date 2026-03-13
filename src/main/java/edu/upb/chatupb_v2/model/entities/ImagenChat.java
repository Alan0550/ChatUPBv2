package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.network.ClientMediator;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.DaoHelper;

import java.util.regex.Pattern;

public class ImagenChat extends Message {
    private static final String STORAGE_PREFIX = "__IMG__:";

    private String idUser;
    private String idMensaje;
    private String imagenBase64;

    public ImagenChat() {
        super("021");
    }

    public ImagenChat(String idUser, String idMensaje, String imagenBase64) {
        super("021");
        this.idUser = idUser;
        this.idMensaje = idMensaje;
        this.imagenBase64 = imagenBase64;
    }

    public static ImagenChat parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"), 4);
        if (split.length != 4) {
            throw new IllegalArgumentException("Formato de trama no valido para 021");
        }
        return new ImagenChat(split[1], split[2], split[3]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() + "|" + idUser + "|" + idMensaje + "|" + imagenBase64 + System.lineSeparator();
    }

    @Override
    public void execute(ClientMediator mediator, SocketClient sender, DaoHelper<?> daoHelper) {
        mediator.onImagenRecibida(this, sender);
    }

    @Override
    public String extractClientId() {
        return idUser;
    }

    public String toStoredContent() {
        return STORAGE_PREFIX + (imagenBase64 == null ? "" : imagenBase64);
    }

    public static boolean isStoredContent(String contenido) {
        return contenido != null && contenido.startsWith(STORAGE_PREFIX);
    }

    public static String extractBase64(String contenido) {
        if (!isStoredContent(contenido)) {
            return null;
        }
        return contenido.substring(STORAGE_PREFIX.length());
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

    public String getImagenBase64() {
        return imagenBase64;
    }

    public void setImagenBase64(String imagenBase64) {
        this.imagenBase64 = imagenBase64;
    }
}
