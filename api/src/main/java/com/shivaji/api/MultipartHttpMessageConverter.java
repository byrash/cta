package com.shivaji.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

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
    Charset charset = outputMessage.getHeaders().getContentType().getCharset();
    StringBuilder mainHeadersBuilder = new StringBuilder();
    mainHeadersBuilder
        .append("Content-Type: ")
        .append("multipart/mixed")
        .append("; boundary=")
        .append(boundary)
        .append("\r\n");
    StreamUtils.copy(mainHeadersBuilder.toString().getBytes(charset), outputMessage.getBody());

    // Write each part
    for (Part part : multipart.parts) {
      StringBuilder headersBuilder = new StringBuilder();
      String boundaryMarker = "\r\n--" + boundary + "\r\n";
      StreamUtils.copy(boundaryMarker.getBytes(charset), outputMessage.getBody());
      // Write part headers
      headersBuilder.append("Content-Type: ").append(part.contentType).append("\r\n");
      if (part.filename != null) {
        headersBuilder
            .append("Content-Disposition: attachment; filename=\"")
            .append(part.filename)
            .append("\"\r\n");
      }
      headersBuilder.append("\r\n");
      StreamUtils.copy(headersBuilder.toString().getBytes(charset), outputMessage.getBody());

      // Write part content
      if (part.content != null) {
        StreamUtils.copy(part.content.getBytes(charset), outputMessage.getBody());
      } else if (part.fileContent != null) {
        StreamUtils.copy(part.fileContent, outputMessage.getBody());
      }
    }
    // Final Boundary Marker
    String finalBoundaryMarker = "\r\n--" + boundary + "--\r\n";
    StreamUtils.copy(finalBoundaryMarker.getBytes(charset), outputMessage.getBody());
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
