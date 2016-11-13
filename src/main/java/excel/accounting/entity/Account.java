package excel.accounting.entity;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * Account
 *
 * @author Ramesh
 * @since Oct, 2016
 */
@Table(name = "account")
public class Account extends MasterRecord {

    @JoinColumn(name = "currency", table = "currency")
    private String currency;

    private String description;

    private BigDecimal balance;

    @Column(name = "account_type")
    private AccountType accountType;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}
