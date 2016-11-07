

package excel.accounting.entity;

/**
 * Status
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public enum PaidStatus {
    Unpaid("Unpaid"), FullyPaid("Fully Paid"), PartiallyPaid("Partially Paid");

    private String title;

    PaidStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
