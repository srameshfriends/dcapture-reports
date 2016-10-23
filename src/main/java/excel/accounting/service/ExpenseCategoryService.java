package excel.accounting.service;

import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowTypeConverter;
import excel.accounting.db.Transaction;
import excel.accounting.entity.ExpenseCategory;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.shared.DataConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Expense Category Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public class ExpenseCategoryService extends AbstractService implements
        RowTypeConverter<ExpenseCategory>, ExcelTypeConverter<ExpenseCategory> {

    @Override
    protected String getSqlFileName() {
        return "expense-category";
    }

    /*
    * id, code, name, status, currency, debit_account, credit_account, description order by code
    */
    public List<ExpenseCategory> loadAll() {
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
    private void updateStatus(Status requiredStatus, Status changedStatus, List<ExpenseCategory> categoryList) {
        List<ExpenseCategory> filteredList = filteredByStatus(requiredStatus, categoryList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (ExpenseCategory category : filteredList) {
            category.setStatus(changedStatus);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseCategory category : categoryList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, category));
        }
        transaction.executeBatch();
    }

    public void setAsDrafted(List<ExpenseCategory> categoryList) {
        updateStatus(Status.Confirmed, Status.Drafted, categoryList);
    }

    public void setAsConfirmed(List<ExpenseCategory> categoryList) {
        updateStatus(Status.Drafted, Status.Confirmed, categoryList);
    }

    public void setAsClosed(List<ExpenseCategory> categoryList) {
        updateStatus(Status.Confirmed, Status.Closed, categoryList);
    }

    public void insertExpenseCategory(List<ExpenseCategory> categoryList) {
        QueryBuilder queryBuilder = getQueryBuilder("insertExpenseCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseCategory category : categoryList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, category));
        }
        transaction.executeBatch();
    }

    /*
    * code, name, category, currency, description find by account_number
    */
    public void updateExpenseCategory(List<ExpenseCategory> categoryList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateExpenseCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseCategory category : categoryList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, category));
        }
        transaction.executeBatch();
    }

    public void deleteExpenseCategory(List<ExpenseCategory> categoryList) {
        List<ExpenseCategory> filteredList = filteredByStatus(Status.Drafted, categoryList);
        if (filteredList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deleteExpenseCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseCategory category : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, category));
        }
        transaction.executeBatch();
    }

    /**
     * id, code, name, status, currency, debit_account, credit_account description
     */
    @Override
    public ExpenseCategory getRowType(QueryBuilder builder, Object[] objectArray) {
        ExpenseCategory category = new ExpenseCategory();
        category.setId((Integer) objectArray[0]);
        category.setCode((String) objectArray[1]);
        category.setName((String) objectArray[2]);
        category.setStatus(DataConverter.getStatus(objectArray[3]));
        category.setCurrency((String) objectArray[4]);
        category.setDebitAccount((String) objectArray[5]);
        category.setCreditAccount((String) objectArray[6]);
        category.setDescription((String) objectArray[7]);
        return category;
    }

    /**
     * insertExpenseCategory
     * id, code, name, status, currency, debit_account, credit_account description
     * deleteExpenseCategory
     * find by code
     * updateStatus
     * set status find by code
     * updateExpenseCategory
     * code, name, currency, debit_account, credit_account description
     */
    @Override
    public Map<Integer, Object> getRowObjectMap(QueryBuilder builder, ExpenseCategory type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertExpenseCategory".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, Status.Drafted.toString());
            map.put(4, type.getCurrency());
            map.put(5, type.getDebitAccount());
            map.put(6, type.getCreditAccount());
            map.put(7, type.getDescription());
        } else if ("deleteExpenseCategory".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
        } else if ("updateStatus".equals(builder.getQueryName())) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getCode());
        } else if ("updateExpenseCategory".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, type.getCurrency());
            map.put(4, type.getDebitAccount());
            map.put(5, type.getCreditAccount());
            map.put(6, type.getDescription());
            map.put(7, type.getCode());
        }
        return map;
    }

    /**
     * code, name, status, currency, debit account, credit account, description
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"Category Code", "Name", "Status", "Currency", "Debit Account", "Credit Account",
                "Description"};
    }

    /**
     * code, name, status, currency, debit_account, credit_account, description
     */
    @Override
    public ExpenseCategory getExcelType(String type, Cell[] array) {
        ExpenseCategory category = new ExpenseCategory();
        category.setCode(DataConverter.getString(array[0]));
        category.setName(DataConverter.getString(array[1]));
        category.setStatus(DataConverter.getStatus(array[2]));
        category.setCurrency(DataConverter.getString(array[3]));
        category.setDebitAccount(DataConverter.getString(array[4]));
        category.setCreditAccount(DataConverter.getString(array[5]));
        category.setDescription(DataConverter.getString(array[6]));
        return category;
    }

    /**
     * code, name, status, currency, debit_account, credit_account description
     */
    @Override
    public Object[] getExcelRow(String type, ExpenseCategory category) {
        Object[] cellData = new Object[7];
        cellData[0] = category.getCode();
        cellData[1] = category.getName();
        cellData[2] = category.getStatus().toString();
        cellData[3] = category.getCurrency();
        cellData[4] = category.getDebitAccount();
        cellData[5] = category.getCreditAccount();
        cellData[6] = category.getDescription();
        return cellData;
    }

    private List<ExpenseCategory> filteredByStatus(Status status, List<ExpenseCategory> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
