package excel.accounting.entity;

/**
 * Status
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public enum Status {
    Drafted("Drafted"), Verified("Verified"), Linked("Linked"), Closed("Closed");

    private String title;

    Status(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
