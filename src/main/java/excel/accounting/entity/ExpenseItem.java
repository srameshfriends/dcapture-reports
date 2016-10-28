package excel.accounting.entity;

import excel.accounting.model.EntityRow;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Expense Item
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ExpenseItem extends EntityRow {
    private Date expenseDate;
    private String referenceNumber, description, currency;
    private BigDecimal amount;
    private Status status;

    public Date getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
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
