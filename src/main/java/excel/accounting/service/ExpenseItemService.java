package excel.accounting.service;

import excel.accounting.dao.AccountDao;
import excel.accounting.dao.CurrencyDao;
import excel.accounting.dao.ExpenseCategoryDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.EntityToRowColumns;
import excel.accounting.db.Transaction;
import excel.accounting.entity.*;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.ExpenseItemDao;
import excel.accounting.shared.*;
import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Expense Item Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public class ExpenseItemService extends AbstractService implements
        EntityToRowColumns<ExpenseItem>, ExcelTypeConverter<ExpenseItem> {
    private CurrencyDao currencyDao;
    private ExpenseItemDao expenseItemDao;
    private AccountDao accountDao;
    private ExpenseCategoryDao expenseCategoryDao;

    @Override
    protected String getSqlFileName() {
        return "expense-item";
    }

    private AccountDao getAccountDao() {
        if (accountDao == null) {
            accountDao = (AccountDao) getBean("accountDao");
        }
        return accountDao;
    }

    private ExpenseCategoryDao getExpenseCategoryDao() {
        if (expenseCategoryDao == null) {
            expenseCategoryDao = (ExpenseCategoryDao) getBean("expenseCategoryDao");
        }
        return expenseCategoryDao;
    }

    private CurrencyDao getCurrencyDao() {
        if (currencyDao == null) {
            currencyDao = (CurrencyDao) getBean("currencyDao");
        }
        return currencyDao;
    }

    private ExpenseItemDao getExpenseItemDao() {
        if (expenseItemDao == null) {
            expenseItemDao = (ExpenseItemDao) getBean("expenseItemDao");
        }
        return expenseItemDao;
    }

    private void updateStatus(List<ExpenseItem> itemList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem item : itemList) {
            transaction.addBatch(getColumnsMap("updateStatus", item));
        }
        executeBatch(transaction);
    }

    public void setAsDrafted(List<ExpenseItem> dataList) {
        List<ExpenseItem> filteredList = filteredByStatus(Status.Confirmed, dataList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : Confirmed expense items are allowed to modify as drafted");
            return;
        }
        for (ExpenseItem expenseItem : filteredList) {
            String errorMessage = getExpenseItemDao().isEntityReferenceUsed(expenseItem.getCode());
            if(errorMessage != null) {
                setMessage(errorMessage);
                return;
            }
            expenseItem.setStatus(Status.Drafted);
        }
        updateStatus(filteredList);
    }

    private boolean confirmValidate(ExpenseItem expenseItem) {
        return !(expenseItem.getCurrency() == null || expenseItem.getAccount() == null ||
                expenseItem.getExpenseCategory() == null);
    }

    public void setAsConfirmed(List<ExpenseItem> expenseItemList) {
        List<ExpenseItem> filteredList = filteredByStatus(Status.Drafted, expenseItemList);
        if (filteredList.isEmpty()) {
            setMessage("Error : Only drafted expenses are allowed to confirm");
            return;
        }
        List<ExpenseItem> validList = new ArrayList<>();
        filteredList.stream().filter(this::confirmValidate).forEach(expenseItem -> {
            expenseItem.setStatus(Status.Confirmed);
            validList.add(expenseItem);
        });
        if (validList.isEmpty()) {
            setMessage("Error : valid drafted expenses not found");
            return;
        }
        updateStatus(validList);
    }

    private boolean insertValidate(ExpenseItem item) {
        return !(item.getExpenseDate() == null || StringRules.isEmpty(item.getDescription()) ||
                !DataValidator.isMoreThenZero(item.getAmount()));
    }

    public boolean insertExpenseItem(List<ExpenseItem> itemList) {
        setMessage("");
        int sequence = getExpenseItemDao().findLastSequence();
        List<ExpenseItem> validList = new ArrayList<>();
        for (ExpenseItem expenseItem : itemList) {
            if (insertValidate(expenseItem)) {
                validList.add(expenseItem);
            }
        }
        if (validList.isEmpty()) {
            setMessage("Valid accounts not found");
            return false;
        }
        List<String> currencyList = new ArrayList<>(); // getCurrencyDao().findCodeList()
        List<String> accountList = getAccountDao().findCodeList();
        List<String> categoryList = getExpenseCategoryDao().findCodeList();
        //
        QueryBuilder queryBuilder = getQueryBuilder("insertExpenseItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        StringRules rules = new StringRules();
        rules.setFirstCharAlphaOnly(true);
        rules.setMinMaxLength(2, 6);
        rules.setRulesType(RulesType.Alphanumeric);
        for (ExpenseItem expenseItem : validList) {
            if(!rules.isValid(expenseItem.getCode())) {
                sequence += 1;
                expenseItem.setCode(EntitySequence.getExpenseItemCode(sequence));
            }
            if (expenseItem.getCurrency() != null && !currencyList.contains(expenseItem.getCurrency())) {
                expenseItem.setCurrency(null);
            }
            if (expenseItem.getAccount() != null && !accountList.contains(expenseItem.getAccount())) {
                expenseItem.setAccount(null);
            }
            if (expenseItem.getExpenseCategory() != null && !categoryList.contains(expenseItem.getExpenseCategory())) {
                expenseItem.setCurrency(null);
            }
            transaction.addBatch(getColumnsMap("insertExpenseItem", expenseItem));
        }
        executeBatch(transaction);
        return true;
    }

    public void deleteExpenseItem(List<ExpenseItem> itemList) {
        List<ExpenseItem> filteredList = filteredByStatus(Status.Drafted, itemList);
        if (filteredList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deleteExpenseItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem item : filteredList) {
            transaction.addBatch(getColumnsMap("deleteExpenseItem", item));
        }
        executeBatch(transaction);
    }

    public void updateExpenseAccount(Account account, List<ExpenseItem> expenseItemList) {
        final String expenseCode = account == null ? null : account.getCode();
        QueryBuilder queryBuilder = getQueryBuilder("updateExpenseAccount");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem expenseItem : expenseItemList) {
            expenseItem.setAccount(expenseCode);
            transaction.addBatch(getColumnsMap("updateExpenseAccount", expenseItem));
        }
        executeBatch(transaction);
    }

    public void updateCurrency(Currency currency, List<ExpenseItem> dataList) {
        List<ExpenseItem> filteredList = filteredByStatus(Status.Drafted, dataList);
        if (filteredList.isEmpty()) {
            setMessage("Error : Only drafted accounts are allowed to change currency");
            return;
        }
        final String currencyCode = currency == null ? null : currency.getCode();
        for (ExpenseItem expenseItem : filteredList) {
            expenseItem.setCurrency(currencyCode);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateCurrency");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem expenseItem : filteredList) {
            transaction.addBatch(getColumnsMap("updateCurrency", expenseItem));
        }
        executeBatch(transaction);
    }

    public void updateExpenseCategory(ExpenseCategory expenseCategory, List<ExpenseItem> expenseItemList) {
        final String categoryCode = expenseCategory == null ? null : expenseCategory.getCode();
        QueryBuilder queryBuilder = getQueryBuilder("updateExpenseCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem expenseItem : expenseItemList) {
            expenseItem.setExpenseCategory(categoryCode);
            transaction.addBatch(getColumnsMap("updateExpenseCategory", expenseItem));
        }
        executeBatch(transaction);
    }

    @Override
    public Map<Integer, Object> getColumnsMap(final String queryName, ExpenseItem type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertExpenseItem".equals(queryName)) {
            map.put(1, type.getCode());
            map.put(2, type.getGroupCode());
            map.put(3, type.getExpenseDate());
            map.put(4, type.getReference());
            map.put(5, type.getDescription());
            map.put(6, type.getCurrency());
            map.put(7, type.getAmount());
            map.put(8, Status.Drafted);
            map.put(9, type.getExpenseCategory());
            map.put(10, type.getAccount());
            map.put(11, PaidStatus.Unpaid);
        } else if ("deleteExpenseItem".equals(queryName)) {
            map.put(1, type.getCode());
        } else if ("updateStatus".equals(queryName)) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getCode());
        } else if ("updateCurrency".equals(queryName)) {
            map.put(1, type.getCurrency());
            map.put(2, type.getCode());
        } else if ("updateExpenseAccount".equals(queryName)) {
            map.put(1, type.getAccount());
            map.put(2, type.getCode());
        } else if ("updateExpenseCategory".equals(queryName)) {
            map.put(1, type.getExpenseCategory());
            map.put(2, type.getCode());
        }
        return map;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"Expense Code", "Group Code", "Expense Date", "Reference", "Description", "Currency",
                "Amount", "Status", "Category", "Account", "Paid"};
    }

    @Override
    public ExpenseItem getExcelType(String type, Cell[] array) {
        ExpenseItem item = new ExpenseItem();
        item.setCode(DataConverter.getString(array[0]));
        item.setGroupCode(DataConverter.getString(array[1]));
        item.setExpenseDate(DataConverter.getDate(array[2]));
        item.setReference(DataConverter.getString(array[3]));
        item.setDescription(DataConverter.getString(array[4]));
        item.setCurrency(DataConverter.getString(array[5]));
        item.setAmount(DataConverter.getBigDecimal(array[6]));
        item.setStatus(DataConverter.getStatus(array[7]));
        item.setExpenseCategory(DataConverter.getString(array[8]));
        item.setAccount(DataConverter.getString(array[9]));
        item.setPaidStatus(DataConverter.getPaidStatus(array[10]));
        return item;
    }

    @Override
    public Object[] getExcelRow(String type, ExpenseItem item) {
        Object[] cellData = new Object[11];
        cellData[0] = item.getCode();
        cellData[1] = item.getGroupCode();
        cellData[2] = item.getExpenseDate();
        cellData[3] = item.getReference();
        cellData[4] = item.getDescription();
        cellData[5] = item.getCurrency();
        cellData[6] = item.getAmount();
        cellData[7] = item.getStatus();
        cellData[8] = item.getExpenseCategory();
        cellData[9] = item.getAccount();
        cellData[10] = item.getPaidStatus();
        return cellData;
    }

    private List<ExpenseItem> filteredByStatus(Status status, List<ExpenseItem> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
