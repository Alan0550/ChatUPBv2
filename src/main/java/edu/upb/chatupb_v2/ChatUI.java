package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.bl.message.AceptacionInvitacion;
import edu.upb.chatupb_v2.bl.message.Invitacion;
import edu.upb.chatupb_v2.bl.message.MensajeChat;
import edu.upb.chatupb_v2.bl.message.RechazoConexion;
import edu.upb.chatupb_v2.bl.server.ChatEventListener;
import edu.upb.chatupb_v2.bl.server.ClientMediator;
import edu.upb.chatupb_v2.bl.server.SocketClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.UUID;

public class ChatUI extends JFrame implements ChatEventListener {
    private SocketClient client;
    private final String miId = UUID.randomUUID().toString();
    private final String miNombre = System.getProperty("user.name", "Usuario");
    private String idContactoActual;
    private String nombreContactoActual = "Sin contacto";

    private JTextField txtIp;
    private JButton btnConectar;
    private JLabel lblEstado;
    private JLabel lblContacto;
    private JTextArea areaChat;
    private JTextField txtMensaje;
    private JButton btnEnviar;

    public ChatUI() {
        initUI();
    }

    private void initUI() {
        setTitle("Chat UPB - P2P");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(680, 500));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(new Color(236, 240, 237));
        setContentPane(root);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(new EmptyBorder(8, 10, 8, 10));
        top.setBackground(new Color(18, 140, 126));

        JLabel titulo = new JLabel("ChatUPB");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        lblContacto = new JLabel("Contacto: " + nombreContactoActual);
        lblContacto.setForeground(Color.WHITE);
        lblEstado = new JLabel("Estado: Desconectado");
        lblEstado.setForeground(new Color(255, 230, 153));
        topRight.add(lblContacto);
        topRight.add(lblEstado);

        top.add(titulo, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);

        JPanel connectBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        connectBar.setOpaque(false);
        connectBar.add(new JLabel("IP destino:"));
        txtIp = new JTextField("127.0.0.1", 16);
        connectBar.add(txtIp);
        btnConectar = new JButton("Conectar");
        btnConectar.addActionListener(e -> conectar());
        connectBar.add(btnConectar);

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        areaChat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        areaChat.setBackground(Color.WHITE);
        areaChat.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(areaChat);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        center.add(connectBar, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setOpaque(false);
        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnEnviar.addActionListener(e -> enviarMensajeChat());
        bottom.add(txtMensaje, BorderLayout.CENTER);
        bottom.add(btnEnviar, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        appendSistema("Tu id: " + miId);
        pack();
    }

    private void conectar() {
        try {
            String ip = txtIp.getText().trim();
            if (ip.isEmpty()) {
                ip = "127.0.0.1";
                txtIp.setText(ip);
            }

            client = new SocketClient(ip);
            client.setChatEventListener(this);
            client.start();

            Invitacion invitacion = new Invitacion(miId, miNombre);
            client.send(invitacion.generarTrama());
            setEstado("Esperando respuesta", new Color(255, 230, 153));
            appendSistema("Invitacion 001 enviada a " + ip);
        } catch (Exception e) {
            appendSistema("No se pudo conectar: " + e.getMessage());
        }
    }

    private void enviarMensajeChat() {
        if (client != null) {
            try {
                String texto = txtMensaje.getText().trim();
                if (texto.isEmpty()) {
                    return;
                }
                MensajeChat mensajeChat = new MensajeChat(miId, UUID.randomUUID().toString(), texto);
                String trama = mensajeChat.generarTrama();
                boolean enviado = false;
                if (idContactoActual != null) {
                    enviado = ClientMediator.getInstance().enviarMensaje(idContactoActual, trama);
                }
                if (!enviado) {
                    client.send(trama);
                }
                appendYo(texto);
                txtMensaje.setText("");
            } catch (Exception e) {
                appendSistema("No se pudo enviar mensaje");
            }
        }
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ChatUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChatUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChatUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChatUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(() -> new ChatUI().setVisible(true));
    }

    @Override
    public void onInvitacionRecibida(Invitacion inv, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "Invitacion de " + inv.getNombre() + ".\nDeseas aceptar la conexion?",
                    "Solicitud 001",
                    JOptionPane.YES_NO_OPTION
            );

            try {
                if (respuesta == JOptionPane.YES_OPTION) {
                    AceptacionInvitacion aceptacion = new AceptacionInvitacion(miId, miNombre);
                    sender.send(aceptacion.generarTrama());
                    client = sender;
                    idContactoActual = inv.getIdUsuario();
                    nombreContactoActual = inv.getNombre();
                    setEstado("Conectado", new Color(187, 247, 208));
                    setContacto(nombreContactoActual);
                    appendSistema("Conexion aceptada. Ya pueden chatear.");
                } else {
                    RechazoConexion rechazo = new RechazoConexion();
                    sender.send(rechazo.generarTrama());
                    appendSistema("Conexion rechazada.");
                }
            } catch (Exception e) {
                appendSistema("Error al responder invitacion.");
            }
        });
    }

    @Override
    public void onAceptacionRecibida(AceptacionInvitacion acc, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            client = sender;
            idContactoActual = acc.getIdUsuario();
            nombreContactoActual = acc.getNombre();
            setEstado("Conectado", new Color(187, 247, 208));
            setContacto(nombreContactoActual);
            appendSistema(acc.getNombre() + " acepto tu invitacion 002.");
        });
    }

    @Override
    public void onRechazoRecibido(RechazoConexion rechazo, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            setEstado("Rechazado", new Color(254, 202, 202));
            appendSistema("La solicitud fue rechazada (003).");
        });
    }

    @Override
    public void onMensajeRecibido(MensajeChat mensaje, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            if (idContactoActual == null) {
                idContactoActual = mensaje.getIdUser();
                setContacto(nombreContactoActual);
            }
            appendContacto(mensaje.getMensaje());
        });
    }

    private void appendSistema(String texto) {
        areaChat.append("[Sistema] " + texto + System.lineSeparator());
    }

    private void appendYo(String texto) {
        areaChat.append("Yo: " + texto + System.lineSeparator());
    }

    private void appendContacto(String texto) {
        areaChat.append(nombreContactoActual + ": " + texto + System.lineSeparator());
    }

    private void setEstado(String texto, Color color) {
        lblEstado.setText("Estado: " + texto);
        lblEstado.setForeground(color);
    }

    private void setContacto(String nombre) {
        lblContacto.setText("Contacto: " + nombre);
    }
}
