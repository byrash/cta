package com.shivaji.api;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.ScriptingHandler;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
  private Map<String, Path> cocMap = new HashMap<>();
  private final Path basePath = Paths.get("./uploads/");

  @GetMapping(value = "/cta/{filledFormID}", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<Resource> getFilledFormForDisplay(
      @PathVariable("filledFormID") String filledFormID) throws IOException {
    Path renderedForm = filledMap.get(filledFormID);
    ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(renderedForm));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + "download.pdf");
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(resource.contentLength())
        .contentType(MediaType.APPLICATION_PDF)
        .body(resource);
  }

  @GetMapping(value = "/coc/{filledFormID}", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<Resource> getCocForDisplay(
      @PathVariable("filledFormID") String filledFormID) throws IOException {
    Path renderedForm = cocMap.get(filledFormID);
    ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(renderedForm));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + "download.pdf");
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(resource.contentLength())
        .contentType(MediaType.APPLICATION_PDF)
        .body(resource);
  }

  @PostMapping(value = "/signer/cta")
  public ResponseEntity<Boolean> cta(@RequestBody Map<String, String> req)
      throws InterruptedException {
    System.out.println(Calendar.getInstance().getTime() + "Started -->" + req);
    Thread.sleep((long) (Math.random() * 5000));
    System.out.println(Calendar.getInstance().getTime() + "Completed -->" + req);
    return ResponseEntity.ok(true);
  }

  @PostMapping(value = "/signer/reminder")
  public ResponseEntity<Boolean> reminder(@RequestBody Map<String, String> req) {
    System.out.println(Calendar.getInstance().getTime() + "" + req);
    return ResponseEntity.ok(true);
  }

  @PostMapping(value = "/recall/cta")
  public ResponseEntity<Boolean> recall(@RequestBody Map<String, String> req) {
    System.out.println(Calendar.getInstance().getTime() + "" + req);
    return ResponseEntity.ok(true);
  }

  @PostMapping(value = "/cta/{filledFormID}")
  public ResponseEntity<String> ctaSubmission(@PathVariable("filledFormID") String filledFormID)
      throws IOException {
    Path filledForm = filledMap.get(filledFormID);
    String cocPath = "./coc/" + filledForm.getFileName();
    try (PDDocument pdfDocument = Loader.loadPDF(new File(filledForm.toString()))) {
      PDPage page = new PDPage();
      pdfDocument.addPage(page);
      PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page);
      PDType1Font pdType1Font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);

      // Begin the Content stream
      contentStream.beginText();
      // Setting the font to the Content stream
      contentStream.setFont(pdType1Font, 12);
      // Setting the position for the line
      contentStream.newLineAtOffset(25, 100);
      String text = "Document ID " + filledFormID;
      // Adding text in the form of string
      contentStream.showText(text);
      // Ending the content stream
      contentStream.endText();

      // Begin the Content stream
      contentStream.beginText();
      // Setting the position for the line
      contentStream.newLineAtOffset(25, 150);
      text =
          "Signed On "
              + new SimpleDateFormat("MM.dd.yyyy").format(Calendar.getInstance().getTime());
      // Adding text in the form of string
      contentStream.showText(text);
      // Ending the content stream
      contentStream.endText();

      // Begin the Content stream
      contentStream.beginText();
      // Setting the position for the line
      contentStream.newLineAtOffset(25, 200);
      text = "Document Name " + filledForm.getFileName();
      // Adding text in the form of string
      contentStream.showText(text);
      // Ending the content stream
      contentStream.endText();

      // Begin the Content stream
      contentStream.beginText();
      // Setting the position for the line
      contentStream.newLineAtOffset(25, 250);
      text = "Location " + filledForm.toString();
      // Adding text in the form of string
      contentStream.showText(text);
      // Ending the content stream
      contentStream.endText();

      // Closing the content stream
      contentStream.close();
      pdfDocument.save(cocPath);
    }
    String uuid = generateUUID();
    cocMap.put(uuid, Path.of(cocPath));
    return ResponseEntity.ok(uuid);
  }

  @PostMapping(
      value = "/data/{templateId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<String> submitData(
      @PathVariable("templateId") String templateId,
      @RequestBody Map<String, String> data,
      HttpServletResponse response)
      throws IOException {
    if (templateMap.get(templateId) == null) {
      return ResponseEntity.badRequest().build();
    }
    Path renderedForm = fillForm(templateMap.get(templateId), data);
    String uuid = generateUUID();
    filledMap.put(uuid, renderedForm);
    return ResponseEntity.ok(uuid);
  }

  @ResponseBody
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Schema> upload(
      @RequestParam(name = "file", required = false) MultipartFile file) throws IOException {
    Path filePath = write(file, basePath);
    Map<String, PDField> PDFieldMap = loadFields(filePath);
    Map<String, Object> data = new HashMap<>();
    List<Component> components =
        PDFieldMap.entrySet().stream()
            .map(
                entry -> {
                  String key = entry.getKey();
                  PDField pdField = entry.getValue();
                  data.put(key, pdField.getValueAsString());
                  return Component.builder()
                      .key(key)
                      .label(key)
                      .type(getFieldType(pdField))
                      .validate(Validate.builder().required(false).build())
                      .build();
                })
            .collect(Collectors.toList());
    Schema s = Schema.builder().type("default").components(components).data(data).build();
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
              PDField field = acroForm.getField(key);
              if (field != null) {
                try {
                  field.setValue(getFormattedValue(field, value));
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
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
      }

      // Save and close the filled out form.
      //      pdfDocument.save("FillFormField.pdf");
    }
    return results;
  }

  public static String getFormattedValue(PDField field, String apValue) {
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

  public String getFieldType(PDField field) {
    switch (field.getClass().getSimpleName()) {
      case "PDTextField":
        return "textfield";
      case "PDCheckBox":
        return "checklist";
      case "PDRadioButton":
        return "radio";
      case "PDChoice":
        return "select";
      default:
        return "textfield";
    }
  }
}
