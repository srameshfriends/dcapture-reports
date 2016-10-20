package excel.accounting.model;

/**
 * Entity Row
 */
public abstract class EntityRow {
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isNew() {
        return getId() < 1;
    }
}
