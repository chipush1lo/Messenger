package org.example;

import org.example.protocol.Message;
import org.example.protocol.MessageType;
import org.example.protocol.XmlProtocolHelper;

import java.util.Base64;

public class TestXml {
    public static void main(String[] args) throws Exception {
        Message msg = new Message(MessageType.CHAT, "A", "B", "");
        byte[] large = new byte[5 * 1024 * 1024]; // 5MB
        msg.setFileDataBase64(Base64.getEncoder().encodeToString(large));
        
        System.out.println("Serializing...");
        String xml = XmlProtocolHelper.toXml(msg);
        System.out.println("XML length: " + xml.length());
        System.out.println("Contains newline? " + xml.contains("\n"));
        
        System.out.println("Deserializing...");
        Message out = XmlProtocolHelper.fromXml(xml);
        System.out.println("Deserialized base64 length: " + out.getFileDataBase64().length());
    }
}
