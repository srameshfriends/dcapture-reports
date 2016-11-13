package excel.accounting.entity;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Income Item
 *
 * @author Ramesh
 * @since Oct, 2016
 */
@Table(name = "income_item")
public class IncomeItem extends DocumentRecord {

    @Column(name = "income_date")
    private Date incomeDate;

    private String description;

    @JoinColumn(name = "currency", table = "currency")
    private String currency;

    @JoinColumn(name = "income_category", table = "income_category")
    private String incomeCategory;

    @JoinColumn(name = "account", table = "account")
    private String account;

    private BigDecimal amount;

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

    public String getIncomeCategory() {
        return incomeCategory;
    }

    public void setIncomeCategory(String incomeCategory) {
        this.incomeCategory = incomeCategory;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
