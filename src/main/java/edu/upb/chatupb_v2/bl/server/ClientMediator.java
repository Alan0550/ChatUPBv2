package edu.upb.chatupb_v2.bl.server;

import java.util.HashMap;
import java.util.Map;

public class ClientMediator {

    private static final ClientMediator instance = new ClientMediator();
    private final HashMap<String, SocketClient> clientes;

    private ClientMediator() {
        this.clientes = new HashMap<>();
    }

    public static ClientMediator getInstance() {
        return instance;
    }

    public synchronized void agregarCliente(String idCliente, SocketClient cliente) {
        if (idCliente == null || cliente == null) {
            return;
        }
        clientes.put(idCliente, cliente);
    }

    public synchronized void removerCliente(String idCliente) {
        if (idCliente == null) {
            return;
        }
        clientes.remove(idCliente);
    }

    public synchronized boolean enviarMensaje(String idCliente, String mensaje) {
        SocketClient cliente = clientes.get(idCliente);
        if (cliente == null) {
            return false;
        }
        try {
            cliente.send(mensaje);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized Map<String, SocketClient> getClientes() {
        return new HashMap<>(clientes);
    }
}
