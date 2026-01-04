package com.drivingschool.common.feign;

import com.drivingschool.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;

/**
 * AI Generated
 * Custom error decoder for Feign clients.
 * Converts HTTP errors from other services into appropriate exceptions.
 * 
 * <p>This decoder should be used in all services that use Feign clients
 * to ensure consistent error handling across the microservices' architecture.</p>
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        
        log.debug("Feign error for method {}: status={}", methodKey, status);
        
        // Handle 404 Not Found
        if (status == HttpStatus.NOT_FOUND) {
            String message = extractErrorMessage(response);
            if (message != null && !message.isEmpty()) {
                // Extract resource name and ID from message if possible
                // Format: "Instructor with id 22 not found" or "Resource with id X not found"
                if (message.contains("Instructor")) {
                    Long id = extractIdFromMessage(message);
                    return new ResourceNotFoundException("Instructor", id != null ? id : 0L);
                } else if (message.contains("Vehicle")) {
                    Long id = extractIdFromMessage(message);
                    return new ResourceNotFoundException("Vehicle", id != null ? id : 0L);
                } else if (message.contains("Student")) {
                    Long id = extractIdFromMessage(message);
                    return new ResourceNotFoundException("Student", id != null ? id : 0L);
                } else if (message.contains("Course")) {
                    Long id = extractIdFromMessage(message);
                    return new ResourceNotFoundException("Course", id != null ? id : 0L);
                } else if (message.contains("Lesson")) {
                    Long id = extractIdFromMessage(message);
                    return new ResourceNotFoundException("Lesson", id != null ? id : 0L);
                }
                return new ResourceNotFoundException(message);
            }
            return new ResourceNotFoundException("Resource not found");
        }
        
        // Handle 400 Bad Request
        if (status == HttpStatus.BAD_REQUEST) {
            String message = extractErrorMessage(response);
            return new com.drivingschool.common.exception.BusinessException(
                    message != null ? message : "Bad request", 
                    "BAD_REQUEST");
        }
        
        // For other errors, use default decoder
        return defaultErrorDecoder.decode(methodKey, response);
    }

    private String extractErrorMessage(Response response) {
        try {
            if (response.body() != null) {
                try (InputStream bodyStream = response.body().asInputStream()) {
                    byte[] bodyBytes = bodyStream.readAllBytes();
                    String bodyString = new String(bodyBytes);
                    
                    // Try to parse as JSON and extract message
                    try {
                        var jsonNode = objectMapper.readTree(bodyString);
                        if (jsonNode.has("message")) {
                            return jsonNode.get("message").asText();
                        }
                    } catch (Exception e) {
                        // If not JSON, return the raw body (truncated if too long)
                        return bodyString.length() > 200 ? bodyString.substring(0, 200) : bodyString;
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Failed to read error response body", e);
        }
        return null;
    }

    private Long extractIdFromMessage(String message) {
        try {
            // Try to extract ID from messages like "Instructor with id 22 not found"
            String[] parts = message.split("id ");
            if (parts.length > 1) {
                String idPart = parts[1].split(" ")[0];
                return Long.parseLong(idPart);
            }
        } catch (Exception e) {
            log.debug("Could not extract ID from message: {}", message);
        }
        return null;
    }
}

