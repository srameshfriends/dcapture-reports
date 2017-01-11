package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.ExpenseCategory;
import excel.accounting.entity.Status;
import excel.accounting.shared.DataConverter;

import java.util.List;

/**
 * Expense Category Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class ExpenseCategoryDao extends AbstractDao<ExpenseCategory> {
    @Override
    protected String getTableName() {
        return "entity.expense_category";
    }

    @Override
    protected String getSqlFileName() {
        return "expense-category";
    }

    public List<ExpenseCategory> searchExpenseCategory(String searchText, Status... statuses) {
        return null;
    }
}
