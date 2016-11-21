package excel.accounting.entity;

import excel.accounting.db.ColumnIndex;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * Base Record
 */
@ColumnIndex(columns = {"code"})
abstract class BaseRecord {
    @Id
    @Column(name = "code", length = 8)
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
