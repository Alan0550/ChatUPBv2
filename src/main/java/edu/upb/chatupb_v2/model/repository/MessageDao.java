package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.ChatMessageRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {

    public void save(ChatMessageRecord record) {
        if (record == null) {
            return;
        }

        String sql = "INSERT INTO chat_message(message_id, owner_id, contact_id, sender_id, contenido) VALUES(?,?,?,?,?)";
        try (Connection conn = ConnectionDB.getInstance().getConection()) {
            if (conn == null) {
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, record.getMessageId());
                ps.setString(2, record.getOwnerId());
                ps.setString(3, record.getContactId());
                ps.setString(4, record.getSenderId());
                ps.setString(5, record.getContenido());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("No se pudo guardar mensaje: " + e.getMessage());
        }
    }

    public List<ChatMessageRecord> findByOwnerAndContact(String ownerId, String contactId) {
        List<ChatMessageRecord> lista = new ArrayList<>();
        String sql = "SELECT id, message_id, owner_id, contact_id, sender_id, contenido, fecha_hora " +
                "FROM chat_message WHERE owner_id = ? AND contact_id = ? ORDER BY fecha_hora ASC";

        try (Connection conn = ConnectionDB.getInstance().getConection()) {
            if (conn == null) {
                return lista;
            }
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
                        r.setFechaHora(rs.getTimestamp("fecha_hora"));
                        lista.add(r);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar historial: " + e.getMessage());
        }

        return lista;
    }
}
