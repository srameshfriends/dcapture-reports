package excel.accounting.entity;

import excel.accounting.db.ColumnIndex;

import javax.persistence.Column;

/**
 * Document Record
 */
@ColumnIndex(columns = {"status"})
public abstract class DocumentRecord extends BaseRecord {
    @Column(name = "status", length = 16)
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
