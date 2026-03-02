package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.view.IChatView;

import java.util.List;

public class ContactController {
    private final ContactDao contactDao;
    private final IChatView chatView;

    public ContactController(IChatView chatView) {
        this.chatView = chatView;
        this.contactDao = new ContactDao();
    }

    public void onload() {
        try {
            List<Contact> contactos = contactDao.findAll();
            for (Contact c : contactos) {
                c.setStateConnect(false);
            }
            chatView.onload(contactos);
        } catch (Exception e) {
            throw new OperationException("No se pudieron cargar contactos de la BD.", e);
        }
    }

    public Contact guardarOActualizarContacto(String idUsuario, String nombre, String ip, boolean online) {
        if (idUsuario == null || idUsuario.isBlank()) {
            return null;
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
            chatView.refrescarContacto(contacto);
            return contacto;
        } catch (Exception e) {
            throw new OperationException("No se pudo guardar el contacto en BD.", e);
        }
    }

    public boolean existeContactoPorCodigo(String idUsuario) {
        if (idUsuario == null || idUsuario.isBlank()) {
            return false;
        }
        try {
            return contactDao.existByCode(idUsuario);
        } catch (Exception e) {
            throw new OperationException("No se pudo validar el contacto en BD.", e);
        }
    }
}

