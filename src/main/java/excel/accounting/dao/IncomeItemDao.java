package excel.accounting.dao;

import excel.accounting.entity.IncomeItem;

/**
 * Income Item Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class IncomeItemDao extends AbstractDao<IncomeItem> {
    @Override
    protected String getTableName() {
        return "income_item";
    }
}
