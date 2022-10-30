package dcapture.reports.jasper;

import com.arangodb.entity.Entity;
import com.arangodb.springframework.annotation.ArangoId;
import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.Field;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Document("jasper_source")
@Data
public class JasperSource implements Entity {
    @Id
    private String id;
    @ArangoId
    private String arangoId;

    @Field("report_name")
    @JsonProperty("report_name")
    @ToString.Include(name = "report_name")
    private String reportName;

    @Field("report_title")
    @JsonProperty("report_title")
    @ToString.Include(name = "report_title")
    private String reportTitle;

    @Field("data_format")
    @JsonProperty("data_format")
    @ToString.Include(name = "data_format")
    private String dataFormat;

    @Field("updated_on")
    @JsonProperty("updated_on")
    @ToString.Include(name = "updated_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate updatedOn;

    @Field("jasper_class_modified")

    @JsonProperty("jasper_class_modified")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @ToString.Include(name = "jasper_class_modified")
    private LocalDate jasperClassModified;

    public JasperSource() {
        setUpdatedOn(LocalDate.now());
    }

    public JasperSource(String reportName, String dataFormat) {
        this.reportName = reportName;
        this.dataFormat = dataFormat;
        setUpdatedOn(LocalDate.now());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public LocalDate getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDate updatedOn) {
        this.updatedOn = updatedOn;
    }

    public LocalDate getJasperClassModified() {
        return jasperClassModified;
    }

    public void setJasperClassModified(LocalDate jasperClassModified) {
        this.jasperClassModified = jasperClassModified;
    }

    public boolean isNew() {
        return id == null || id.isBlank();
    }
}
