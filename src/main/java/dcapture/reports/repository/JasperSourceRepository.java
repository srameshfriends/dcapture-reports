package dcapture.reports.repository;

import dcapture.reports.jasper.JRTypeMap;
import dcapture.reports.jasper.JasperSource;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

@Repository
public interface JasperSourceRepository {
    void initialize();

    Iterable<JasperSource> findAll();

    JasperSource findByName(String reportName);

    JasperSource add(String reportName, MultipartFile jrXml, MultipartFile jasper);

    JasperSource update(JasperSource source);

    void delete(JasperSource source);

    JasperReport getJasperReport(String reportName);

    JRTypeMap getJasperTypeMap(JasperSource jasperSource);

    JasperSource insert(JasperSource jasperSource);
}
