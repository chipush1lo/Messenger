package org.example.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class XmlProtocolHelper {
    private static final XmlMapper xmlMapper;

    static {
        xmlMapper = new XmlMapper();
        
        // Increase max string length for large Base64 file transfers (20MB max string)
        xmlMapper.getFactory().setStreamReadConstraints(
            com.fasterxml.jackson.core.StreamReadConstraints.builder()
                .maxStringLength(20_000_000)
                .build()
        );
        
        // Register JavaTimeModule to properly handle LocalDateTime
        xmlMapper.registerModule(new JavaTimeModule());
        // Disable writing dates as timestamps to keep them readable in XML
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static String toXml(Message message) throws JsonProcessingException {
        return xmlMapper.writeValueAsString(message);
    }

    public static Message fromXml(String xml) throws JsonProcessingException {
        return xmlMapper.readValue(xml, Message.class);
    }
}
