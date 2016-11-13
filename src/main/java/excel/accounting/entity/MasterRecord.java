package excel.accounting.entity;

import excel.accounting.db.ColumnIndex;

import javax.persistence.Column;

/**
 * Master Record
 */
@ColumnIndex(columns = {"dataType", "status", "name"})
public class MasterRecord extends BaseRecord {
    @Column(name = "data_type", length = 16)
    private String dataType;

    @Column(name = "status", length = 16)
    private Status status;

    @Column(name = "name")
    private String name;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
