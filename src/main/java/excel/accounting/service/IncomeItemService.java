package excel.accounting.service;

import excel.accounting.entity.IncomeItem;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.IncomeItemDao;
import excel.accounting.shared.DataConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Income Item Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public class IncomeItemService extends AbstractService implements ExcelTypeConverter<IncomeItem> {
    private IncomeItemDao incomeItemDao;

    private IncomeItemDao getIncomeItemDao() {
        if (incomeItemDao == null) {
            incomeItemDao = (IncomeItemDao) getBean("incomeItemDao");
        }
        return incomeItemDao;
    }

    @Override
    protected String getSqlFileName() {
        return "income-item";
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
        /*QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeItem item : filteredList) {
            transaction.addBatch(getColumnsMap("updateStatus", item));
        }
        executeBatch(transaction);*/
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
        /*QueryBuilder queryBuilder = getQueryBuilder("insertIncomeItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeItem item : itemList) {
            transaction.addBatch(getColumnsMap("insertIncomeItem", item));
        }
        executeBatch(transaction);*/
    }

    /*
    * income_date, description, currency, amount
    */
    public void updateIncomeItem(List<IncomeItem> itemList) {
        /*QueryBuilder queryBuilder = getQueryBuilder("updateIncomeItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeItem item : itemList) {
            transaction.addBatch(getColumnsMap("updateIncomeItem", item));
        }
        executeBatch(transaction);*/
    }

    public void deleteIncomeItem(List<IncomeItem> itemList) {
        List<IncomeItem> filteredList = filteredByStatus(Status.Drafted, itemList);
        if (filteredList.isEmpty()) {
            return;
        }
        /*QueryBuilder queryBuilder = getQueryBuilder("deleteIncomeItem");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeItem item : filteredList) {
            transaction.addBatch(getColumnsMap("deleteIncomeItem", item));
        }
        executeBatch(transaction);*/
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
        item.setCode(DataConverter.getString(array[0]));
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
        cellData[0] = item.getCode();
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
