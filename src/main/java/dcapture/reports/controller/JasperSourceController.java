package dcapture.reports.controller;

import dcapture.reports.jasper.JasperSource;
import dcapture.reports.repository.JasperSourceRepository;
import dcapture.reports.util.LocaleMessage;
import dcapture.reports.util.MessageException;
import dcapture.reports.util.MessageResponse;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.StringReader;
import java.time.LocalDate;

@RestController
@RequestMapping(value = "/jasper", produces = MediaType.APPLICATION_JSON_VALUE)
public class JasperSourceController {
    private final JasperSourceRepository repository;
    private final LocaleMessage localeMessage;

    @Autowired
    public JasperSourceController(JasperSourceRepository jasperSourceRepository, LocaleMessage localeMessage) {
        this.repository = jasperSourceRepository;
        this.localeMessage = localeMessage;
    }

    @GetMapping(value = "/source/loadAll", produces = "application/json")
    public @ResponseBody Iterable<JasperSource> loadAll() {
        return repository.findAll();
    }

    @PostMapping("/source/add")
    public ModelAndView addSource(@RequestParam("report_name") String reportName,
                                  @RequestParam("jrxml") MultipartFile jrxml,
                                  @RequestParam("jasper") MultipartFile jasper) {
        if (reportName == null || reportName.isBlank()) {
            return new ModelAndView("redirect:/index.html?msg=" + localeMessage.getMessage("jasper.source.empty"));
        }
        repository.add(reportName, jrxml, jasper);
        return new ModelAndView("redirect:/index.html");
    }

    @PutMapping(value = "/source/update")
    public @ResponseBody ResponseEntity<MessageResponse> updateJasperSource(@RequestBody JasperSource source) {
        JasperSource target = isValidSource(source) ? repository.findByName(source.getReportName()) : null;
        if (target == null) {
            throw new MessageException("jasper.id.empty");
        }
        JsonObject jsonObject = getValidDataFormat(source.getDataFormat());
        if (jsonObject == null) {
            throw new MessageException("jasper.data_format.error", " JSON format ");
        }
        target.setUpdatedOn(LocalDate.now());
        target.setReportTitle(source.getReportTitle());
        target.setDataFormat(jsonObject.toString());
        repository.update(target);
        repository.initialize();
        return localeMessage.ok("jasper.updated", target.getReportTitle());
    }

    @DeleteMapping("/source/delete")
    public @ResponseBody ResponseEntity<MessageResponse> delete(@RequestParam("report_name") String reportName) {
        JasperSource source = reportName == null || reportName.isBlank() ? null : repository.findByName(reportName);
        if (source == null) {
            throw new MessageException("jasper.source.empty");
        }
        repository.delete(source);
        repository.initialize();
        return localeMessage.ok("jasper-report-list", "jasper.deleted", source.getReportTitle());
    }

    @GetMapping(value = "/reload")
    public @ResponseBody ResponseEntity<MessageResponse> reload() {
        this.repository.initialize();
        return MessageResponse.ok(localeMessage, "jasper.reloaded");
    }

    private boolean isValidSource(JasperSource source) {
        return source != null && source.getReportName() != null && !source.getReportName().isBlank();
    }

    private JsonObject getValidDataFormat(String dataFormatText) {
        dataFormatText = dataFormatText.replace("\n", "").trim();
        JsonObject jsonObject;
        try (JsonReader parser = Json.createReader(new StringReader(dataFormatText))) {
            jsonObject = parser.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new MessageException("jasper.data_format.error", ex.getMessage());
        }
        return jsonObject;
    }
}
