package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.bl.message.AceptacionInvitacion;
import edu.upb.chatupb_v2.bl.message.Invitacion;
import edu.upb.chatupb_v2.bl.message.MensajeChat;
import edu.upb.chatupb_v2.bl.message.Offline;
import edu.upb.chatupb_v2.bl.message.RechazoConexion;
import edu.upb.chatupb_v2.bl.server.ChatEventListener;
import edu.upb.chatupb_v2.bl.server.ClientMediator;
import edu.upb.chatupb_v2.bl.server.SocketClient;
import edu.upb.chatupb_v2.repository.BlackListDao;
import edu.upb.chatupb_v2.repository.Contact;
import edu.upb.chatupb_v2.repository.ContactDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.UUID;

public class ChatUI extends JFrame implements ChatEventListener {
    private SocketClient client;
    private static final String MI_ID = "af3bc20a-766c-4cd4-813d-b1067a01fa9a";
    private final String miNombre = System.getProperty("user.name", "Alan");
    private final ContactDao contactDao = new ContactDao();
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
        cargarContactosIniciales();
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
        if (client == null) {
            appendSistema("No hay conexion activa.");
            return;
        }
        try {
            Offline offline = new Offline(MI_ID);
            client.send(offline.generarTrama());
        } catch (Exception e) {
            appendSistema("No se pudo enviar offline (0018).");
        } finally {
            try {
                client.close();
            } catch (Exception ignored) {
            }
            client = null;
            marcarContactoOnline(idContactoActual, false);
            if (idContactoActual != null) {
                ClientMediator.getInstance().removerCliente(idContactoActual);
            }
            idContactoActual = null;
            nombreContactoActual = "Sin contacto";
            setContacto(nombreContactoActual);
            setEstado("Desconectado", new Color(255, 230, 153));
            appendSistema("Te desconectaste (0018).");
        }
    }
//
    private void cargarContactosIniciales() {
        contactModel.clear();
        try {
            List<Contact> contactos = contactDao.findAll();
            for (Contact c : contactos) {
                c.setStateConnect(false);
                contactModel.addElement(c);
            }
            appendSistema("Contactos cargados: " + contactos.size());
        } catch (Exception e) {
            appendSistema("No se pudieron cargar contactos de la BD.");
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
    }

    private void guardarOActualizarContacto(String idUsuario, String nombre, String ip, boolean online) {
        if (idUsuario == null || idUsuario.isBlank()) {
            return;
        }

        try {
            Contact contacto = contactDao.findByCode(idUsuario);
            if (contacto == null) {
                contacto = new Contact();
                contacto.setCode(idUsuario);
                contacto.setName((nombre == null || nombre.isBlank()) ? idUsuario : nombre);
                contacto.setIp(ip);
                contactDao.save(contacto);
            } else {
                if (nombre != null && !nombre.isBlank()) {
                    contacto.setName(nombre);
                }
                contacto.setIp(ip);
                contactDao.update(contacto);
            }

            contacto.setStateConnect(online);
            upsertContactoEnLista(contacto);
        } catch (Exception e) {
            appendSistema("No se pudo guardar el contacto en BD.");
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
        try {
            String ip = txtIp.getText().trim();
            if (ip.isEmpty()) {
                ip = "127.0.0.1";
                txtIp.setText(ip);
            }

            client = new SocketClient(ip);
            client.start();

            Invitacion invitacion = new Invitacion(MI_ID, miNombre);
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
                MensajeChat mensajeChat = new MensajeChat(MI_ID, UUID.randomUUID().toString(), texto);
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
        BlackListDao blackListDao = new BlackListDao();
        String ipRemota = sender.getIp();
        guardarOActualizarContacto(inv.getIdUsuario(), inv.getNombre(), ipRemota, false);

        if (blackListDao.isBlacklisted(ipRemota)) {
            try {
                RechazoConexion rechazo = new RechazoConexion();
                sender.send(rechazo.generarTrama());
                sender.close();
                SwingUtilities.invokeLater(() -> appendSistema("Solicitud rechazada (IP en lista negra)."));
            } catch (Exception e) {
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
                    AceptacionInvitacion aceptacion = new AceptacionInvitacion(MI_ID, miNombre);
                    sender.send(aceptacion.generarTrama());
                    client = sender;
                    idContactoActual = inv.getIdUsuario();
                    nombreContactoActual = inv.getNombre();
                    setEstado("Conectado", new Color(187, 247, 208));
                    setContacto(nombreContactoActual);
                    guardarOActualizarContacto(inv.getIdUsuario(), inv.getNombre(), sender.getIp(), true);
                    marcarContactoOnline(inv.getIdUsuario(), true);
                    seleccionarContactoEnLista(inv.getIdUsuario());
                    appendSistema("Conexion aceptada. Ya pueden chatear.");
                } else {
                    RechazoConexion rechazo = new RechazoConexion();
                    sender.send(rechazo.generarTrama());

                    BlackListDao blackListDao2 = new BlackListDao();

                    blackListDao2.addToBlacklist(sender.getIp());

                    appendSistema("Conexion rechazada y agregada a lista negra.");
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
            guardarOActualizarContacto(acc.getIdUsuario(), acc.getNombre(), sender.getIp(), true);
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
    public void onMensajeRecibido(MensajeChat mensaje, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            Contact contactoActual = buscarContactoPorId(mensaje.getIdUser());
            String nombre = contactoActual != null ? contactoActual.getName() : mensaje.getIdUser();
            guardarOActualizarContacto(mensaje.getIdUser(), nombre, sender.getIp(), true);
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
            appendContacto(mensaje.getMensaje());
        });
    }

    @Override
    public void onClienteOffline(String idUsuario, SocketClient sender) {
        SwingUtilities.invokeLater(() -> {
            marcarContactoOnline(idUsuario, false);
            if (idUsuario != null && idUsuario.equals(idContactoActual)) {
                setEstado("Desconectado", new Color(255, 230, 153));
                appendSistema("El contacto se ha desconectado (0018).");
                if (client != null) {
                    client.close();
                    client = null;
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
