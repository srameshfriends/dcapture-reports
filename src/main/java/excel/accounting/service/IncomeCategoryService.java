package excel.accounting.service;

import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowTypeConverter;
import excel.accounting.db.Transaction;
import excel.accounting.entity.IncomeCategory;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.shared.DataConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Income Category Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public class IncomeCategoryService extends AbstractService implements
        RowTypeConverter<IncomeCategory>, ExcelTypeConverter<IncomeCategory> {

    @Override
    protected String getSqlFileName() {
        return "income-category";
    }

    public List<IncomeCategory> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    /*
   * code
   */
    public List<String> findCodeList() {
        QueryBuilder queryBuilder = getQueryBuilder("findCodeList");
        return getDataReader().findString(queryBuilder);
    }

    /**
     * status, code
     */
    private void updateStatus(Status requiredStatus, Status changedStatus, List<IncomeCategory> categoryList) {
        List<IncomeCategory> filteredList = filteredByStatus(requiredStatus, categoryList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (IncomeCategory category : filteredList) {
            category.setStatus(changedStatus);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeCategory category : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, category));
        }
        transaction.executeBatch();
    }

    public void setAsDrafted(List<IncomeCategory> categoryList) {
        updateStatus(Status.Confirmed, Status.Drafted, categoryList);
    }

    public void setAsConfirmed(List<IncomeCategory> categoryList) {
        updateStatus(Status.Drafted, Status.Confirmed, categoryList);
    }

    public void setAsClosed(List<IncomeCategory> categoryList) {
        updateStatus(Status.Confirmed, Status.Closed, categoryList);
    }

    public void insertIncomeCategory(List<IncomeCategory> categoryList) {
        QueryBuilder queryBuilder = getQueryBuilder("insertIncomeCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeCategory category : categoryList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, category));
        }
        transaction.executeBatch();
    }

    public void updateIncomeCategory(List<IncomeCategory> categoryList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateIncomeCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeCategory category : categoryList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, category));
        }
        transaction.executeBatch();
    }

    public void deleteIncomeCategory(List<IncomeCategory> categoryList) {
        List<IncomeCategory> filteredList = filteredByStatus(Status.Drafted, categoryList);
        if (filteredList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deleteIncomeCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeCategory category : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, category));
        }
        transaction.executeBatch();
    }

    /**
     * code, name, status, currency, income_account, description
     */
    @Override
    public IncomeCategory getRowType(QueryBuilder builder, Object[] objectArray) {
        IncomeCategory category = new IncomeCategory();
        category.setCode((String) objectArray[0]);
        category.setName((String) objectArray[1]);
        category.setStatus(DataConverter.getStatus(objectArray[2]));
        category.setCurrency((String) objectArray[3]);
        category.setIncomeAccount((String) objectArray[4]);
        category.setDescription((String) objectArray[5]);
        return category;
    }

    /**
     * insertIncomeCategory
     * code, name, status, currency, income_account, description
     * deleteIncomeCategory
     * find by code
     * updateStatus
     * set status find by code
     * updateIncomeCategory
     * code, name, currency, income_account, description
     */
    @Override
    public Map<Integer, Object> getRowObjectMap(QueryBuilder builder, IncomeCategory type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertIncomeCategory".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, Status.Drafted.toString());
            map.put(4, type.getCurrency());
            map.put(5, type.getIncomeAccount());
            map.put(6, type.getDescription());
        } else if ("deleteIncomeCategory".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
        } else if ("updateStatus".equals(builder.getQueryName())) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getCode());
        } else if ("updateIncomeCategory".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, type.getCurrency());
            map.put(4, type.getIncomeAccount());
            map.put(5, type.getDescription());
            map.put(6, type.getCode());
        }
        return map;
    }

    /**
     * code, name, status, currency, income account, description
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"Category Code", "Name", "Status", "Currency", "Income Account", "Description"};
    }

    /**
     * code, name, status, currency, income_account, description
     */
    @Override
    public IncomeCategory getExcelType(String type, Cell[] array) {
        IncomeCategory category = new IncomeCategory();
        category.setCode(DataConverter.getString(array[0]));
        category.setName(DataConverter.getString(array[1]));
        category.setStatus(DataConverter.getStatus(array[2]));
        category.setCurrency(DataConverter.getString(array[3]));
        category.setIncomeAccount(DataConverter.getString(array[4]));
        category.setDescription(DataConverter.getString(array[5]));
        return category;
    }

    /**
     * code, name, status, currency, income_account description
     */
    @Override
    public Object[] getExcelRow(String type, IncomeCategory category) {
        Object[] cellData = new Object[6];
        cellData[0] = category.getCode();
        cellData[1] = category.getName();
        cellData[2] = category.getStatus().toString();
        cellData[3] = category.getCurrency();
        cellData[4] = category.getIncomeAccount();
        cellData[5] = category.getDescription();
        return cellData;
    }

    private List<IncomeCategory> filteredByStatus(Status status, List<IncomeCategory> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
