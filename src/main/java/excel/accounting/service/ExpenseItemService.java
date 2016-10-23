package excel.accounting.service;

import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowTypeConverter;
import excel.accounting.db.Transaction;
import excel.accounting.entity.ExpenseItem;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.shared.DataConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.math.BigDecimal;
import java.util.Date;
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
        RowTypeConverter<ExpenseItem>, ExcelTypeConverter<ExpenseItem> {

    @Override
    protected String getSqlFileName() {
        return "expense-item";
    }

    /*
    * id, expense_date, description, currency, amount, status
    */
    public List<ExpenseItem> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    /*
    * find all id list
    */
    public List<Integer> findIdList() {
        QueryBuilder queryBuilder = getQueryBuilder("findIdList");
        return getDataReader().findInteger(queryBuilder);
    }

    /**
     * status, code
     */
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
        for (ExpenseItem item : itemList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, item));
        }
        transaction.executeBatch();
    }

    public void setAsDrafted(List<ExpenseItem> itemList) {
        updateStatus(Status.Confirmed, Status.Drafted, itemList);
    }

    public void setAsConfirmed(List<ExpenseItem> itemList) {
        updateStatus(Status.Drafted, Status.Confirmed, itemList);
    }

    public void setAsClosed(List<ExpenseItem> itemList) {
        updateStatus(Status.Confirmed, Status.Closed, itemList);
    }

    public void insertExpenseItem(List<ExpenseItem> itemList) {
        QueryBuilder queryBuilder = getQueryBuilder("insertExpenseItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem item : itemList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, item));
        }
        transaction.executeBatch();
    }

    /*
    * expense_date, description, currency, amount
    */
    public void updateExpenseItem(List<ExpenseItem> itemList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateExpenseItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseItem item : itemList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, item));
        }
        transaction.executeBatch();
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
            transaction.addBatch(getRowObjectMap(queryBuilder, item));
        }
        transaction.executeBatch();
    }

    /**
     * id, expense_date, description, currency, amount, status
     */
    @Override
    public ExpenseItem getRowType(QueryBuilder builder, Object[] objectArray) {
        ExpenseItem item = new ExpenseItem();
        item.setId((Integer) objectArray[0]);
        item.setExpenseDate((Date) objectArray[1]);
        item.setDescription((String) objectArray[2]);
        item.setCurrency((String) objectArray[3]);
        item.setAmount((BigDecimal) objectArray[4]);
        item.setStatus(DataConverter.getStatus(objectArray[5]));
        return item;
    }

    /**
     * insertExpenseItem
     * expense_date, description, currency, amount, status
     * deleteExpenseItem
     * find by id
     * updateStatus
     * set status find by id
     * updateExpenseItem
     * expense_date, description, currency, amount By Id
     */
    @Override
    public Map<Integer, Object> getRowObjectMap(QueryBuilder builder, ExpenseItem type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertExpenseItem".equals(builder.getQueryName())) {
            map.put(1, type.getExpenseDate());
            map.put(2, type.getDescription());
            map.put(3, type.getCurrency());
            map.put(4, type.getAmount());
            map.put(5, Status.Drafted.toString());
        } else if ("deleteExpenseItem".equals(builder.getQueryName())) {
            map.put(1, type.getId());
        } else if ("updateStatus".equals(builder.getQueryName())) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getId());
        } else if ("updateExpenseItem".equals(builder.getQueryName())) {
            map.put(1, type.getExpenseDate());
            map.put(2, type.getDescription());
            map.put(3, type.getCurrency());
            map.put(4, type.getAmount());
            map.put(5, type.getId());
        }
        return map;
    }

    /**
     * id, expense_date, description, currency, amount, status
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"id", "Expense Date", "Description", "Currency", "Account", "Status"};
    }

    /**
     * id, expense_date, description, currency, amount, status
     */
    @Override
    public ExpenseItem getExcelType(String type, Cell[] array) {
        ExpenseItem item = new ExpenseItem();
        item.setId(DataConverter.getInteger(array[0]));
        item.setExpenseDate(DataConverter.getDate(array[1]));
        item.setDescription(DataConverter.getString(array[2]));
        item.setCurrency(DataConverter.getString(array[3]));
        item.setAmount(DataConverter.getBigDecimal(array[4]));
        item.setStatus(DataConverter.getStatus(array[5]));
        return item;
    }

    /**
     * id, expense_date, description, currency, amount, status
     */
    @Override
    public Object[] getExcelRow(String type, ExpenseItem item) {
        Object[] cellData = new Object[6];
        cellData[0] = item.getId();
        cellData[1] = item.getExpenseDate();
        cellData[2] = item.getDescription();
        cellData[3] = item.getCurrency();
        cellData[4] = item.getAmount();
        cellData[5] = item.getStatus().toString();
        return cellData;
    }

    private List<ExpenseItem> filteredByStatus(Status status, List<ExpenseItem> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
