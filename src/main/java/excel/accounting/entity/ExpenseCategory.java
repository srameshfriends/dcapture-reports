package excel.accounting.entity;

import javax.persistence.JoinColumn;
import javax.persistence.Table;

/**
 * Expense Category
 *
 * @author Ramesh
 * @since Oct, 2016
 */
@Table(name = "expense_category")
public class ExpenseCategory extends MasterRecord {

    private String description;

    @JoinColumn(name = "currency", table = "currency")
    private String currency;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
