package excel.accounting.entity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * System Setting
 *
 * @author Ramesh
 * @since Nov, 2016
 */
@Table(name = "system_setting")
public class SystemSetting extends DocumentRecord {

    @Column(name = "group_code")
    private String groupCode;

    private String name;

    @Column(name = "text_value")
    private String textValue;

    @Column(name = "decimal_value")
    private BigDecimal decimalValue;

    @Column(name = "date_value")
    private Date dateValue;

    @Column(name = "bool_value")
    private Boolean boolValue;

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Boolean getBoolValue() {
        return boolValue;
    }

    public void setBoolValue(Boolean boolValue) {
        this.boolValue = boolValue;
    }
}
