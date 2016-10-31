package excel.accounting.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Income Item
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class IncomeItem {
    private int id;
    private Date incomeDate;
    private String description, currency, incomeCategory, incomeAccount;;

    private BigDecimal amount;
    private Status status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getIncomeDate() {
        return incomeDate;
    }

    public void setIncomeDate(Date incomeDate) {
        this.incomeDate = incomeDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getIncomeCategory() {
        return incomeCategory;
    }

    public void setIncomeCategory(String incomeCategory) {
        this.incomeCategory = incomeCategory;
    }

    public String getIncomeAccount() {
        return incomeAccount;
    }

    public void setIncomeAccount(String incomeAccount) {
        this.incomeAccount = incomeAccount;
    }
}
