package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.*;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Expense Item Dao
 */
public class ExpenseItemDao extends AbstractDao<ExpenseItem> implements RowColumnsToEntity<ExpenseItem> {
    @Override
    protected String getTableName() {
        return "expense_item";
    }

    @Override
    protected String getSqlFileName() {
        return "expense-item";
    }

    @Override
    protected ExpenseItem getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    @Override
    public ExpenseItem getEntity(String queryName, Object[] columns) {
        ExpenseItem item = new ExpenseItem();
        item.setCode((String) columns[0]);
        item.setGroupCode((String) columns[1]);
        item.setExpenseDate((Date) columns[2]);
        item.setReferenceNumber((String) columns[3]);
        item.setDescription((String) columns[4]);
        item.setCurrency((String) columns[5]);
        item.setAmount((BigDecimal) columns[6]);
        item.setStatus(DataConverter.getStatus(columns[7]));
        item.setExpenseCategory((String) columns[8]);
        item.setAccount((String) columns[9]);
        item.setPaidStatus(DataConverter.getPaidStatus(columns[10]));
        return item;
    }

    public List<ExpenseItem> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<ExpenseItem> searchExpenseItems(String searchText, Status[] status, PaidStatus[] paidStatuses) {
        QueryBuilder queryBuilder = getQueryBuilder("searchExpenseItems");
        //
        InClauseQuery statusQuery = new InClauseQuery(status);
        queryBuilder.addInClauseQuery("$status", statusQuery);
        //
        InClauseQuery accountTypeQuery = null;
        if(paidStatuses != null) {
            accountTypeQuery = new InClauseQuery(paidStatuses);
        }
        queryBuilder.addInClauseQuery("$paidStatus", accountTypeQuery);
        //
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "group_code", "reference_number", "description");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public int findLastSequence() {
        QueryBuilder builder = getQueryBuilder("findLastSequence");
        String value = (String) getDataReader().findSingleObject(builder);
        return value == null ? 0 : DataConverter.getInteger(value.substring(2));
    }
}
