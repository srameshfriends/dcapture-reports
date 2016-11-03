package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.entity.ExpenseItem;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Expense Item Dao
 */
public class ExpenseItemDao extends AbstractDao<ExpenseItem> implements RowColumnsToEntity<ExpenseItem> {

    public ExpenseItemDao() {
    }

    @Override
    protected ExpenseItem getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("expense-item", "findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    @Override
    public ExpenseItem getEntity(String queryName, Object[] columns) {
        ExpenseItem item = new ExpenseItem();
        item.setExpenseCode((String) columns[0]);
        item.setExpenseDate((Date) columns[1]);
        item.setReferenceNumber((String) columns[2]);
        item.setDescription((String) columns[3]);
        item.setCurrency((String) columns[4]);
        item.setAmount((BigDecimal) columns[5]);
        item.setStatus(DataConverter.getStatus(columns[6]));
        item.setExpenseCategory((String) columns[7]);
        item.setExpenseAccount((String) columns[8]);
        return item;
    }
}
