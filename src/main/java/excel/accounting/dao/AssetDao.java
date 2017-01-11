package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.entity.Asset;

/**
 * Asset Dao
 */
public class AssetDao extends AbstractDao<Asset> {
    @Override
    protected String getTableName() {
        return "asset";
    }

    @Override
    protected String getSqlFileName() {
        return "asset";
    }
}
