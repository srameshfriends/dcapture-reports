package excel.accounting.entity;

import excel.accounting.model.EntityRow;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Income Item
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class IncomeItem extends EntityRow {
    private Date incomeDate;
    private String description, currency;
    private BigDecimal amount;
    private Status status;

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
}