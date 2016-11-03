package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.entity.Asset;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Asset Dao
 */
public class AssetDao extends AbstractDao<Asset> implements RowColumnsToEntity<Asset> {

    @Override
    protected Asset getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("asset", "findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    @Override
    public Asset getEntity(String queryName, Object[] columns) {
        Asset asset = new Asset();
        asset.setCode((String) columns[0]);
        asset.setName((String) columns[1]);
        asset.setDescription((String) columns[2]);
        asset.setAssetType((String) columns[3]);
        asset.setStartDate((Date) columns[4]);
        asset.setEndDate((Date) columns[5]);
        asset.setCurrency((String) columns[6]);
        asset.setCost((BigDecimal) columns[7]);
        asset.setStatus(DataConverter.getStatus(columns[8]));
        asset.setUnits((BigDecimal) columns[9]);
        asset.setReferenceNumber((String) columns[10]);
        asset.setCategory((String) columns[11]);
        return asset;
    }
}
