package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.entity.ExpenseCategory;
import excel.accounting.shared.DataConverter;

/**
 * Expense Category Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class ExpenseCategoryDao extends AbstractDao<ExpenseCategory> implements //
        RowColumnsToEntity<ExpenseCategory> {
    @Override
    protected String getTableName() {
        return "expense_category";
    }

    @Override
    protected String getSqlFileName() {
        return "expense-category";
    }

    @Override
    protected ExpenseCategory getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    /**
     * code, name, status, currency, expense_account description
     */
    @Override
    public ExpenseCategory getEntity(String queryName, Object[] columns) {
        ExpenseCategory category = new ExpenseCategory();
        category.setCode((String) columns[0]);
        category.setName((String) columns[1]);
        category.setStatus(DataConverter.getStatus(columns[2]));
        category.setCurrency((String) columns[3]);
        category.setExpenseAccount((String) columns[4]);
        category.setDescription((String) columns[5]);
        return category;
    }
}
