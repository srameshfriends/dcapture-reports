package excel.accounting.service;

import excel.accounting.db.*;
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
public class IncomeCategoryService extends AbstractService implements ExcelTypeConverter<IncomeCategory> {

    @Override
    protected String getSqlFileName() {
        return "income-category";
    }

    public List<IncomeCategory> searchIncomeCategory(String searchText, Status... statuses) {
       /* ClauseQuery clauseQuery = new ClauseQuery("status");
        clauseQuery.addParameter(statuses);
        QueryBuilder queryBuilder = getQueryBuilder("searchIncomeCategory");
        queryBuilder.addInClauseQuery("$status", clauseQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);*/
        return null;
    }

    private void updateStatus(Status requiredStatus, Status changedStatus, List<IncomeCategory> categoryList) {
        List<IncomeCategory> filteredList = filteredByStatus(requiredStatus, categoryList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (IncomeCategory category : filteredList) {
            category.setStatus(changedStatus);
        }
        /*QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeCategory category : filteredList) {
            transaction.addBatch(getColumnsMap("updateStatus", category));
        }
        executeBatch(transaction);*/
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
        /*QueryBuilder queryBuilder = getQueryBuilder("insertIncomeCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeCategory category : categoryList) {
            transaction.addBatch(getColumnsMap("insertIncomeCategory", category));
        }
        executeBatch(transaction);*/
    }

    public void updateIncomeCategory(List<IncomeCategory> categoryList) {
        /*QueryBuilder queryBuilder = getQueryBuilder("updateIncomeCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeCategory category : categoryList) {
            transaction.addBatch(getColumnsMap("updateIncomeCategory", category));
        }
        executeBatch(transaction);*/
    }

    public void deleteIncomeCategory(List<IncomeCategory> categoryList) {
        List<IncomeCategory> filteredList = filteredByStatus(Status.Drafted, categoryList);
        if (filteredList.isEmpty()) {
            return;
        }
        /*QueryBuilder queryBuilder = getQueryBuilder("deleteIncomeCategory");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (IncomeCategory category : filteredList) {
            transaction.addBatch(getColumnsMap("deleteIncomeCategory", category));
        }
        executeBatch(transaction);*/
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
        cellData[5] = category.getDescription();
        return cellData;
    }

    private List<IncomeCategory> filteredByStatus(Status status, List<IncomeCategory> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
