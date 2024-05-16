package com.shivaji.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import java.io.ByteArrayInputStream;

class MultipartHttpMessageConverter implements HttpMessageConverter<Multipart> {

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return Multipart.class.isAssignableFrom(clazz);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(MediaType.MULTIPART_MIXED);
    }

    @Override
    public Multipart read(Class<? extends Multipart> clazz, org.springframework.http.HttpInputMessage inputMessage)
            throws IOException {
        throw new UnsupportedOperationException("Reading multipart messages is not supported.");
    }

    @Override
    public void write(Multipart multipart, MediaType contentType,
            org.springframework.http.HttpOutputMessage outputMessage) throws IOException {
        // Set headers
        HttpHeaders headers = outputMessage.getHeaders();
        headers.setContentType(MediaType.MULTIPART_MIXED);

        // Write each part
        for (Part part : multipart.parts) {
            // Set content type
            MediaType partMediaType = MediaType.parseMediaType(part.contentType);
            if (partMediaType == null) {
                partMediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
            headers.setContentType(partMediaType);
            // Set filename if available
            if (part.filename != null) {
                headers.setContentDispositionFormData("file", part.filename);
            }
            // Write content
            if (part.content != null) {
                outputMessage.getBody().write(part.content.getBytes());
            } else if (part.fileContent != null) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(part.fileContent);
                StreamUtils.copy(inputStream, outputMessage.getBody());
            }
            outputMessage.getBody().write("\r\n\r\n".getBytes()); // End of part marker
        }
    }

}

class Multipart {
    public Iterable<Part> parts;
}

class Part {
    public String contentType;
    public String content;
    public String filename;
    public byte[] fileContent;

    public Part(String contentType, String content) {
        this.contentType = contentType;
        this.content = content;
    }

    public Part(String contentType, String filename, byte[] fileContent) {
        this.contentType = contentType;
        this.filename = filename;
        this.fileContent = fileContent;
    }
}