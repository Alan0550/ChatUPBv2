package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.ConnectionController;
import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.AceptarHello;
import edu.upb.chatupb_v2.model.entities.AceptacionInvitacion;
import edu.upb.chatupb_v2.model.entities.ConfirmacionLectura;
import edu.upb.chatupb_v2.model.entities.EliminarMensaje;
import edu.upb.chatupb_v2.model.entities.EnviarContacto;
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatUI extends JFrame implements ChatEventListener, IChatView {
    private static final String TEXTO_MENSAJE_ELIMINADO = "Este mensaje fue eliminado";
    private static final String MI_ID = "af3bc20a-766c-43d4-813d-b1067a01fa9a";
    private final String miNombre = System.getProperty("user.name", "Alan");
    private ConnectionController connectionController;
    private ContactController contactController;
    private MessageController messageController;
    private final DefaultListModel<Contact> contactModel = new DefaultListModel<>();
    private final Map<String, JLabel> indicadoresLectura = new HashMap<>();
    private final Map<String, JLabel> mensajesPorId = new HashMap<>();
    private final Set<String> mensajesEliminados = new HashSet<>();

    private String idContactoActual;
    private String nombreContactoActual = "Sin contacto";

    private JTextField txtIp;
    private JButton btnConectar;
    private JLabel lblEstado;
    private JLabel lblContacto;
    private JPanel messagesContainer;
    private JScrollPane scrollChat;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    private JButton btnEnviarContacto;
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
        lblEstado = new JLabel("Estado: Sin conexion");
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

        JPanel panelChat = new JPanel(new BorderLayout(0, 0));
        panelChat.setBackground(Color.WHITE);
        panelChat.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        JPanel connectBar = new JPanel(new BorderLayout(8, 0));
        connectBar.setBackground(new Color(245, 247, 246));
        connectBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 210, 210)),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftControls.setOpaque(false);
        leftControls.add(new JLabel("IP destino:"));
        txtIp = new JTextField("127.0.0.1", 16);
        leftControls.add(txtIp);
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
        leftControls.add(btnConectar);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightControls.setOpaque(false);
        btnOffline = new JButton("Offline");
        btnOffline.addActionListener(e -> ponermeOffline());
        rightControls.add(btnOffline);

        connectBar.add(leftControls, BorderLayout.WEST);
        connectBar.add(rightControls, BorderLayout.EAST);

        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(Color.WHITE);
        messagesContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollChat = new JScrollPane(messagesContainer);
        scrollChat.setBorder(BorderFactory.createEmptyBorder());
        scrollChat.getViewport().setBackground(Color.WHITE);

        panelChat.add(connectBar, BorderLayout.NORTH);
        panelChat.add(scrollChat, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelContactos, panelChat);
        splitPane.setDividerLocation(240);
        splitPane.setResizeWeight(0.20);

        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setBackground(new Color(245, 247, 246));
        bottom.setBorder(new EmptyBorder(6, 0, 0, 0));
        txtMensaje = new JTextField();
        btnEnviarContacto = new JButton("Enviar contacto");
        btnEnviarContacto.addActionListener(e -> enviarContactoAAmigo());
        btnEnviar = new JButton("Enviar");
        btnEnviar.addActionListener(e -> enviarMensajeChat());
        JPanel accionesMensaje = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        accionesMensaje.setOpaque(false);
        accionesMensaje.add(btnEnviarContacto);
        accionesMensaje.add(btnEnviar);
        bottom.add(txtMensaje, BorderLayout.CENTER);
        bottom.add(accionesMensaje, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

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
            setEstado("Sin conexion", new Color(255, 230, 153));
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

    private void enviarContactoAAmigo() {
        if (contactController == null) {
            appendSistema("Controlador de contactos no configurado.");
            return;
        }
        if (idContactoActual == null || idContactoActual.isBlank()) {
            appendSistema("Selecciona el amigo destino para compartir contacto.");
            return;
        }

        List<Contact> contactosCompartibles = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        for (int i = 0; i < contactModel.size(); i++) {
            Contact c = contactModel.getElementAt(i);
            if (c == null || c.getCode() == null || c.getCode().isBlank()) {
                continue;
            }
            if (c.getCode().equals(idContactoActual) || MI_ID.equals(c.getCode())) {
                continue;
            }
            contactosCompartibles.add(c);
            etiquetas.add(c.getName() + " (" + c.getCode() + ")");
        }

        if (contactosCompartibles.isEmpty()) {
            appendSistema("No hay contactos para compartir.");
            return;
        }

        String[] opciones = etiquetas.toArray(new String[0]);
        Object elegido = JOptionPane.showInputDialog(
                this,
                "Selecciona el contacto a compartir con " + nombreContactoActual + ":",
                "Compartir contacto",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (elegido == null) {
            return;
        }

        int idx = etiquetas.indexOf(elegido.toString());
        if (idx < 0) {
            return;
        }

        Contact contactoAEnviar = contactosCompartibles.get(idx);
        try {
            contactController.enviarContacto(idContactoActual, contactoAEnviar);
            appendSistema("Contacto enviado: " + contactoAEnviar.getName());
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
    public void onEnviarContactoRecibido(EnviarContacto contacto, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            if (contacto == null) {
                return;
            }

            String ip = contacto.getIp();
            if (ip == null || ip.isBlank()) {
                ip = sender != null ? sender.getIp() : null;
            }

            Contact guardado = guardarOActualizarConControlador(
                    contacto.getIdUser(),
                    contacto.getNombre(),
                    ip,
                    false
            );

            if (guardado != null) {
                appendSistema("Contacto recibido: " + guardado.getName());
            }
        });
    }

    @Override
    public void onEliminarMensajeRecibido(EliminarMensaje eliminarMensaje, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            if (messageController == null) {
                return;
            }
            try {
                messageController.recibirEliminacion(MI_ID, eliminarMensaje);
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
        });
    }

    @Override
    public void onConfirmacionLecturaRecibida(ConfirmacionLectura confirmacion, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            if (messageController != null) {
                try {
                    messageController.confirmarLecturaRecibida(confirmacion);
                } catch (OperationException e) {
                    appendSistema(e.getMessage());
                }
            }
        });
    }

    @Override
    public void onClienteOffline(String idUsuario, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            marcarContactoOnline(idUsuario, false);
            if (idUsuario != null && idUsuario.equals(idContactoActual)) {
                setEstado("Sin conexion", new Color(255, 230, 153));
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
        System.out.println("[Sistema] " + texto);
    }

    private void appendYo(String idMensaje, String texto) {
        agregarBurbuja(idMensaje, texto, true, false);
    }

    private void appendYo(String texto) {
        agregarBurbuja(null, texto, true, false);
    }

    private void appendContacto(String texto) {
        agregarBurbuja(null, texto, false, false);
    }

    private void appendContacto(String idMensaje, String texto) {
        agregarBurbuja(idMensaje, texto, false, false);
    }

    private void appendYoEliminado(String idMensaje) {
        agregarBurbuja(idMensaje, TEXTO_MENSAJE_ELIMINADO, true, true);
    }

    private void appendContactoEliminado(String idMensaje) {
        agregarBurbuja(idMensaje, TEXTO_MENSAJE_ELIMINADO, false, true);
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
    public void mostrarMensajePropio(String idMensaje, String mensaje) {
        SwingUtilities.invokeLater(() -> appendYo(idMensaje, mensaje));
    }

    @Override
    public void mostrarMensajePropioEliminado(String idMensaje) {
        SwingUtilities.invokeLater(() -> appendYoEliminado(idMensaje));
    }

    @Override
    public void mostrarMensajePropio(String mensaje) {
        SwingUtilities.invokeLater(() -> appendYo(mensaje));
    }

    @Override
    public void mostrarMensajeContacto(String idMensaje, String mensaje) {
        SwingUtilities.invokeLater(() -> appendContacto(idMensaje, mensaje));
    }

    @Override
    public void mostrarMensajeContactoEliminado(String idMensaje) {
        SwingUtilities.invokeLater(() -> appendContactoEliminado(idMensaje));
    }

    @Override
    public void mostrarMensajeContacto(String mensaje) {
        SwingUtilities.invokeLater(() -> appendContacto(mensaje));
    }

    @Override
    public void marcarMensajeEliminado(String idMensaje) {
        SwingUtilities.invokeLater(() -> {
            if (idMensaje == null || idMensaje.isBlank()) {
                return;
            }
            JLabel etiqueta = mensajesPorId.get(idMensaje);
            if (etiqueta == null) {
                return;
            }
            mensajesEliminados.add(idMensaje);
            etiqueta.setText("<html><div style='width: 240px;'>" + TEXTO_MENSAJE_ELIMINADO + "</div></html>");
            etiqueta.setForeground(new Color(150, 150, 150));
            etiqueta.revalidate();
            etiqueta.repaint();
        });
    }

    @Override
    public void marcarMensajeLeido(String idMensaje) {
        SwingUtilities.invokeLater(() -> {
            JLabel indicador = indicadoresLectura.get(idMensaje);
            if (indicador == null) {
                return;
            }
            indicador.setText("leido");
            indicador.revalidate();
            indicador.repaint();
            messagesContainer.revalidate();
            messagesContainer.repaint();
        });
    }

    @Override
    public void limpiarMensajes() {
        SwingUtilities.invokeLater(() -> {
            indicadoresLectura.clear();
            mensajesPorId.clear();
            mensajesEliminados.clear();
            messagesContainer.removeAll();
            messagesContainer.revalidate();
            messagesContainer.repaint();
        });
    }

    @Override
    public void limpiarInputMensaje() {
        SwingUtilities.invokeLater(() -> txtMensaje.setText(""));
    }

    private void agregarBurbuja(String idMensaje, String texto, boolean propia, boolean eliminado) {
        JPanel fila = new JPanel(new FlowLayout(propia ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 6));
        fila.setOpaque(false);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        JLabel burbuja = new JLabel(
                "<html><div style='width: 240px;'>" + escaparHtml(texto) + "</div></html>"
        );
        burbuja.setOpaque(true);
        burbuja.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        burbuja.setForeground(eliminado ? new Color(150, 150, 150) : new Color(33, 33, 33));
        burbuja.setBackground(propia ? new Color(220, 248, 198) : new Color(242, 245, 247));
        burbuja.setBorder(new EmptyBorder(8, 12, 8, 12));
        burbuja.setAlignmentX(propia ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        contenido.add(burbuja);
        if (idMensaje != null && !idMensaje.isBlank()) {
            mensajesPorId.put(idMensaje, burbuja);
            if (eliminado) {
                mensajesEliminados.add(idMensaje);
            }
        }

        if (propia && idMensaje != null && !idMensaje.isBlank() && !eliminado) {
            JLabel indicador = new JLabel(" ");
            indicador.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            indicador.setForeground(new Color(18, 140, 126));
            indicador.setBorder(new EmptyBorder(2, 4, 0, 4));
            indicador.setAlignmentX(Component.RIGHT_ALIGNMENT);
            contenido.add(indicador);
            indicadoresLectura.put(idMensaje, indicador);

            if (!eliminado) {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem itemEliminar = new JMenuItem("Eliminar mensaje");
                itemEliminar.addActionListener(e -> eliminarMensajePropio(idMensaje));
                popup.add(itemEliminar);
                burbuja.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                });
            }
        }

        fila.add(contenido);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));

        messagesContainer.add(fila);
        messagesContainer.revalidate();
        messagesContainer.repaint();
        desplazarChatAlFinal();
    }

    private void eliminarMensajePropio(String idMensaje) {
        if (idMensaje == null || idMensaje.isBlank()) {
            return;
        }
        if (mensajesEliminados.contains(idMensaje)) {
            return;
        }
        if (messageController == null) {
            appendSistema("Controlador de mensajes no configurado.");
            return;
        }
        try {
            messageController.eliminarMensaje(MI_ID, idContactoActual, idMensaje);
        } catch (OperationException e) {
            appendSistema(e.getMessage());
        }
    }

    private void desplazarChatAlFinal() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar scrollBar = scrollChat.getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMaximum());
        });
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br>");
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

