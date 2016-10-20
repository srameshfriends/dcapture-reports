package excel.accounting.entity;

import excel.accounting.model.EntityRow;

/**
 * Account
 */
public class Account extends EntityRow {
    private String name, accountNumber;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
