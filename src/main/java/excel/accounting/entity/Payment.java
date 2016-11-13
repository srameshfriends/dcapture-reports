package excel.accounting.entity;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Payment
 */
@Table(name = "payment")
public class Payment extends DocumentRecord {

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "data_code")
    private String dataCode;

    private String description;

    @JoinColumn(name = "currency", table = "currency")
    private String currency;

    @JoinColumn(name = "account", table = "account")
    private String account;

    private int instalment;

    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(name = "exchange_unit")
    private int exchangeUnit;

    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;

    private BigDecimal amount;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataCode() {
        return dataCode;
    }

    public void setDataCode(String dataCode) {
        this.dataCode = dataCode;
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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getInstalment() {
        return instalment;
    }

    public void setInstalment(int instalment) {
        this.instalment = instalment;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public int getExchangeUnit() {
        return exchangeUnit;
    }

    public void setExchangeUnit(int exchangeUnit) {
        this.exchangeUnit = exchangeUnit;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
