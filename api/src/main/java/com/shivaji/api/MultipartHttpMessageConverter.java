package com.shivaji.api;

import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.nio.charset.Charset;

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
  public Multipart read(
      Class<? extends Multipart> clazz, org.springframework.http.HttpInputMessage inputMessage)
      throws IOException {
    throw new UnsupportedOperationException("Reading multipart messages is not supported.");
  }

  @Override
  public void write(Multipart multipart, MediaType contentType, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    String boundary = UUID.randomUUID().toString(); // Generate a unique boundary

    // Write boundary marker at the beginning
    String boundaryMarker = "--" + boundary + "\r\n";
    MediaType outContentType = outputMessage.getHeaders().getContentType();
    if (outContentType != null && outContentType.getCharset() != null) {
      StreamUtils.copy(
          boundaryMarker.getBytes(outContentType.getCharset()), outputMessage.getBody());
    } else {
      throw new IllegalArgumentException("Charset not specified in Content-Type header");
    }

    // Write each part
    for (Part part : multipart.parts) {
      // Write part headers
      StringBuilder headersBuilder = new StringBuilder();
      headersBuilder.append("Content-Type: ").append(part.contentType).append("\r\n");
      if (part.filename != null) {
        headersBuilder
            .append("Content-Disposition: attachment; filename=\"")
            .append(part.filename)
            .append("\"\r\n");
      }
      headersBuilder.append("\r\n");
      StreamUtils.copy(
          headersBuilder
              .toString()
              .getBytes(outputMessage.getHeaders().getContentType().getCharset()),
          outputMessage.getBody());

      // Write part content
      if (part.content != null) {
        StreamUtils.copy(
            part.content.getBytes(outputMessage.getHeaders().getContentType().getCharset()),
            outputMessage.getBody());
      } else if (part.fileContent != null) {
        StreamUtils.copy(part.fileContent, outputMessage.getBody());
      }

      // Write boundary marker between parts
      StreamUtils.copy(
          boundaryMarker.getBytes(outputMessage.getHeaders().getContentType().getCharset()),
          outputMessage.getBody());
    }

    // Write final boundary marker
    String finalBoundaryMarker = "--" + boundary + "--\r\n";
    StreamUtils.copy(
        finalBoundaryMarker.getBytes(outputMessage.getHeaders().getContentType().getCharset()),
        outputMessage.getBody());
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
