package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.ChatMessageRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {
    private static volatile boolean schemaChecked = false;

    public void save(ChatMessageRecord record) {
        if (record == null) {
            return;
        }

        try (Connection conn = ConnectionDB.getInstance().getConection()) {
            if (conn == null) {
                return;
            }
            ensureDeletedSchema(conn);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO chat_message(message_id, owner_id, contact_id, sender_id, contenido, eliminado) VALUES(?,?,?,?,?,?)"
            )) {
                ps.setString(1, record.getMessageId());
                ps.setString(2, record.getOwnerId());
                ps.setString(3, record.getContactId());
                ps.setString(4, record.getSenderId());
                ps.setString(5, record.getContenido());
                ps.setBoolean(6, false);
                ps.executeUpdate();
            } catch (Exception ignored) {
                // Fallback para esquemas antiguos sin columna eliminado
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO chat_message(message_id, owner_id, contact_id, sender_id, contenido) VALUES(?,?,?,?,?)"
                )) {
                    ps.setString(1, record.getMessageId());
                    ps.setString(2, record.getOwnerId());
                    ps.setString(3, record.getContactId());
                    ps.setString(4, record.getSenderId());
                    ps.setString(5, record.getContenido());
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo guardar mensaje: " + e.getMessage());
        }
    }

    public List<ChatMessageRecord> findByOwnerAndContact(String ownerId, String contactId) {
        List<ChatMessageRecord> lista = new ArrayList<>();

        try (Connection conn = ConnectionDB.getInstance().getConection()) {
            if (conn == null) {
                return lista;
            }
            ensureDeletedSchema(conn);
            lista.addAll(findWithDeleted(conn, ownerId, contactId));
            if (lista.isEmpty()) {
                lista.addAll(findWithoutDeleted(conn, ownerId, contactId));
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar historial: " + e.getMessage());
        }

        return lista;
    }

    public void markDeleted(String ownerId, String messageId) {
        if (ownerId == null || ownerId.isBlank() || messageId == null || messageId.isBlank()) {
            return;
        }

        try (Connection conn = ConnectionDB.getInstance().getConection()) {
            if (conn == null) {
                return;
            }
            ensureDeletedSchema(conn);

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE chat_message SET contenido = '', eliminado = 1 WHERE owner_id = ? AND message_id = ?"
            )) {
                ps.setString(1, ownerId);
                ps.setString(2, messageId);
                ps.executeUpdate();
            } catch (Exception ignored) {
                // Fallback si no existe columna eliminado
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE chat_message SET contenido = '' WHERE owner_id = ? AND message_id = ?"
                )) {
                    ps.setString(1, ownerId);
                    ps.setString(2, messageId);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo marcar mensaje eliminado: " + e.getMessage());
        }
    }

    private void ensureDeletedSchema(Connection conn) {
        if (schemaChecked || conn == null) {
            return;
        }
        synchronized (MessageDao.class) {
            if (schemaChecked) {
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "ALTER TABLE chat_message ADD COLUMN eliminado TINYINT(1) NOT NULL DEFAULT 0"
            )) {
                ps.executeUpdate();
            } catch (Exception ignored) {
            }
            schemaChecked = true;
        }
    }

    private List<ChatMessageRecord> findWithDeleted(Connection conn, String ownerId, String contactId) {
        List<ChatMessageRecord> lista = new ArrayList<>();
        String sql = "SELECT id, message_id, owner_id, contact_id, sender_id, contenido, eliminado, fecha_hora " +
                "FROM chat_message WHERE owner_id = ? AND contact_id = ? ORDER BY fecha_hora ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ownerId);
            ps.setString(2, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatMessageRecord r = new ChatMessageRecord();
                    r.setId(rs.getLong("id"));
                    r.setMessageId(rs.getString("message_id"));
                    r.setOwnerId(rs.getString("owner_id"));
                    r.setContactId(rs.getString("contact_id"));
                    r.setSenderId(rs.getString("sender_id"));
                    r.setContenido(rs.getString("contenido"));
                    r.setEliminado(rs.getBoolean("eliminado"));
                    r.setFechaHora(rs.getTimestamp("fecha_hora"));
                    lista.add(r);
                }
            }
        } catch (Exception ignored) {
        }

        return lista;
    }

    private List<ChatMessageRecord> findWithoutDeleted(Connection conn, String ownerId, String contactId) {
        List<ChatMessageRecord> lista = new ArrayList<>();
        String sql = "SELECT id, message_id, owner_id, contact_id, sender_id, contenido, fecha_hora " +
                "FROM chat_message WHERE owner_id = ? AND contact_id = ? ORDER BY fecha_hora ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ownerId);
            ps.setString(2, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatMessageRecord r = new ChatMessageRecord();
                    r.setId(rs.getLong("id"));
                    r.setMessageId(rs.getString("message_id"));
                    r.setOwnerId(rs.getString("owner_id"));
                    r.setContactId(rs.getString("contact_id"));
                    r.setSenderId(rs.getString("sender_id"));
                    r.setContenido(rs.getString("contenido"));
                    String contenido = r.getContenido();
                    r.setEliminado(contenido == null || contenido.isBlank());
                    r.setFechaHora(rs.getTimestamp("fecha_hora"));
                    lista.add(r);
                }
            }
        } catch (Exception ignored) {
        }

        return lista;
    }
}
