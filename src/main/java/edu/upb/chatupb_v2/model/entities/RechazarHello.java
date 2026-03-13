package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.network.ClientMediator;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.DaoHelper;

public class RechazarHello extends Message {

    public RechazarHello() {
        super("006");
    }

    public static RechazarHello parse(String trama) {
        if (!"006".equals(trama)) {
            throw new IllegalArgumentException("Formato de trama no valido para 006");
        }
        return new RechazarHello();
    }

    @Override
    public String generarTrama() {
        return getCodigo() + System.lineSeparator();
    }

    @Override
    public void execute(ClientMediator mediator, SocketClient sender, DaoHelper<?> daoHelper) {
        mediator.onRechazarHelloRecibido(this, sender);
    }
}
