package com.shivaji.api;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.pdmodel.interactive.form.ScriptingHandler;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiHandler {

  private Map<String, Path> templateMap = new HashMap<>();
  private Map<String, Path> filledMap = new HashMap<>();
  private final Path basePath = Paths.get("./uploads/");

  @PostMapping(
      value = "/data/{templateId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<Resource> submitData(
      @PathVariable("templateId") String templateId,
      @RequestBody Map<String, String> data,
      HttpServletResponse response)
      throws IOException {
    if (templateMap.get(templateId) == null) {
      return ResponseEntity.badRequest().build();
    }
    Path renderedForm = fillForm(templateMap.get(templateId), data);
    ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(renderedForm));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add(
        HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + "download.pdf");
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(resource.contentLength())
        .contentType(MediaType.APPLICATION_PDF)
        .body(resource);
  }

  @ResponseBody
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Schema> upload(
      @RequestParam(name = "file", required = false) MultipartFile file) throws IOException {
    Path filePath = write(file, basePath);
    Map<String, PDField> PDFieldMap = loadFields(filePath);
    List<Component> components =
        PDFieldMap.entrySet().stream()
            .map(
                entry -> {
                  String key = entry.getKey();
                  return Component.builder()
                      .key(key)
                      .label(key)
                      .type("textfield")
                      .validate(Validate.builder().required(false).build())
                      .build();
                })
            .collect(Collectors.toList());
    Schema s = Schema.builder().type("default").components(components).build();
    String uuid = generateUUID();
    s.setTemplateId(uuid);
    templateMap.put(uuid, filePath);
    return ResponseEntity.ok(s);
  }

  public Path write(MultipartFile file, Path dir) throws IOException {
    Path filepath = Paths.get(dir.toString(), file.getOriginalFilename());
    try (OutputStream os = Files.newOutputStream(filepath)) {
      os.write(file.getBytes());
    }
    return filepath;
  }

  public Path fillForm(Path formTemplate, Map<String, String> data) throws IOException {
    try (PDDocument pdfDocument = Loader.loadPDF(new File(formTemplate.toString()))) {
      // get the document catalog
      PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();
      // as there might not be an AcroForm entry a null check is necessary
      if (acroForm != null) {
        data.forEach(
            (key, value) -> {
              PDTextField field = (PDTextField) acroForm.getField(key);
              try {
                field.setValue(value);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
      }
      pdfDocument.save("./filled/" + formTemplate.getFileName());
    }
    return Path.of("./filled/" + formTemplate.getFileName());
  }

  public Map<String, PDField> loadFields(Path formTemplate) throws IOException {
    Map<String, PDField> results = new LinkedHashMap<>();
    try (PDDocument pdfDocument = Loader.loadPDF(new File(formTemplate.toString()))) {
      // get the document catalog
      PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();
      // as there might not be an AcroForm entry a null check is necessary
      if (acroForm != null) {
        List<PDField> fields = acroForm.getFields();
        fields.forEach(
            pdField -> {
              if (!pdField.getFieldType().equalsIgnoreCase("Btn")) {
                results.put(pdField.getPartialName(), pdField);
              }
            });

        // Retrieve an individual field and set its value.
        //        PDTextField field = (PDTextField) acroForm.getField("DayPhone");
        //        field.setValue("7033889277");

        // If a field is nested within the form tree a fully qualified name
        // might be provided to access the field.
        //        field = (PDTextField) acroForm.getField("AccountNumber");
        //        field.setValue(getFormattedValue(field, "123456789"));

        //        field = (PDTextField) acroForm.getField("Owner_Name");
        //        field.setValue(getFormattedValue(field, "Shivaji Byrapaneni"));
      }

      // Save and close the filled out form.
      //      pdfDocument.save("FillFormField.pdf");
    }
    return results;
  }

  public static String getFormattedValue(PDTextField field, String apValue) {
    // format the field value for the appearance if there is scripting support and the field
    // has a format event
    PDFormFieldAdditionalActions actions = field.getActions();
    if (actions == null) {
      return apValue;
    }
    PDAction actionF = actions.getF();
    if (actionF != null) {
      if (field.getAcroForm().getScriptingHandler() != null) {
        ScriptingHandler scriptingHandler = field.getAcroForm().getScriptingHandler();
        return scriptingHandler.format((PDActionJavaScript) actionF, apValue);
      } else {
        System.out.println(
            "Field contains a formatting action but no ScriptingHandler "
                + "has been supplied - formatted value might be incorrect");
      }
    }
    return apValue;
  }

  public String generateUUID() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
