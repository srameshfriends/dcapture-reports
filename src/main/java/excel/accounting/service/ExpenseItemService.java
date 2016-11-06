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
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.DataValidator;
import excel.accounting.shared.StringRules;
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

    public boolean isValidInsert(ExpenseItem expenseItem) {
        return !(expenseItem.getCode() == null || expenseItem.getExpenseDate() == null ||
                expenseItem.getDescription() == null || !DataValidator.isMoreThenZero(expenseItem.getAmount()));
    }

    public List<ExpenseItem> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, getExpenseItemDao());
    }

    public List<String> findExpenseCodeList() {
        QueryBuilder queryBuilder = getQueryBuilder("findExpenseCodeList");
        return getDataReader().findString(queryBuilder);
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
        return !(expenseItem.getCurrency() == null || expenseItem.getExpenseAccount() == null ||
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

    public void insertExpenseItem(List<ExpenseItem> itemList) {
        setMessage("Expense date, description and amount should not be empty");
        //
        int sequence = getExpenseItemDao().findLastSequence();
        List<ExpenseItem> validList = new ArrayList<>();
        for (ExpenseItem expenseItem : itemList) {
            if (insertValidate(expenseItem)) {
                validList.add(expenseItem);
            }
        }
        if (validList.isEmpty()) {
            setMessage("Valid accounts not found");
            return;
        }
        List<String> currencyList = getCurrencyDao().findCodeList();
        List<String> accountList = getAccountDao().findCodeList();
        List<String> categoryList = getExpenseCategoryDao().findCodeList();
        //
        QueryBuilder queryBuilder = getQueryBuilder("insertExpenseItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem expenseItem : validList) {
            sequence += 1;
            expenseItem.setCode(DataConverter.getSequence("EI", sequence));
            if (expenseItem.getCurrency() != null && !currencyList.contains(expenseItem.getCurrency())) {
                expenseItem.setCurrency(null);
            }
            if (expenseItem.getExpenseAccount() != null && !accountList.contains(expenseItem.getExpenseAccount())) {
                expenseItem.setExpenseAccount(null);
            }
            if (expenseItem.getExpenseCategory() != null && !categoryList.contains(expenseItem.getExpenseCategory())) {
                expenseItem.setCurrency(null);
            }
            transaction.addBatch(getColumnsMap("insertExpenseItem", expenseItem));
        }
        executeBatch(transaction);
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
            expenseItem.setExpenseAccount(expenseCode);
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
            map.put(2, type.getExpenseDate());
            map.put(3, type.getReferenceNumber());
            map.put(4, type.getDescription());
            map.put(5, type.getCurrency());
            map.put(6, type.getAmount());
            map.put(7, Status.Drafted.toString());
            map.put(8, type.getExpenseCategory());
            map.put(9, type.getExpenseAccount());
            map.put(10, type.getPaid());
        } else if ("deleteExpenseItem".equals(queryName)) {
            map.put(1, type.getCode());
        } else if ("updateStatus".equals(queryName)) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getCode());
        } else if ("updateCurrency".equals(queryName)) {
            map.put(1, type.getCurrency());
            map.put(2, type.getCode());
        } else if ("updateExpenseAccount".equals(queryName)) {
            map.put(1, type.getExpenseAccount());
            map.put(2, type.getCode());
        } else if ("updateExpenseCategory".equals(queryName)) {
            map.put(1, type.getExpenseCategory());
            map.put(2, type.getCode());
        }
        return map;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"Expense Code", "Expense Date", "Reference", "Description", "Currency", "Amount", "Status",
                "Category", "Account", "Paid Info"};
    }

    @Override
    public ExpenseItem getExcelType(String type, Cell[] array) {
        ExpenseItem item = new ExpenseItem();
        item.setCode(DataConverter.getString(array[0]));
        item.setExpenseDate(DataConverter.getDate(array[1]));
        item.setReferenceNumber(DataConverter.getString(array[2]));
        item.setDescription(DataConverter.getString(array[3]));
        item.setCurrency(DataConverter.getString(array[4]));
        item.setAmount(DataConverter.getBigDecimal(array[5]));
        item.setStatus(DataConverter.getStatus(array[6]));
        item.setExpenseCategory(DataConverter.getString(array[7]));
        item.setExpenseAccount(DataConverter.getString(array[8]));
        item.setPaid(DataConverter.getBoolean(array[9]));
        return item;
    }

    @Override
    public Object[] getExcelRow(String type, ExpenseItem item) {
        Object[] cellData = new Object[10];
        cellData[0] = item.getCode();
        cellData[1] = item.getExpenseDate();
        cellData[2] = item.getReferenceNumber();
        cellData[3] = item.getDescription();
        cellData[4] = item.getCurrency();
        cellData[5] = item.getAmount();
        cellData[6] = item.getStatus();
        cellData[7] = item.getExpenseCategory();
        cellData[8] = item.getExpenseAccount();
        cellData[9] = item.getPaid();
        return cellData;
    }

    private List<ExpenseItem> filteredByStatus(Status status, List<ExpenseItem> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
