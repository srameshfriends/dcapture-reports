package dcapture.reports.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import dcapture.reports.jasper.JasperSource;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

public class ReportHref extends MessageResponse {
    @JsonProperty("href")
    @ToString.Include(name = "href")
    private String href;

    public static ResponseEntity<ReportHref> get(JasperSource jasperSource, String href) {
        ReportHref reportLink = new ReportHref();
        reportLink.setStatusCode(HttpStatus.OK.value());
        reportLink.setTimestamp(new Date());
        reportLink.setMessage(jasperSource.getReportName());
        reportLink.setDescription(jasperSource.getReportTitle());
        reportLink.setHref(href);
        return ResponseEntity.ok(reportLink);
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
