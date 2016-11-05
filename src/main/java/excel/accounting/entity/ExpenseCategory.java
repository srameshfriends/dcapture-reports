package excel.accounting.entity;

/**
 * Expense Category
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ExpenseCategory {
    private String code, name, chartOfAccounts, description;
    private Status status;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChartOfAccounts() {
        return chartOfAccounts;
    }

    public void setChartOfAccounts(String chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
