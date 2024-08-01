package com.shivaji.api;

import java.util.Arrays;

import java.util.List;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUpload {

  public class FileUploadDto {
    private int id;
    private MultipartFile[] file;
    private List<NestedObj> nestedObjs;

    public static class NestedObj {
      private String key;
      private String value;

      public String getKey() {
        return key;
      }

      public void setKey(String key) {
        this.key = key;
      }

      public String getValue() {
        return value;
      }

      public void setValue(String value) {
        this.value = value;
      }
    }

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

    public List<NestedObj> getNestedObjs() {
      return nestedObjs;
    }

    public void setNestedObjs(List<NestedObj> nestedObjs) {
      this.nestedObjs = nestedObjs;
    }
  }

  @PostMapping("/upload")
  public void upload(@ModelAttribute FileUploadDto dto) {
    System.out.println(dto.id);
    if (dto.file == null) return;
    Arrays.stream(dto.file)
        .forEach(
            multipartFile -> {
              System.out.println(
                  multipartFile.getName()
                      + " "
                      + multipartFile.getOriginalFilename()
                      + " "
                      + multipartFile.getContentType());
            });
    if (!CollectionUtils.isEmpty(dto.nestedObjs)) {
      dto.nestedObjs.forEach(
          nestedObj -> System.out.println(nestedObj.getKey() + " " + nestedObj.getValue()));
    }
  }
}
