package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.entity.ExpenseItem;
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

    public int findLastSequence() {
        QueryBuilder builder = getQueryBuilder("findLastSequence");
        String value = (String) getDataReader().findSingleObject(builder);
        return value == null ? 0 : DataConverter.getInteger(value.substring(2));
    }
}
