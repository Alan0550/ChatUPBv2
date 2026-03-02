package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.ConnectionController;
import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.AceptarHello;
import edu.upb.chatupb_v2.model.entities.AceptacionInvitacion;
import edu.upb.chatupb_v2.model.entities.Invitacion;
import edu.upb.chatupb_v2.model.entities.Hello;
import edu.upb.chatupb_v2.model.entities.MensajeChat;
import edu.upb.chatupb_v2.model.entities.RechazarHello;
import edu.upb.chatupb_v2.model.entities.RechazoConexion;
import edu.upb.chatupb_v2.model.network.ChatEventListener;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.model.repository.BlackListDao;
import edu.upb.chatupb_v2.model.entities.Contact;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ChatUI extends JFrame implements ChatEventListener, IChatView {
    private static final String MI_ID = "af3bc20a-766c-4cd4-813d-b1067a01fa9a";
    private final String miNombre = System.getProperty("user.name", "Alan");
    private ConnectionController connectionController;
    private ContactController contactController;
    private MessageController messageController;
    private final DefaultListModel<Contact> contactModel = new DefaultListModel<>();

    private String idContactoActual;
    private String nombreContactoActual = "Sin contacto";

    private JTextField txtIp;
    private JButton btnConectar;
    private JLabel lblEstado;
    private JLabel lblContacto;
    private JTextArea areaChat;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    private JButton btnOffline;
    private JList<Contact> listContactos;


    public ChatUI() {
        initUI();
    }

    public void setConnectionController(ConnectionController connectionController) {
        this.connectionController = connectionController;
    }

    public void setContactController(ContactController contactController) {
        this.contactController = contactController;
        if (this.contactController != null) {
            try {
                this.contactController.onload();
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
        }
    }

    public void setMessageController(MessageController messageController) {
        this.messageController = messageController;
        if (this.messageController != null) {
            try {
                this.messageController.onload();
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
        }
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
//
        JPanel panelContactos = new JPanel(new BorderLayout(6, 6));
        panelContactos.setBorder(BorderFactory.createTitledBorder("Contactos"));
        panelContactos.setBackground(Color.WHITE);

        listContactos = new JList<>(contactModel);
        listContactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listContactos.setCellRenderer(new ContactCellRenderer());
        listContactos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onContactoSeleccionado();
            }
        });
        panelContactos.add(new JScrollPane(listContactos), BorderLayout.CENTER);

        JPanel panelChat = new JPanel(new BorderLayout(0, 8));
        panelChat.setOpaque(false);

        JPanel connectBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        connectBar.setOpaque(false);
        connectBar.add(new JLabel("IP destino:"));
        txtIp = new JTextField("127.0.0.1", 16);
        connectBar.add(txtIp);
        btnConectar = new JButton("Conectar");
        btnConectar.addActionListener(e -> conectar());
        btnConectar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    conectarAutomatico();
                }
            }
        });
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

        panelChat.add(connectBar, BorderLayout.NORTH);
        panelChat.add(scroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelContactos, panelChat);
        splitPane.setDividerLocation(240);
        splitPane.setResizeWeight(0.20);

        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setOpaque(false);
        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnEnviar.addActionListener(e -> enviarMensajeChat());
        bottom.add(txtMensaje, BorderLayout.CENTER);
        bottom.add(btnEnviar, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        JPanel botton = new JPanel(new BorderLayout(8, 0));
        botton.setOpaque(false);
        btnOffline = new JButton("Offline");
        btnOffline.addActionListener(e -> ponermeOffline());
        botton.add(btnOffline, BorderLayout.SOUTH);
        root.add(botton, BorderLayout.EAST);

        appendSistema("Tu id: " + MI_ID);
        setSize(980, 620);
    }

    private void ponermeOffline() {
        if (connectionController == null) {
            appendSistema("No hay conexion activa.");
            return;
        }
        try {
            connectionController.ponermeOffline(MI_ID, idContactoActual);
            marcarContactoOnline(idContactoActual, false);
            idContactoActual = null;
            nombreContactoActual = "Sin contacto";
            setContacto(nombreContactoActual);
            setEstado("Desconectado", new Color(255, 230, 153));
            appendSistema("Te desconectaste (0018).");
        } catch (OperationException e) {
            appendSistema(e.getMessage());
        }
    }
    private void onContactoSeleccionado() {
        Contact seleccionado = listContactos.getSelectedValue();
        if (seleccionado == null) {
            return;
        }
        idContactoActual = seleccionado.getCode();
        nombreContactoActual = seleccionado.getName();
        setContacto(nombreContactoActual);

        if (seleccionado.getIp() != null && !seleccionado.getIp().isBlank()) {
            txtIp.setText(seleccionado.getIp());
        }
        if (seleccionado.isStateConnect()) {
            setEstado("Conectado", new Color(187, 247, 208));
        } else {
            setEstado("Sin conexion", new Color(255, 230, 153));
        }
        if (messageController != null) {
            try {
                messageController.cargarHistorial(MI_ID, idContactoActual);
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
        }
    }

    private Contact guardarOActualizarConControlador(String idUsuario, String nombre, String ip, boolean online) {
        if (contactController == null) {
            appendSistema("Controlador de contactos no configurado.");
            return null;
        }
        try {
            return contactController.guardarOActualizarContacto(idUsuario, nombre, ip, online);
        } catch (OperationException e) {
            appendSistema(e.getMessage());
            return null;
        }
    }

    private void upsertContactoEnLista(Contact contacto) {
        int index = buscarIndiceContacto(contacto.getCode());
        if (index >= 0) {
            contactModel.set(index, contacto);
        } else {
            contactModel.addElement(contacto);
        }
    }

    private int buscarIndiceContacto(String code) {
        if (code == null) {
            return -1;
        }
        for (int i = 0; i < contactModel.size(); i++) {
            Contact c = contactModel.getElementAt(i);
            if (code.equals(c.getCode())) {
                return i;
            }
        }
        return -1;
    }

    private Contact buscarContactoPorId(String code) {
        int index = buscarIndiceContacto(code);
        if (index < 0) {
            return null;
        }
        return contactModel.getElementAt(index);
    }

    private void marcarContactoOnline(String idUsuario, boolean online) {
        if (idUsuario == null) {
            return;
        }
        int index = buscarIndiceContacto(idUsuario);
        if (index < 0) {
            return;
        }
        Contact contacto = contactModel.getElementAt(index);
        contacto.setStateConnect(online);
        contactModel.set(index, contacto);
    }

    private void seleccionarContactoEnLista(String idUsuario) {
        int index = buscarIndiceContacto(idUsuario);
        if (index >= 0) {
            listContactos.setSelectedIndex(index);
        }
    }

    private void conectar() {
        if (connectionController == null) {
            appendSistema("Controlador de conexion no configurado.");
            return;
        }
        String ip = txtIp.getText().trim();
        if (ip.isEmpty()) {
            ip = "127.0.0.1";
            txtIp.setText(ip);
        }
        try {
            connectionController.conectarPorIp(ip, MI_ID, miNombre);
            setEstado("Esperando respuesta", new Color(255, 230, 153));
        } catch (OperationException e) {
            appendSistema(e.getMessage());
        }
    }

    private void conectarAutomatico() {
        if (connectionController == null) {
            appendSistema("Controlador de conexion no configurado.");
            return;
        }
        String ip = txtIp.getText().trim();
        if (ip.isEmpty()) {
            ip = "127.0.0.1";
            txtIp.setText(ip);
        }
        try {
            connectionController.conectarAutomaticoPorIp(ip, MI_ID);
            setEstado("Esperando respuesta", new Color(255, 230, 153));
        } catch (OperationException e) {
            appendSistema(e.getMessage());
        }
    }

    private void enviarMensajeChat() {
        if (messageController == null) {
            appendSistema("Controlador de mensajes no configurado.");
            return;
        }
        try {
            messageController.enviarMensaje(MI_ID, idContactoActual, txtMensaje.getText());
        } catch (OperationException e) {
            appendSistema(e.getMessage());
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
        BlackListDao blackListDao = new BlackListDao();
        String ipRemota = sender.getIp();
        guardarOActualizarConControlador(inv.getIdUsuario(), inv.getNombre(), ipRemota, false);

        if (blackListDao.isBlacklisted(ipRemota)) {
            try {
                if (connectionController == null) {
                    throw new OperationException("Controlador de conexion no configurado.");
                }
                connectionController.rechazarInvitacion(sender, true);
                SwingUtilities.invokeLater(() -> appendSistema("Solicitud rechazada (IP en lista negra)."));
            } catch (OperationException e) {
                SwingUtilities.invokeLater(() -> appendSistema("Error al rechazar automaticamente."));
            }
            return;
        }
        SwingUtilities.invokeLater(() -> {
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "Invitacion de " + inv.getNombre() + ".\nDeseas aceptar la conexion?",
                    "Solicitud 001",
                    JOptionPane.YES_NO_OPTION
            );

            try {
                if (respuesta == JOptionPane.YES_OPTION) {
                    if (connectionController == null) {
                        throw new OperationException("Controlador de conexion no configurado.");
                    }
                    connectionController.aceptarInvitacion(sender, MI_ID, miNombre);
                    idContactoActual = inv.getIdUsuario();
                    nombreContactoActual = inv.getNombre();
                    setEstado("Conectado", new Color(187, 247, 208));
                    setContacto(nombreContactoActual);
                    guardarOActualizarConControlador(inv.getIdUsuario(), inv.getNombre(), sender.getIp(), true);
                    marcarContactoOnline(inv.getIdUsuario(), true);
                    seleccionarContactoEnLista(inv.getIdUsuario());
                    appendSistema("Conexion aceptada. Ya pueden chatear.");
                } else {
                    if (connectionController == null) {
                        throw new OperationException("Controlador de conexion no configurado.");
                    }
                    connectionController.rechazarInvitacion(sender, false);

                    BlackListDao blackListDao2 = new BlackListDao();

                    blackListDao2.addToBlacklist(sender.getIp());

                    appendSistema("Conexion rechazada y agregada a lista negra.");
                }
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
        });
    }

    @Override
    public void onAceptacionRecibida(AceptacionInvitacion acc, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            if (connectionController != null) {
                connectionController.usarConexion(sender);
            }
            idContactoActual = acc.getIdUsuario();
            nombreContactoActual = acc.getNombre();
            setEstado("Conectado", new Color(187, 247, 208));
            setContacto(nombreContactoActual);
            guardarOActualizarConControlador(acc.getIdUsuario(), acc.getNombre(), sender.getIp(), true);
            marcarContactoOnline(acc.getIdUsuario(), true);
            seleccionarContactoEnLista(acc.getIdUsuario());
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
    public void onHelloRecibido(Hello hello, SocketClient sender) {
        if (hello == null) {
            return;
        }

        boolean existe;
        try {
            if (contactController == null) {
                throw new OperationException("Controlador de contactos no configurado.");
            }
            existe = contactController.existeContactoPorCodigo(hello.getIdUsuario());
        } catch (OperationException e) {
            SwingUtilities.invokeLater(() -> appendSistema(e.getMessage()));
            return;
        }

        if (!existe) {
            try {
                if (connectionController == null) {
                    throw new OperationException("Controlador de conexion no configurado.");
                }
                connectionController.rechazarHello(sender, true);
                SwingUtilities.invokeLater(() -> {
                    setEstado("Sin conexion", new Color(255, 230, 153));
                    appendSistema("Conexion automatica rechazada (006).");
                });
            } catch (OperationException e) {
                SwingUtilities.invokeLater(() -> appendSistema(e.getMessage()));
            }
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                if (connectionController == null) {
                    throw new OperationException("Controlador de conexion no configurado.");
                }
                connectionController.aceptarHello(sender, MI_ID);
                idContactoActual = hello.getIdUsuario();
                Contact contacto = buscarContactoPorId(idContactoActual);
                nombreContactoActual = contacto != null ? contacto.getName() : idContactoActual;
                guardarOActualizarConControlador(idContactoActual, nombreContactoActual, sender.getIp(), true);
                marcarContactoOnline(idContactoActual, true);
                seleccionarContactoEnLista(idContactoActual);
                setEstado("Conectado", new Color(187, 247, 208));
                setContacto(nombreContactoActual);
                appendSistema("Conexion automatica aceptada (005).");
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
        });
    }

    @Override
    public void onAceptarHelloRecibido(AceptarHello aceptarHello, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            if (connectionController != null) {
                connectionController.usarConexion(sender);
            }
            idContactoActual = aceptarHello.getIdUsuario();
            Contact contacto = buscarContactoPorId(idContactoActual);
            nombreContactoActual = contacto != null ? contacto.getName() : idContactoActual;
            guardarOActualizarConControlador(idContactoActual, nombreContactoActual, sender.getIp(), true);
            marcarContactoOnline(idContactoActual, true);
            seleccionarContactoEnLista(idContactoActual);
            setEstado("Conectado", new Color(187, 247, 208));
            setContacto(nombreContactoActual);
            appendSistema("Conexion automatica establecida (005).");
        });
    }

    @Override
    public void onRechazarHelloRecibido(RechazarHello rechazoHello, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            if (connectionController != null) {
                connectionController.cerrarConexionActual();
            }
            setEstado("Rechazado", new Color(254, 202, 202));
            appendSistema("La conexion automatica fue rechazada (006).");
        });
    }

    @Override
    public void onMensajeRecibido(MensajeChat mensaje, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            Contact contactoActual = buscarContactoPorId(mensaje.getIdUser());
            String nombre = contactoActual != null ? contactoActual.getName() : mensaje.getIdUser();
            guardarOActualizarConControlador(mensaje.getIdUser(), nombre, sender.getIp(), true);
            if (idContactoActual == null) {
                idContactoActual = mensaje.getIdUser();
                Contact contacto = buscarContactoPorId(idContactoActual);
                if (contacto != null) {
                    nombreContactoActual = contacto.getName();
                }
                setContacto(nombreContactoActual);
            }
            marcarContactoOnline(mensaje.getIdUser(), true);
            seleccionarContactoEnLista(mensaje.getIdUser());
            if (messageController != null) {
                try {
                    messageController.recibirMensaje(mensaje);
                } catch (OperationException e) {
                    appendSistema(e.getMessage());
                }
            } else {
                appendContacto(mensaje.getMensaje());
            }
        });
    }

    @Override
    public void onClienteOffline(String idUsuario, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            marcarContactoOnline(idUsuario, false);
            if (idUsuario != null && idUsuario.equals(idContactoActual)) {
                setEstado("Desconectado", new Color(255, 230, 153));
                appendSistema("El contacto se ha desconectado (0018).");
                if (connectionController != null) {
                    connectionController.cerrarConexionActual();
                }
                idContactoActual = null;
                nombreContactoActual = "Sin contacto";
                setContacto(nombreContactoActual);
            }
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

    @Override
    public void onload(List<Contact> contactos) {
        SwingUtilities.invokeLater(() -> {
            contactModel.clear();
            for (Contact c : contactos) {
                contactModel.addElement(c);
            }
            appendSistema("Contactos cargados: " + contactos.size());
        });
    }

    @Override
    public void mostrarMensajeSistema(String mensaje) {
        SwingUtilities.invokeLater(() -> appendSistema(mensaje));
    }

    @Override
    public void refrescarContacto(Contact contacto) {
        SwingUtilities.invokeLater(() -> upsertContactoEnLista(contacto));
    }

    @Override
    public void mostrarMensajePropio(String mensaje) {
        SwingUtilities.invokeLater(() -> appendYo(mensaje));
    }

    @Override
    public void mostrarMensajeContacto(String mensaje) {
        SwingUtilities.invokeLater(() -> appendContacto(mensaje));
    }

    @Override
    public void limpiarMensajes() {
        SwingUtilities.invokeLater(() -> areaChat.setText(""));
    }

    @Override
    public void limpiarInputMensaje() {
        SwingUtilities.invokeLater(() -> txtMensaje.setText(""));
    }

    private static class ContactCellRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Contact) {
                Contact c = (Contact) value;
                String estado = c.isStateConnect() ? "[ON]" : "[OFF]";
                label.setText(estado + " " + c.getName());
            }
            return label;
        }
    }
}

