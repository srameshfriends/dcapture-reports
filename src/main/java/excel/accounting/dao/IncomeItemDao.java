package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.entity.IncomeItem;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Income Item Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class IncomeItemDao extends AbstractDao<IncomeItem> implements RowColumnsToEntity<IncomeItem> {
    @Override
    protected String getTableName() {
        return "entity.Income_item";
    }

    @Override
    protected String getSqlFileName() {
        return "Income-item";
    }

    @Override
    protected IncomeItem getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    /**
     * id, income_date, description, currency, amount, status
     */
    @Override
    public IncomeItem getEntity(String queryName, Object[] columns) {
        IncomeItem item = new IncomeItem();
        item.setCode((String) columns[0]);
        item.setIncomeDate((Date) columns[1]);
        item.setDescription((String) columns[2]);
        item.setCurrency((String) columns[3]);
        item.setAmount((BigDecimal) columns[4]);
        item.setStatus(DataConverter.getStatus(columns[5]));
        return item;
    }
}
