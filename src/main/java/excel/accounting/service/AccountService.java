package excel.accounting.service;

import excel.accounting.db.*;
import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.Currency;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.shared.DataConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Account Service
 */
public class AccountService extends AbstractService implements RowTypeConverter<Account>, ExcelTypeConverter<Account> {
    private CurrencyService currencyService;

    @Override
    protected String getSqlFileName() {
        return "account";
    }

    private CurrencyService getCurrencyService() {
        if (currencyService == null) {
            currencyService = (CurrencyService) getService("currencyService");
        }
        return currencyService;
    }

    public List<AccountType> getAccountTypeList() {
        List<AccountType> accountTypeList = new ArrayList<>();
        Collections.addAll(accountTypeList, AccountType.values());
        return accountTypeList;
    }

    public List<Account> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<Account> findAccountsByType(String searchText, AccountType... accountTypeArray) {
        InClauseQuery inClauseQuery = new InClauseQuery(accountTypeArray);
        QueryBuilder queryBuilder = getQueryBuilder("findAccountsByType");
        queryBuilder.addInClauseQuery("$account_type", inClauseQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("account_number", "name", "description");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<String> findAccountNumberList() {
        QueryBuilder queryBuilder = getQueryBuilder("findAccountNumberList");
        return getDataReader().findString(queryBuilder);
    }

    private void updateStatus(Status requiredStatus, Status changedStatus, List<Account> accountList) {
        List<Account> filteredList = filteredByStatus(requiredStatus, accountList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (Account account : filteredList) {
            account.setStatus(changedStatus);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Account account : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, account));
        }
        transaction.executeBatch();
    }

    public void setAsDrafted(List<Account> accountList) {
        updateStatus(Status.Confirmed, Status.Drafted, accountList);
    }

    public void setAsConfirmed(List<Account> accountList) {
        updateStatus(Status.Drafted, Status.Confirmed, accountList);
    }

    public void setAsClosed(List<Account> accountList) {
        updateStatus(Status.Confirmed, Status.Closed, accountList);
    }

    public void insertAccount(List<Account> accountList) {
        List<String> currencyList = getCurrencyService().findCodeList();
        List<AccountType> accountTypeList = getAccountTypeList();
        //
        QueryBuilder queryBuilder = getQueryBuilder("insertAccount");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Account account : accountList) {
            if(account.getAccountType() != null && !accountTypeList.contains(account.getAccountType())) {
                account.setAccountType(null);
            }
            if(account.getCurrency() != null && !currencyList.contains(account.getCurrency())) {
                account.setCurrency(null);
            }
            transaction.addBatch(getRowObjectMap(queryBuilder, account));
        }
        transaction.executeBatch();
    }

    public void updateAccount(List<Account> accountList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateAccount");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Account account : accountList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, account));
        }
        transaction.executeBatch();
    }

    public void deleteAccount(List<Account> accountList) {
        List<Account> filteredList = filteredByStatus(Status.Drafted, accountList);
        if (filteredList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deleteAccount");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Account account : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, account));
        }
        transaction.executeBatch();
    }

    /**
     * account_number, name, account_type, status, currency, balance, description
     */
    @Override
    public Account getRowType(QueryBuilder builder, Object[] objectArray) {
        Account account = new Account();
        account.setAccountNumber((String) objectArray[0]);
        account.setName((String) objectArray[1]);
        account.setAccountType(DataConverter.getAccountType(objectArray[2]));
        account.setStatus(DataConverter.getStatus(objectArray[3]));
        account.setCurrency((String) objectArray[4]);
        account.setBalance((BigDecimal) objectArray[5]);
        account.setDescription((String) objectArray[6]);
        return account;
    }

    /**
     * insertAccount
     * account_number, name, account_type, status, currency, balance, description
     * deleteAccount
     * find by account_number
     * updateStatus
     * set status find by account_number
     * updateAccount
     * account_number, name, account_type, currency, description find by account_number
     */
    @Override
    public Map<Integer, Object> getRowObjectMap(QueryBuilder builder, Account type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertAccount".equals(builder.getQueryName())) {
            map.put(1, type.getAccountNumber());
            map.put(2, type.getName());
            map.put(3, type.getAccountType().toString());
            map.put(4, Status.Drafted.toString());
            map.put(5, type.getCurrency());
            map.put(6, BigDecimal.ZERO);
            map.put(7, type.getDescription());
        } else if ("deleteAccount".equals(builder.getQueryName())) {
            map.put(1, type.getAccountNumber());
        } else if ("updateStatus".equals(builder.getQueryName())) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getAccountNumber());
        } else if ("updateAccount".equals(builder.getQueryName())) {
            map.put(1, type.getAccountNumber());
            map.put(2, type.getName());
            map.put(3, type.getAccountType().toString());
            map.put(4, type.getCurrency());
            map.put(5, type.getDescription());
            map.put(6, type.getAccountNumber());
        }
        return map;
    }

    /**
     * account_number, name, account_type, status, currency, balance, description
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"Account Number", "Name", "Account Type", "Status", "Currency", "Description"};
    }

    /**
     * account_number, name, account_type, status, currency, description
     */
    @Override
    public Account getExcelType(String type, Cell[] array) {
        Account account = new Account();
        account.setAccountNumber(DataConverter.getString(array[0]));
        account.setName(DataConverter.getString(array[1]));
        account.setAccountType(DataConverter.getAccountType(array[2]));
        account.setStatus(DataConverter.getStatus(array[3]));
        account.setCurrency(DataConverter.getString(array[4]));
        account.setDescription(DataConverter.getString(array[5]));
        return account;
    }

    /**
     * account_number, name, account_type, status, currency, balance, description
     */
    @Override
    public Object[] getExcelRow(String type, Account account) {
        Object[] cellData = new Object[7];
        cellData[0] = account.getAccountNumber();
        cellData[1] = account.getName();
        cellData[2] = account.getAccountType().toString();
        cellData[3] = account.getStatus().toString();
        cellData[4] = account.getCurrency();
        cellData[5] = account.getDescription();
        return cellData;
    }

    private List<Account> filteredByStatus(Status status, List<Account> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
