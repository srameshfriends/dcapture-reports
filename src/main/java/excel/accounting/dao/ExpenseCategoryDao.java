package excel.accounting.dao;

import excel.accounting.entity.ExpenseCategory;
import excel.accounting.entity.Status;

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
        return "expense_category";
    }

    public List<ExpenseCategory> searchExpenseCategory(String searchText, Status... statuses) {
        return null;
    }
}
