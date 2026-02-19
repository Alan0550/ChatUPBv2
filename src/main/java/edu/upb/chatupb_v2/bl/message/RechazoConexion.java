package edu.upb.chatupb_v2.bl.message;

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
}
