package excel.accounting.service;

import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowTypeConverter;
import excel.accounting.db.Transaction;
import excel.accounting.entity.IncomeItem;
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
 * Income Item Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public class IncomeItemService extends AbstractService implements
        RowTypeConverter<IncomeItem>, ExcelTypeConverter<IncomeItem> {

    @Override
    protected String getSqlFileName() {
        return "income-item";
    }

    /*
    * id, expense_date, description, currency, amount, status
    */
    public List<IncomeItem> loadAll() {
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
    private void updateStatus(Status requiredStatus, Status changedStatus, List<IncomeItem> itemList) {
        List<IncomeItem> filteredList = filteredByStatus(requiredStatus, itemList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (IncomeItem item : filteredList) {
            item.setStatus(changedStatus);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeItem item : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, item));
        }
        transaction.executeBatch();
    }

    public void setAsDrafted(List<IncomeItem> itemList) {
        updateStatus(Status.Confirmed, Status.Drafted, itemList);
    }

    public void setAsConfirmed(List<IncomeItem> itemList) {
        updateStatus(Status.Drafted, Status.Confirmed, itemList);
    }

    public void setAsClosed(List<IncomeItem> itemList) {
        updateStatus(Status.Confirmed, Status.Closed, itemList);
    }

    public void insertIncomeItem(List<IncomeItem> itemList) {
        QueryBuilder queryBuilder = getQueryBuilder("insertIncomeItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeItem item : itemList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, item));
        }
        transaction.executeBatch();
    }

    /*
    * income_date, description, currency, amount
    */
    public void updateIncomeItem(List<IncomeItem> itemList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateIncomeItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeItem item : itemList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, item));
        }
        transaction.executeBatch();
    }

    public void deleteIncomeItem(List<IncomeItem> itemList) {
        List<IncomeItem> filteredList = filteredByStatus(Status.Drafted, itemList);
        if (filteredList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deleteIncomeItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeItem item : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, item));
        }
        transaction.executeBatch();
    }

    /**
     * id, income_date, description, currency, amount, status
     */
    @Override
    public IncomeItem getRowType(QueryBuilder builder, Object[] objectArray) {
        IncomeItem item = new IncomeItem();
        item.setId((Integer) objectArray[0]);
        item.setIncomeDate((Date) objectArray[1]);
        item.setDescription((String) objectArray[2]);
        item.setCurrency((String) objectArray[3]);
        item.setAmount((BigDecimal) objectArray[4]);
        item.setStatus(DataConverter.getStatus(objectArray[5]));
        return item;
    }

    /**
     * insertIncomeItem
     * income_date, description, currency, amount, status
     * deleteIncomeItem
     * find by id
     * updateStatus
     * set status find by id
     * updateIncomeItem
     * income_date, description, currency, amount By Id
     */
    @Override
    public Map<Integer, Object> getRowObjectMap(QueryBuilder builder, IncomeItem type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertIncomeItem".equals(builder.getQueryName())) {
            map.put(1, type.getIncomeDate());
            map.put(2, type.getDescription());
            map.put(3, type.getCurrency());
            map.put(4, type.getAmount());
            map.put(5, Status.Drafted.toString());
        } else if ("deleteIncomeItem".equals(builder.getQueryName())) {
            map.put(1, type.getId());
        } else if ("updateStatus".equals(builder.getQueryName())) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getId());
        } else if ("updateIncomeItem".equals(builder.getQueryName())) {
            map.put(1, type.getIncomeDate());
            map.put(2, type.getDescription());
            map.put(3, type.getCurrency());
            map.put(4, type.getAmount());
            map.put(5, type.getId());
        }
        return map;
    }

    /**
     * id, income_date, description, currency, amount, status
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"id", "Income Date", "Description", "Currency", "Account", "Status"};
    }

    /**
     * id, income_date, description, currency, amount, status
     */
    @Override
    public IncomeItem getExcelType(String type, Cell[] array) {
        IncomeItem item = new IncomeItem();
        item.setId(DataConverter.getInteger(array[0]));
        item.setIncomeDate(DataConverter.getDate(array[1]));
        item.setDescription(DataConverter.getString(array[2]));
        item.setCurrency(DataConverter.getString(array[3]));
        item.setAmount(DataConverter.getBigDecimal(array[4]));
        item.setStatus(DataConverter.getStatus(array[5]));
        return item;
    }

    /**
     * id, income_date, description, currency, amount, status
     */
    @Override
    public Object[] getExcelRow(String type, IncomeItem item) {
        Object[] cellData = new Object[6];
        cellData[0] = item.getId();
        cellData[1] = item.getIncomeDate();
        cellData[2] = item.getDescription();
        cellData[3] = item.getCurrency();
        cellData[4] = item.getAmount();
        cellData[5] = item.getStatus().toString();
        return cellData;
    }

    private List<IncomeItem> filteredByStatus(Status status, List<IncomeItem> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
