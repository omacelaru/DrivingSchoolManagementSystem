package com.drivingschool.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class ObjectMapperDeserializer implements Deserializer<Object> {
    
    private final ObjectMapper objectMapper;

    public ObjectMapperDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, Object.class);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing JSON to object", e);
        }
    }
}

