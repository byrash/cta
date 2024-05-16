package com.shivaji.api;

import java.util.Arrays;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUpload {

  public class FileUploadDto {
    private int id;
    private MultipartFile[] file;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public MultipartFile[] getFile() {
      return file;
    }

    public void setFile(MultipartFile[] file) {
      this.file = file;
    }
  }

  @PostMapping("/upload")
  public void upload(@ModelAttribute FileUploadDto dto) {
    System.out.println(dto.id);
    if (dto.file == null)
      return;
    Arrays.stream(dto.file)
        .forEach(
            multipartFile -> {
              System.out.println(multipartFile.getName());
              System.out.println(multipartFile.getOriginalFilename());
              System.out.println(multipartFile.getContentType());
            });
  }

}
