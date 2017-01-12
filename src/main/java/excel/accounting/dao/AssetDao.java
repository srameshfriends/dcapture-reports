package excel.accounting.dao;

import excel.accounting.entity.Asset;

/**
 * Asset Dao
 */
public class AssetDao extends AbstractDao<Asset> {
    @Override
    protected String getTableName() {
        return "asset";
    }
}
