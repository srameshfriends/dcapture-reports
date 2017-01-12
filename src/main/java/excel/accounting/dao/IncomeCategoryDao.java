package excel.accounting.dao;

import excel.accounting.entity.IncomeCategory;

/**
 * Income Category Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class IncomeCategoryDao extends AbstractDao<IncomeCategory> {
    @Override
    protected String getTableName() {
        return "income_category";
    }
}
