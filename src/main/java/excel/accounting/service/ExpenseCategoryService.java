package excel.accounting.service;

import excel.accounting.db.*;
import excel.accounting.entity.ExpenseCategory;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.ExpenseCategoryDao;
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
        EntityToRowColumns<ExpenseCategory>, ExcelTypeConverter<ExpenseCategory> {
    private ExpenseCategoryDao expenseCategoryDao;

    private ExpenseCategoryDao getExpenseCategoryDao() {
        if (expenseCategoryDao == null) {
            expenseCategoryDao = (ExpenseCategoryDao) getBean("expenseCategoryDao");
        }
        return expenseCategoryDao;
    }

    @Override
    protected String getSqlFileName() {
        return "expense-category";
    }

    public List<ExpenseCategory> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, getExpenseCategoryDao());
    }

    public List<ExpenseCategory> searchExpenseCategory(String searchText, Status... statuses) {
        InClauseQuery inClauseQuery = new InClauseQuery(statuses);
        QueryBuilder queryBuilder = getQueryBuilder("searchExpenseCategory");
        queryBuilder.addInClauseQuery("$status", inClauseQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, getExpenseCategoryDao());
    }

    public List<String> findCodeList() {
        QueryBuilder queryBuilder = getQueryBuilder("findCodeList");
        return getDataReader().findString(queryBuilder);
    }

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
        for (ExpenseCategory category : filteredList) {
            transaction.addBatch(getColumnsMap("updateStatus", category));
        }
        executeBatch(transaction);
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
            transaction.addBatch(getColumnsMap("insertExpenseCategory", category));
        }
        executeBatch(transaction);
    }

    public void updateExpenseCategory(List<ExpenseCategory> categoryList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateExpenseCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExpenseCategory category : categoryList) {
            transaction.addBatch(getColumnsMap("updateExpenseCategory", category));
        }
        executeBatch(transaction);
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
            transaction.addBatch(getColumnsMap("deleteExpenseCategory", category));
        }
        executeBatch(transaction);
    }

    /**
     * insertExpenseCategory
     * code, name, status, currency, expense_account description
     * deleteExpenseCategory
     * find by code
     * updateStatus
     * set status find by code
     * updateExpenseCategory
     * code, name, currency, expense_account description
     */
    @Override
    public Map<Integer, Object> getColumnsMap(final String queryName, ExpenseCategory type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertExpenseCategory".equals(queryName)) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, Status.Drafted.toString());
            map.put(4, type.getCurrency());
            map.put(5, type.getExpenseAccount());
            map.put(6, type.getDescription());
        } else if ("deleteExpenseCategory".equals(queryName)) {
            map.put(1, type.getCode());
        } else if ("updateStatus".equals(queryName)) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getCode());
        } else if ("updateExpenseCategory".equals(queryName)) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, type.getCurrency());
            map.put(4, type.getExpenseAccount());
            map.put(5, type.getDescription());
            map.put(6, type.getCode());
        }
        return map;
    }

    /**
     * code, name, status, currency, expense account, description
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"Category Code", "Name", "Status", "Currency", "Expense Account", "Description"};
    }

    /**
     * code, name, status, currency, expense_account, description
     */
    @Override
    public ExpenseCategory getExcelType(String type, Cell[] array) {
        ExpenseCategory category = new ExpenseCategory();
        category.setCode(DataConverter.getString(array[0]));
        category.setName(DataConverter.getString(array[1]));
        category.setStatus(DataConverter.getStatus(array[2]));
        category.setCurrency(DataConverter.getString(array[3]));
        category.setExpenseAccount(DataConverter.getString(array[4]));
        category.setDescription(DataConverter.getString(array[5]));
        return category;
    }

    /**
     * code, name, status, currency, expense_account description
     */
    @Override
    public Object[] getExcelRow(String type, ExpenseCategory category) {
        Object[] cellData = new Object[6];
        cellData[0] = category.getCode();
        cellData[1] = category.getName();
        cellData[2] = category.getStatus().toString();
        cellData[3] = category.getCurrency();
        cellData[4] = category.getExpenseAccount();
        cellData[5] = category.getDescription();
        return cellData;
    }

    private List<ExpenseCategory> filteredByStatus(Status status, List<ExpenseCategory> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
