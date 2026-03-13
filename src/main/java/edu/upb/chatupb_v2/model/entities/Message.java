package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.network.ClientMediator;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.DaoHelper;

import java.util.regex.Pattern;

public abstract class Message {
    private String codigo;

    public Message(String codigo) {
        this.codigo = codigo;
    }
    public String getCodigo() {
        return codigo;
    }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public abstract String generarTrama();

    public abstract void execute(ClientMediator mediator, SocketClient sender, DaoHelper<?> daoHelper);

    public String extractClientId() {
        return null;
    }

    public static Message parseMessage(String trama) {
        if (trama == null || trama.isBlank()) {
            throw new IllegalArgumentException("La trama esta vacia.");
        }

        String[] split = trama.split(Pattern.quote("|"), 2);
        if (split.length == 0 || split[0].isBlank()) {
            throw new IllegalArgumentException("Codigo de protocolo invalido.");
        }

        return switch (split[0]) {
            case "001" -> Invitacion.parse(trama);
            case "002" -> AceptacionInvitacion.parse(trama);
            case "003" -> RechazoConexion.parse(trama);
            case "004" -> Hello.parse(trama);
            case "005" -> AceptarHello.parse(trama);
            case "006" -> RechazarHello.parse(trama);
            case "007" -> MensajeChat.parse(trama);
            case "008" -> ConfirmacionLectura.parse(trama);
            case "009" -> EliminarMensaje.parse(trama);
            case "021" -> ImagenChat.parse(trama);
            case "020" -> EnviarContacto.parse(trama);
            case "0018" -> Offline.parse(trama);
            default -> throw new IllegalArgumentException("Protocolo no soportado: " + split[0]);
        };
    }
}

