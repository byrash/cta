package com.shivaji.api;

import java.io.IOException;
import java.util.Arrays;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileDownloadWithPaylod {

  @GetMapping(value = "/download", produces = MediaType.MULTIPART_MIXED_VALUE)
  public ResponseEntity<?> getMultipartMixedResponse() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_MIXED);

    Multipart mixed = new Multipart();
    mixed.parts =
        Arrays.asList(
            new Part("text/plain", "Hello, this is part 1!"),
            new Part("application/json", "{\"key\": \"value\"}"),
            new Part(
                "application/octet-stream",
                "example.txt",
                "This is a text file content.".getBytes()),
            new Part("application/pdf", "dictionary.pdf", loadPDFFile("dictionary.pdf")));

    return new ResponseEntity<>(mixed, headers, HttpStatus.OK);
  }

  //  private byte[] loadPDFFile(String filename) {
  //    Path path = Paths.get(filename);
  //    try {
  //      return Files.readAllBytes(path);
  //    } catch (IOException e) {
  //      e.printStackTrace();
  //      return null;
  //    }
  //  }

  private byte[] loadPDFFile(String filename) {
    try {
      Resource resource = new ClassPathResource(filename);
      return StreamUtils.copyToByteArray(resource.getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
