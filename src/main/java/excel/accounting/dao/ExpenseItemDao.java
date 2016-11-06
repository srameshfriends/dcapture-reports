package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.entity.ExpenseItem;
import excel.accounting.shared.DataConverter;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

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
        item.setExpenseDate((Date) columns[1]);
        item.setReferenceNumber((String) columns[2]);
        item.setDescription((String) columns[3]);
        item.setCurrency((String) columns[4]);
        item.setAmount((BigDecimal) columns[5]);
        item.setStatus(DataConverter.getStatus(columns[6]));
        item.setExpenseCategory((String) columns[7]);
        item.setExpenseAccount((String) columns[8]);
        item.setPaid((Boolean) columns[9]);
        return item;
    }

    public int findLastSequence() {
        QueryBuilder builder = getQueryBuilder("findLastSequence");
        String value = (String) getDataReader().findSingleObject(builder);
        return value == null ? 0 : DataConverter.getInteger(value.substring(2));
    }
}
