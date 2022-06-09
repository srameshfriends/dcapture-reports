package dcapture.reports.jasper;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReportModel {
    @JsonProperty("report_name")
    private String reportName;
    @JsonProperty("data_format")
    private String dataFormat;

    public ReportModel() {
    }

    public ReportModel(String reportName, String dataFormat) {
        this.reportName = reportName;
        this.dataFormat = dataFormat;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }
}
