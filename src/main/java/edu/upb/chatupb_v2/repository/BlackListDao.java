package edu.upb.chatupb_v2.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BlackListDao {

    public boolean isBlacklisted(String ip) {
        String sql = "SELECT COUNT(*) FROM lista_negra WHERE ip = ?";

        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ip);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void addToBlacklist(String ip) {
        String sql = "INSERT INTO lista_negra(ip) VALUES(?)";

        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ip);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}