package excel.accounting.entity;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Expense Item
 *
 * @author Ramesh
 * @since Oct, 2016
 */
@Table(name = "expense_item")
public class ExpenseItem extends MasterRecord {

    @Column(name = "expense_date")
    private Date expenseDate;

    @Column(name = "group_code")
    private String groupCode;

    private String reference;

    private String description;

    @JoinColumn(name = "currency", table = "currency")
    private String currency;

    @Column(name = "expense_category")
    private String expenseCategory;

    @JoinColumn(name = "account", table = "account")
    private String account;

    private BigDecimal amount;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount;

    @Column(name = "paid_status")
    private PaidStatus paidStatus;

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public Date getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExpenseCategory() {
        return expenseCategory;
    }

    public void setExpenseCategory(String expenseCategory) {
        this.expenseCategory = expenseCategory;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public PaidStatus getPaidStatus() {
        return paidStatus;
    }

    public void setPaidStatus(PaidStatus paidStatus) {
        this.paidStatus = paidStatus;
    }
}
