package org.example.server.db;

import org.example.protocol.Message;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static SessionFactory sessionFactory;

    public static void init() {
        if (sessionFactory == null) {
            try {
                sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
                System.out.println("Hibernate and SQLite initialized successfully.");
            } catch (Exception e) {
                System.err.println("Initial SessionFactory creation failed." + e);
            }
        }
    }

    public static void saveMessage(Message msg) {
        if (sessionFactory == null) return;
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            MessageEntity entity = new MessageEntity();
            entity.setType(msg.getType());
            entity.setSender(msg.getSender());
            entity.setReceiver(msg.getReceiver());
            entity.setContent(msg.getContent());
            entity.setTimestamp(msg.getTimestamp());
            entity.setReplyToId(msg.getReplyToId());
            entity.setReplyToContent(msg.getReplyToContent());
            entity.setFileName(msg.getFileName());
            entity.setFileDataBase64(msg.getFileDataBase64());
            
            session.persist(entity);
            session.getTransaction().commit();
            msg.setId(entity.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteMessageById(Long id) {
        if (sessionFactory == null || id == null) return;
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            MessageEntity entity = session.get(MessageEntity.class, id);
            if (entity != null) {
                session.remove(entity);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Message> getAllMessages() {
        if (sessionFactory == null) return List.of();
        try (Session session = sessionFactory.openSession()) {
            List<MessageEntity> entities = session.createQuery("from MessageEntity order by timestamp asc", MessageEntity.class).list();
            return entities.stream().map(e -> {
                Message m = new Message();
                m.setId(e.getId());
                m.setType(e.getType());
                m.setSender(e.getSender());
                m.setReceiver(e.getReceiver());
                m.setContent(e.getContent());
                m.setTimestamp(e.getTimestamp());
                m.setReplyToId(e.getReplyToId());
                m.setReplyToContent(e.getReplyToContent());
                m.setFileName(e.getFileName());
                m.setFileDataBase64(e.getFileDataBase64());
                return m;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
