package excel.accounting.service;

import excel.accounting.dao.CurrencyDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.EntityToRowColumns;
import excel.accounting.db.Transaction;
import excel.accounting.entity.*;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.ExpenseItemDao;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.DataValidator;
import org.apache.poi.ss.usermodel.Cell;

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
    private CurrencyService currencyService;
    private ExpenseItemDao expenseItemDao;

    @Override
    protected String getSqlFileName() {
        return "expense-item";
    }

    private CurrencyService getCurrencyService() {
        if (currencyService == null) {
            currencyService = (CurrencyService) getBean("currencyService");
        }
        return currencyService;
    }

    private CurrencyDao getCurrencyDao() {
        if (currencyDao == null) {
            currencyDao = (CurrencyDao) getBean("currencyDao");
        }
        return currencyDao;
    }

    public ExpenseItemDao getExpenseItemDao() {
        if (expenseItemDao == null) {
            expenseItemDao = (ExpenseItemDao) getBean("expenseItemDao");
        }
        return expenseItemDao;
    }

    public boolean isValidInsert(ExpenseItem expenseItem) {
        return !(expenseItem.getExpenseCode() == null || expenseItem.getExpenseDate() == null ||
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

    private void updateStatus(Status requiredStatus, Status changedStatus, List<ExpenseItem> itemList) {
        List<ExpenseItem> filteredList = filteredByStatus(requiredStatus, itemList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (ExpenseItem item : filteredList) {
            item.setStatus(changedStatus);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem item : filteredList) {
            transaction.addBatch(getColumnsMap("updateStatus", item));
        }
        executeBatch(transaction);
    }

    public void setAsDrafted(List<ExpenseItem> itemList) {
        updateStatus(Status.Confirmed, Status.Drafted, itemList);
    }

    public void setAsConfirmed(List<ExpenseItem> itemList) {
        updateStatus(Status.Drafted, Status.Confirmed, itemList);
    }

    public void insertExpenseItem(List<ExpenseItem> itemList) {
        List<String> currencyCodeList = getCurrencyDao().findCodeList();
        QueryBuilder queryBuilder = getQueryBuilder("insertExpenseItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem item : itemList) {
            if (!currencyCodeList.contains(item.getCurrency())) {
                item.setCurrency(null);
            }
            transaction.addBatch(getColumnsMap("insertExpenseItem", item));
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

    public void updateExpenseAccount(Account expenseAccount, List<ExpenseItem> expenseItemList) {
        final String expenseAccountNumber = expenseAccount == null ? null : expenseAccount.getAccountNumber();
        QueryBuilder queryBuilder = getQueryBuilder("updateExpenseAccount");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem expenseItem : expenseItemList) {
            expenseItem.setExpenseAccount(expenseAccountNumber);
            transaction.addBatch(getColumnsMap("updateExpenseAccount", expenseItem));
        }
        executeBatch(transaction);
    }

    public void updateCurrency(Currency currency, List<ExpenseItem> expenseItemList) {
        final String currencyCode = currency == null ? null : currency.getCode();
        QueryBuilder queryBuilder = getQueryBuilder("updateExpenseAccount");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem expenseItem : expenseItemList) {
            expenseItem.setCurrency(currencyCode);
            transaction.addBatch(getColumnsMap("updateExpenseAccount", expenseItem));
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

    /**
     * insertExpenseItem
     * expense_code, expense_date, reference_number, description, currency, amount, status
     * deleteExpenseItem
     * find by expense_code
     * updateStatus
     * set status find by expense_code
     */
    @Override
    public Map<Integer, Object> getColumnsMap(final String queryName, ExpenseItem type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertExpenseItem".equals(queryName)) {
            map.put(1, type.getExpenseCode());
            map.put(2, type.getExpenseDate());
            map.put(3, type.getReferenceNumber());
            map.put(4, type.getDescription());
            map.put(5, type.getCurrency());
            map.put(6, type.getAmount());
            map.put(7, Status.Drafted.toString());
        } else if ("deleteExpenseItem".equals(queryName)) {
            map.put(1, type.getExpenseCode());
        } else if ("updateStatus".equals(queryName)) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getExpenseCode());
        } else if ("updateExpenseAccount".equals(queryName)) {
            map.put(1, type.getExpenseAccount());
            map.put(2, type.getExpenseCode());
        } else if ("updateExpenseCategory".equals(queryName)) {
            map.put(1, type.getExpenseCategory());
            map.put(2, type.getExpenseCode());
        }
        return map;
    }

    /**
     * expense_code, expense_date, reference_number, description, currency, amount, status
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"Expense Code", "Expense Date", "Reference Number", "Description", "Currency", "Amount", "Status"};
    }

    /**
     * expense_code, expense_date, reference_number, description, currency, amount, status
     */
    @Override
    public ExpenseItem getExcelType(String type, Cell[] array) {
        ExpenseItem item = new ExpenseItem();
        item.setExpenseCode(DataConverter.getString(array[0]));
        item.setExpenseDate(DataConverter.getDate(array[1]));
        item.setReferenceNumber(DataConverter.getString(array[2]));
        item.setDescription(DataConverter.getString(array[3]));
        item.setCurrency(DataConverter.getString(array[4]));
        item.setAmount(DataConverter.getBigDecimal(array[5]));
        item.setStatus(DataConverter.getStatus(array[6]));
        return item;
    }

    /**
     * expense_code, expense_date, reference_number, description, currency, amount, status
     */
    @Override
    public Object[] getExcelRow(String type, ExpenseItem item) {
        Object[] cellData = new Object[7];
        cellData[0] = item.getExpenseCode();
        cellData[1] = item.getExpenseDate();
        cellData[2] = item.getReferenceNumber();
        cellData[3] = item.getDescription();
        cellData[4] = item.getCurrency();
        cellData[5] = item.getAmount();
        cellData[6] = item.getStatus().toString();
        return cellData;
    }

    private List<ExpenseItem> filteredByStatus(Status status, List<ExpenseItem> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
