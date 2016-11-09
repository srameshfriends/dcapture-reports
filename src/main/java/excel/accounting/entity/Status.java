package excel.accounting.entity;

/**
 * Status
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public enum Status {
    Drafted("Drafted"), Confirmed("Confirmed"), Closed("Closed");

    private String title;

    Status(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static Status[] getConfirmed() {
        return new Status[]{Status.Confirmed};
    }
}
