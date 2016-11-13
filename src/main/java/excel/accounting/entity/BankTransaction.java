package excel.accounting.entity;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Bank Transaction
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class BankTransaction extends DocumentRecord {
    @Column(name = "transaction_date")
    private Date transactionDate;

    private String bank;

    @Column(name = "transaction_code")
    private String transactionCode;

    private String description;

    @JoinColumn(name = "currency", table = "currency")
    private String currency;

    @Column(name = "credit_amount")
    private BigDecimal creditAmount;

    @Column(name = "debit_amount")
    private BigDecimal debitAmount;

    @Column(name = "transaction_index")
    private int transactionIndex;

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

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

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public int getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(int transactionIndex) {
        this.transactionIndex = transactionIndex;
    }
}
