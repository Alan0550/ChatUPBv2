package edu.upb.chatupb_v2.model.entities;

import java.sql.Timestamp;

public class ChatMessageRecord {

    private long id;
    private String messageId;
    private String ownerId;
    private String contactId;
    private String senderId;
    private String contenido;
    private boolean eliminado;
    private Timestamp fechaHora;

    public ChatMessageRecord() {
    }

    public ChatMessageRecord(
            String messageId,
            String ownerId,
            String contactId,
            String senderId,
            String contenido
    ) {
        this.messageId = messageId;
        this.ownerId = ownerId;
        this.contactId = contactId;
        this.senderId = senderId;
        this.contenido = contenido;
        this.eliminado = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public Timestamp getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Timestamp fechaHora) {
        this.fechaHora = fechaHora;
    }
}
