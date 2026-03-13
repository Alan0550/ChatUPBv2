package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.network.ClientMediator;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.DaoHelper;

public class RechazoConexion extends Message {

    public RechazoConexion() {
        super("003");
    }

    public static RechazoConexion parse(String trama) {
        if (!"003".equals(trama)) {
            throw new IllegalArgumentException("Formato de trama no valido para 003");
        }
        return new RechazoConexion();
    }

    @Override
    public String generarTrama() {
        return getCodigo() + System.lineSeparator();
    }

    @Override
    public void execute(ClientMediator mediator, SocketClient sender, DaoHelper<?> daoHelper) {
        mediator.onRechazoRecibido(this, sender);
    }
}

