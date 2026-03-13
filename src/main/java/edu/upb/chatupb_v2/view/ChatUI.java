package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.ConnectionController;
import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.model.entities.ConfirmacionLectura;
import edu.upb.chatupb_v2.model.entities.EliminarMensaje;
import edu.upb.chatupb_v2.model.entities.EnviarContacto;
import edu.upb.chatupb_v2.model.entities.ImagenChat;
import edu.upb.chatupb_v2.model.entities.MensajeChat;
import edu.upb.chatupb_v2.model.entities.Contact;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatUI extends JFrame implements IChatView {
    private static final String TEXTO_MENSAJE_ELIMINADO = "Este mensaje fue eliminado";
    private static final int MAX_CHARS_CONTINUOS_BURBUJA = 28;
    private static final int MAX_IMAGE_BUBBLE_SIZE = 260;
    private static final String MI_ID = "af3bc20a-766c-43d4-813d-b1067a01fa9a";
    private static final Color COLOR_APP_GREEN = new Color(45, 112, 82);
    private static final Color COLOR_BG = new Color(235, 238, 236);
    private static final Color COLOR_PANEL = new Color(244, 246, 245);
    private static final Color COLOR_CHAT_BG = new Color(233, 237, 233);
    private static final Color COLOR_OWN_BUBBLE = new Color(43, 112, 81);
    private static final Color COLOR_OTHER_BUBBLE = Color.WHITE;
    private static final Color COLOR_TEXT_PRIMARY = new Color(34, 44, 39);
    private static final Color COLOR_TEXT_MUTED = new Color(122, 132, 126);
    private final String miNombre = System.getProperty("user.name", "Alan");
    private ConnectionController connectionController;
    private ContactController contactController;
    private MessageController messageController;
    private final DefaultListModel<Contact> contactModel = new DefaultListModel<>();
    private final Map<String, JLabel> indicadoresLectura = new HashMap<>();
    private final Map<String, JLabel> mensajesPorId = new HashMap<>();
    private final Set<String> mensajesEliminados = new HashSet<>();
    private final Map<String, Integer> mensajesNoLeidos = new HashMap<>();
    private final Map<String, List<String>> idsNoLeidosPorContacto = new HashMap<>();

    private String idContactoActual;
    private String nombreContactoActual = "Sin contacto";

    private JTextField txtIp;
    private JButton btnConectar;
    private JLabel lblEstado;
    private JLabel lblContacto;
    private JLabel lblUsuarioLocal;
    private JLabel lblUsuarioIp;
    private JPanel messagesContainer;
    private JScrollPane scrollChat;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    private JButton btnEnviarContacto;
    private JButton btnEnviarImagen;
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
        setTitle("ChatUPB - P2P");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG);
        setContentPane(root);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10, 16, 10, 16));
        top.setBackground(COLOR_APP_GREEN);

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("ChatUPB");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JLabel subtitulo = new JLabel("P2P MESSENGER");
        subtitulo.setForeground(new Color(178, 220, 201));
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        brandPanel.add(titulo);
        brandPanel.add(subtitulo);

        JPanel topRight = new JPanel();
        topRight.setOpaque(false);
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));

        lblUsuarioLocal = new JLabel(miNombre);
        lblUsuarioLocal.setForeground(Color.WHITE);
        lblUsuarioLocal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblUsuarioLocal.setAlignmentX(Component.RIGHT_ALIGNMENT);

        lblUsuarioIp = new JLabel("ID: " + MI_ID.substring(0, 8) + "...");
        lblUsuarioIp.setForeground(new Color(185, 220, 204));
        lblUsuarioIp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUsuarioIp.setAlignmentX(Component.RIGHT_ALIGNMENT);

        topRight.add(lblUsuarioLocal);
        topRight.add(lblUsuarioIp);

        top.add(brandPanel, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);

        JPanel panelContactos = new JPanel(new BorderLayout());
        panelContactos.setBackground(COLOR_PANEL);
        panelContactos.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(214, 219, 216)));

        JLabel lblContactos = new JLabel("CONTACTOS");
        lblContactos.setForeground(new Color(126, 137, 131));
        lblContactos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblContactos.setBorder(new EmptyBorder(12, 14, 8, 14));

        listContactos = new JList<>(contactModel);
        listContactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listContactos.setCellRenderer(new ContactCellRenderer(mensajesNoLeidos));
        listContactos.setBackground(COLOR_PANEL);
        listContactos.setBorder(new EmptyBorder(6, 8, 6, 8));
        listContactos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onContactoSeleccionado();
            }
        });
        JScrollPane scrollContactos = new JScrollPane(listContactos);
        scrollContactos.setBorder(BorderFactory.createEmptyBorder());
        scrollContactos.getViewport().setBackground(COLOR_PANEL);

        JPanel panelConexionP2P = new JPanel();
        panelConexionP2P.setLayout(new BoxLayout(panelConexionP2P, BoxLayout.Y_AXIS));
        panelConexionP2P.setBackground(new Color(240, 243, 241));
        panelConexionP2P.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(215, 220, 217)),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblConexion = new JLabel("");
        lblConexion.setForeground(new Color(131, 141, 136));
        lblConexion.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblIp = new JLabel("ip destino");
        lblIp.setForeground(new Color(114, 123, 118));
        lblIp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIp.setBorder(new EmptyBorder(10, 0, 4, 0));

        txtIp = new JTextField("127.0.0.1", 16);
        txtIp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        styleTextField(txtIp);

        JPanel accionesP2P = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        accionesP2P.setOpaque(false);

        btnConectar = new JButton("Conectar");
        stylePrimaryButton(btnConectar);
        btnConectar.addActionListener(e -> conectar());
        btnConectar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    conectarAutomatico();
                }
            }
        });

        btnOffline = new JButton("Offline");
        styleDangerButton(btnOffline);
        btnOffline.addActionListener(e -> ponermeOffline());

        accionesP2P.add(btnConectar);
        accionesP2P.add(btnOffline);

        panelConexionP2P.add(lblConexion);
        panelConexionP2P.add(lblIp);
        panelConexionP2P.add(txtIp);
        panelConexionP2P.add(accionesP2P);

        panelContactos.add(lblContactos, BorderLayout.NORTH);
        panelContactos.add(scrollContactos, BorderLayout.CENTER);
        panelContactos.add(panelConexionP2P, BorderLayout.SOUTH);

        JPanel panelChat = new JPanel(new BorderLayout());
        panelChat.setBackground(COLOR_CHAT_BG);

        JPanel chatHeader = new JPanel();
        chatHeader.setLayout(new BoxLayout(chatHeader, BoxLayout.Y_AXIS));
        chatHeader.setBackground(Color.WHITE);
        chatHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 222)),
                new EmptyBorder(12, 18, 10, 18)
        ));

        lblContacto = new JLabel(nombreContactoActual);
        lblContacto.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblContacto.setForeground(COLOR_TEXT_PRIMARY);

        lblEstado = new JLabel("Sin conexion");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEstado.setForeground(new Color(170, 123, 71));

        chatHeader.add(lblContacto);
        chatHeader.add(lblEstado);

        messagesContainer = new DotPatternPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(COLOR_CHAT_BG);
        messagesContainer.setBorder(new EmptyBorder(14, 18, 14, 18));

        scrollChat = new JScrollPane(messagesContainer);
        scrollChat.setBorder(BorderFactory.createEmptyBorder());
        scrollChat.getViewport().setBackground(COLOR_CHAT_BG);
        scrollChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollChat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        panelChat.add(chatHeader, BorderLayout.NORTH);
        panelChat.add(scrollChat, BorderLayout.CENTER);

        JPanel composer = new JPanel(new BorderLayout(8, 0));
        composer.setBackground(new Color(248, 249, 248));
        composer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(218, 223, 220)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        txtMensaje = new JTextField();
        styleTextField(txtMensaje);

        btnEnviarContacto = new JButton("Enviar contacto");
        styleSecondaryButton(btnEnviarContacto);
        btnEnviarContacto.addActionListener(e -> enviarContactoAAmigo());

        btnEnviarImagen = new JButton("Imagen");
        styleSecondaryButton(btnEnviarImagen);
        btnEnviarImagen.addActionListener(e -> enviarImagenChat());

        btnEnviar = new JButton("Enviar");
        stylePrimaryButton(btnEnviar);
        btnEnviar.addActionListener(e -> enviarMensajeChat());

        JPanel accionesMensaje = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        accionesMensaje.setOpaque(false);
        accionesMensaje.add(btnEnviarContacto);
        accionesMensaje.add(btnEnviarImagen);
        accionesMensaje.add(btnEnviar);

        composer.add(txtMensaje, BorderLayout.CENTER);
        composer.add(accionesMensaje, BorderLayout.EAST);
        panelChat.add(composer, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelContactos, panelChat);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.24);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        root.add(top, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);

        appendSistema("Tu id: " + MI_ID);
        setSize(1280, 800);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 214, 210)),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(COLOR_APP_GREEN);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        button.setFocusPainted(false);
        button.setOpaque(true);
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(221, 235, 228));
        button.setForeground(new Color(40, 101, 74));
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        button.setFocusPainted(false);
        button.setOpaque(true);
    }

    private void styleDangerButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(255, 239, 240));
        button.setForeground(new Color(200, 74, 74));
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        button.setFocusPainted(false);
        button.setOpaque(true);
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
                limpiarNoLeidosContacto(idContactoActual, true);
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

    private boolean esContactoActivo(String idUsuario) {
        return idUsuario != null && idUsuario.equals(idContactoActual);
    }

    private void sumarNoLeido(String idUsuario, String idMensaje) {
        if (idUsuario == null || idUsuario.isBlank()) {
            return;
        }
        int actual = mensajesNoLeidos.getOrDefault(idUsuario, 0);
        mensajesNoLeidos.put(idUsuario, actual + 1);
        if (idMensaje != null && !idMensaje.isBlank()) {
            idsNoLeidosPorContacto.computeIfAbsent(idUsuario, k -> new ArrayList<>()).add(idMensaje);
        }
        refrescarCeldaContacto(idUsuario);
    }

    private void limpiarNoLeidosContacto(String idUsuario, boolean enviarConfirmacion) {
        if (idUsuario == null || idUsuario.isBlank()) {
            return;
        }

        List<String> pendientes = idsNoLeidosPorContacto.remove(idUsuario);
        mensajesNoLeidos.remove(idUsuario);
        refrescarCeldaContacto(idUsuario);

        if (!enviarConfirmacion || messageController == null || pendientes == null || pendientes.isEmpty()) {
            return;
        }

        for (String idMensaje : pendientes) {
            if (idMensaje == null || idMensaje.isBlank()) {
                continue;
            }
            try {
                messageController.confirmarLectura(idUsuario, idMensaje);
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
        }
    }

    private void refrescarCeldaContacto(String idUsuario) {
        int index = buscarIndiceContacto(idUsuario);
        if (index < 0) {
            return;
        }
        Contact contacto = contactModel.getElementAt(index);
        contactModel.set(index, contacto);
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

    private void enviarImagenChat() {
        if (messageController == null) {
            appendSistema("Controlador de mensajes no configurado.");
            return;
        }
        if (idContactoActual == null || idContactoActual.isBlank()) {
            appendSistema("Selecciona un contacto para enviar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona una imagen");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imagenes", "png", "jpg", "jpeg", "gif", "bmp", "webp"
        ));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = chooser.getSelectedFile();
        try {
            messageController.enviarImagen(MI_ID, idContactoActual, archivo);
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

    public String getMiId() {
        return MI_ID;
    }

    public String getMiNombre() {
        return miNombre;
    }

    public int pedirDecisionInvitacion(String nombre) {
        return JOptionPane.showConfirmDialog(
                this,
                "Invitacion de " + nombre + ".\nDeseas aceptar la conexion?",
                "Solicitud 001",
                JOptionPane.YES_NO_OPTION
        );
    }

    public int pedirDecisionHello(String nombre) {
        return JOptionPane.showConfirmDialog(
                this,
                "Conexion automatica de " + nombre + ".\nDeseas aceptar?",
                "Solicitud 004",
                JOptionPane.YES_NO_OPTION
        );
    }

    public Contact guardarOActualizarContactoDesdeEvento(String idUsuario, String nombre, String ip, boolean online) {
        return guardarOActualizarConControlador(idUsuario, nombre, ip, online);
    }

    public Contact obtenerContactoPorId(String idUsuario) {
        return buscarContactoPorId(idUsuario);
    }

    public String getIdContactoActual() {
        return idContactoActual;
    }

    public void activarContactoEnVista(String idUsuario, String nombre, String ip, boolean online) {
        idContactoActual = idUsuario;
        nombreContactoActual = (nombre == null || nombre.isBlank()) ? idUsuario : nombre;
        setContacto(nombreContactoActual);
        if (ip != null && !ip.isBlank()) {
            txtIp.setText(ip);
        }
        marcarContactoOnline(idUsuario, online);
        seleccionarContactoEnLista(idUsuario);
        setEstado(online ? "Conectado" : "Sin conexion", null);
    }

    public void mostrarEstadoRechazado() {
        setEstado("Rechazado", null);
    }

    public void limpiarContactoActivo() {
        idContactoActual = null;
        nombreContactoActual = "Sin contacto";
        setContacto(nombreContactoActual);
    }

    public void manejarMensajeEntrante(MensajeChat mensaje, String ipRemota) {
        Contact contactoActual = buscarContactoPorId(mensaje.getIdUser());
        String nombre = contactoActual != null ? contactoActual.getName() : mensaje.getIdUser();
        guardarOActualizarConControlador(mensaje.getIdUser(), nombre, ipRemota, true);
        marcarContactoOnline(mensaje.getIdUser(), true);
        if (esContactoActivo(mensaje.getIdUser()) && messageController != null) {
            try {
                messageController.recibirMensaje(mensaje);
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
            return;
        }

        sumarNoLeido(mensaje.getIdUser(), mensaje.getIdMensaje());
    }

    public void manejarImagenEntrante(ImagenChat imagen, String ipRemota) {
        Contact contactoActual = buscarContactoPorId(imagen.getIdUser());
        String nombre = contactoActual != null ? contactoActual.getName() : imagen.getIdUser();
        guardarOActualizarConControlador(imagen.getIdUser(), nombre, ipRemota, true);
        marcarContactoOnline(imagen.getIdUser(), true);
        if (esContactoActivo(imagen.getIdUser()) && messageController != null) {
            try {
                messageController.recibirImagen(imagen);
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
            return;
        }

        sumarNoLeido(imagen.getIdUser(), imagen.getIdMensaje());
    }

    public void manejarContactoCompartidoRecibido(EnviarContacto contacto, String ipFallback) {
        if (contacto == null) {
            return;
        }
        String ip = contacto.getIp();
        if (ip == null || ip.isBlank()) {
            ip = ipFallback;
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
    }

    public void manejarConfirmacionLectura(ConfirmacionLectura confirmacion) {
        if (messageController != null) {
            try {
                messageController.confirmarLecturaRecibida(confirmacion);
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
        }
    }

    public void manejarEliminacionMensaje(EliminarMensaje eliminarMensaje) {
        if (messageController != null) {
            try {
                messageController.recibirEliminacion(MI_ID, eliminarMensaje);
            } catch (OperationException e) {
                appendSistema(e.getMessage());
            }
        }
    }

    public void manejarContactoOffline(String idUsuario) {
        marcarContactoOnline(idUsuario, false);
        if (idUsuario != null && idUsuario.equals(idContactoActual)) {
            setEstado("Sin conexion", null);
            appendSistema("El contacto se ha desconectado (0018).");
            if (connectionController != null) {
                connectionController.cerrarConexionActual();
            }
            limpiarContactoActivo();
        }
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

    private void appendYoImagen(String idMensaje, String imagenBase64) {
        agregarBurbujaImagen(idMensaje, imagenBase64, true);
    }

    private void appendContacto(String texto) {
        agregarBurbuja(null, texto, false, false);
    }

    private void appendContacto(String idMensaje, String texto) {
        agregarBurbuja(idMensaje, texto, false, false);
    }

    private void appendContactoImagen(String idMensaje, String imagenBase64) {
        agregarBurbujaImagen(idMensaje, imagenBase64, false);
    }

    private void appendYoEliminado(String idMensaje) {
        agregarBurbuja(idMensaje, TEXTO_MENSAJE_ELIMINADO, true, true);
    }

    private void appendContactoEliminado(String idMensaje) {
        agregarBurbuja(idMensaje, TEXTO_MENSAJE_ELIMINADO, false, true);
    }

    private void setEstado(String texto, Color color) {
        Color finalColor = COLOR_TEXT_MUTED;
        if (texto != null) {
            String t = texto.toLowerCase();
            if (t.contains("conectado")) {
                finalColor = new Color(42, 119, 86);
            } else if (t.contains("esperando")) {
                finalColor = new Color(166, 115, 58);
            } else if (t.contains("rechazado")) {
                finalColor = new Color(177, 72, 72);
            }
        }
        lblEstado.setText("• " + texto);
        lblEstado.setForeground(finalColor);
    }

    private void setContacto(String nombre) {
        lblContacto.setText(nombre == null || nombre.isBlank() ? "Sin contacto" : nombre);
    }

    @Override
    public void onload(List<Contact> contactos) {
        SwingUtilities.invokeLater(() -> {
            contactModel.clear();
            mensajesNoLeidos.clear();
            idsNoLeidosPorContacto.clear();
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
    public void mostrarImagenPropia(String idMensaje, String imagenBase64) {
        SwingUtilities.invokeLater(() -> appendYoImagen(idMensaje, imagenBase64));
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
    public void mostrarImagenContacto(String idMensaje, String imagenBase64) {
        SwingUtilities.invokeLater(() -> appendContactoImagen(idMensaje, imagenBase64));
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
            etiqueta.setIcon(null);
            etiqueta.setText("<html><div style='width: 240px;'>" + TEXTO_MENSAJE_ELIMINADO + "</div></html>");
            etiqueta.setForeground(new Color(150, 150, 150));
            etiqueta.setBackground(new Color(246, 247, 246));
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
        JPanel fila = new JPanel(new FlowLayout(propia ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 8));
        fila.setOpaque(false);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        String textoAdaptado = escaparHtml(insertarCortesSuaves(texto));
        JLabel burbuja = new JLabel(
                "<html><div style='width: 420px;'>" + textoAdaptado + "</div></html>"
        );
        burbuja.setOpaque(true);
        burbuja.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        burbuja.setForeground(eliminado ? new Color(156, 162, 160) : (propia ? Color.WHITE : COLOR_TEXT_PRIMARY));
        burbuja.setBackground(eliminado ? new Color(246, 247, 246) : (propia ? COLOR_OWN_BUBBLE : COLOR_OTHER_BUBBLE));
        burbuja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(213, 220, 217)),
                new EmptyBorder(10, 14, 10, 14)
        ));
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
            indicador.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            indicador.setForeground(new Color(170, 222, 195));
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

    private void agregarBurbujaImagen(String idMensaje, String imagenBase64, boolean propia) {
        JPanel fila = new JPanel(new FlowLayout(propia ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 8));
        fila.setOpaque(false);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        JLabel burbuja = new JLabel();
        burbuja.setOpaque(true);
        burbuja.setBackground(propia ? COLOR_OWN_BUBBLE : COLOR_OTHER_BUBBLE);
        burbuja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(213, 220, 217)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        burbuja.setAlignmentX(propia ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        ImageIcon icono = crearIconoEscalado(imagenBase64);
        if (icono != null) {
            burbuja.setIcon(icono);
        } else {
            burbuja.setText("Imagen no disponible");
            burbuja.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            burbuja.setForeground(propia ? Color.WHITE : COLOR_TEXT_PRIMARY);
            burbuja.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(213, 220, 217)),
                    new EmptyBorder(10, 14, 10, 14)
            ));
        }

        contenido.add(burbuja);
        if (idMensaje != null && !idMensaje.isBlank()) {
            mensajesPorId.put(idMensaje, burbuja);
        }

        if (propia && idMensaje != null && !idMensaje.isBlank()) {
            JLabel indicador = new JLabel(" ");
            indicador.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            indicador.setForeground(new Color(170, 222, 195));
            indicador.setBorder(new EmptyBorder(2, 4, 0, 4));
            indicador.setAlignmentX(Component.RIGHT_ALIGNMENT);
            contenido.add(indicador);
            indicadoresLectura.put(idMensaje, indicador);

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

        fila.add(contenido);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));

        messagesContainer.add(fila);
        messagesContainer.revalidate();
        messagesContainer.repaint();
        desplazarChatAlFinal();
    }

    private ImageIcon crearIconoEscalado(String imagenBase64) {
        if (imagenBase64 == null || imagenBase64.isBlank()) {
            return null;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(imagenBase64);
            ImageIcon original = new ImageIcon(bytes);
            int width = original.getIconWidth();
            int height = original.getIconHeight();
            if (width <= 0 || height <= 0) {
                return null;
            }

            double scale = Math.min(
                    1.0,
                    Math.min(
                            (double) MAX_IMAGE_BUBBLE_SIZE / width,
                            (double) MAX_IMAGE_BUBBLE_SIZE / height
                    )
            );
            int newWidth = Math.max(1, (int) Math.round(width * scale));
            int newHeight = Math.max(1, (int) Math.round(height * scale));

            Image scaled = original.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
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

    private String insertarCortesSuaves(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }

        StringBuilder out = new StringBuilder(texto.length() + 32);
        int consecutivos = 0;

        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            out.append(c);

            if (Character.isWhitespace(c)) {
                consecutivos = 0;
                continue;
            }

            consecutivos++;
            if (consecutivos >= MAX_CHARS_CONTINUOS_BURBUJA) {
                // Salto real para evitar overflow horizontal si la palabra no tiene espacios
                out.append('\n');
                consecutivos = 0;
            }
        }

        return out.toString();
    }

    private static class DotPatternPanel extends JPanel {
        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(220, 225, 222));
            int step = 18;
            for (int y = 8; y < getHeight(); y += step) {
                for (int x = 8; x < getWidth(); x += step) {
                    g.fillRect(x, y, 1, 1);
                }
            }
        }
    }

    private static class ContactCellRenderer extends JPanel implements ListCellRenderer<Contact> {
        private final Map<String, Integer> mensajesNoLeidos;
        private final JLabel lblChip = new JLabel();
        private final JLabel lblNombre = new JLabel();
        private final JLabel lblSub = new JLabel();
        private final JLabel lblBadge = new JLabel();

        ContactCellRenderer(Map<String, Integer> mensajesNoLeidos) {
            this.mensajesNoLeidos = mensajesNoLeidos;
            setLayout(new BorderLayout(8, 2));
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setOpaque(true);

            lblChip.setOpaque(true);
            lblChip.setHorizontalAlignment(SwingConstants.CENTER);
            lblChip.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblChip.setPreferredSize(new Dimension(36, 24));
            lblChip.setBorder(BorderFactory.createLineBorder(new Color(170, 185, 178)));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblNombre.setForeground(COLOR_TEXT_PRIMARY);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSub.setForeground(COLOR_TEXT_MUTED);
            textPanel.add(lblNombre);
            textPanel.add(lblSub);

            lblBadge.setOpaque(true);
            lblBadge.setHorizontalAlignment(SwingConstants.CENTER);
            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblBadge.setForeground(Color.WHITE);
            lblBadge.setBackground(new Color(43, 112, 81));
            lblBadge.setBorder(new EmptyBorder(3, 7, 3, 7));

            add(lblChip, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
            add(lblBadge, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Contact> list,
                Contact value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            if (value == null) {
                lblChip.setText("");
                lblNombre.setText("");
                lblSub.setText("");
                return this;
            }

            boolean on = value.isStateConnect();
            lblChip.setText(on ? "On" : "Off");
            lblChip.setBackground(on ? new Color(188, 225, 203) : new Color(234, 236, 235));
            lblChip.setForeground(on ? new Color(39, 101, 74) : new Color(122, 132, 126));

            lblNombre.setText(value.getName());
            String sub = (value.getIp() == null || value.getIp().isBlank()) ? "Sin IP registrada" : value.getIp();
            lblSub.setText(sub);

            int noLeidos = mensajesNoLeidos.getOrDefault(value.getCode(), 0);
            if (noLeidos > 0) {
                lblBadge.setVisible(true);
                lblBadge.setText(noLeidos > 99 ? "99+" : String.valueOf(noLeidos));
            } else {
                lblBadge.setVisible(false);
                lblBadge.setText("");
            }

            if (isSelected) {
                setBackground(new Color(216, 230, 222));
            } else {
                setBackground(COLOR_PANEL);
            }
            return this;
        }
    }
}
