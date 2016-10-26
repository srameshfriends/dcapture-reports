package excel.accounting.service;

import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowTypeConverter;
import excel.accounting.db.Transaction;
import excel.accounting.entity.Asset;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.shared.DataConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Asset Service
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class AssetService extends AbstractService implements RowTypeConverter<Asset>, ExcelTypeConverter<Asset> {

    @Override
    protected String getSqlFileName() {
        return "asset";
    }

    public List<Asset> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<String> findCodeList() {
        QueryBuilder queryBuilder = getQueryBuilder("findCodeList");
        return getDataReader().findString(queryBuilder);
    }

    private void updateStatus(Status requiredStatus, Status changedStatus, List<Asset> assetList) {
        List<Asset> filteredList = filteredByStatus(requiredStatus, assetList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (Asset asset : filteredList) {
            asset.setStatus(changedStatus);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Asset asset : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, asset));
        }
        transaction.executeBatch();
    }

    public void setAsDrafted(List<Asset> assetList) {
        updateStatus(Status.Confirmed, Status.Drafted, assetList);
    }

    public void setAsConfirmed(List<Asset> assetList) {
        updateStatus(Status.Drafted, Status.Confirmed, assetList);
    }

    public void setAsClosed(List<Asset> assetList) {
        updateStatus(Status.Confirmed, Status.Closed, assetList);
    }

    public void insertAsset(List<Asset> assetList) {
        QueryBuilder queryBuilder = getQueryBuilder("insertAsset");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Asset asset : assetList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, asset));
        }
        transaction.executeBatch();
    }

    public void updateAsset(List<Asset> assetList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateAsset");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Asset asset : assetList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, asset));
        }
        transaction.executeBatch();
    }

    public void deleteAsset(List<Asset> assetList) {
        List<Asset> filteredList = filteredByStatus(Status.Drafted, assetList);
        if (filteredList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deleteAsset");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Asset asset : filteredList) {
            transaction.addBatch(getRowObjectMap(queryBuilder, asset));
        }
        transaction.executeBatch();
    }

    /**
     * id, code, name, description, asset_type, start_date, end_date, currency, cost, status, units, reference_number,
     * category
     */
    @Override
    public Asset getRowType(QueryBuilder builder, Object[] objects) {
        Asset asset = new Asset();
        asset.setId((Integer) objects[0]);
        asset.setCode((String) objects[1]);
        asset.setName((String) objects[2]);
        asset.setDescription((String) objects[3]);
        asset.setAssetType((String) objects[4]);
        asset.setStartDate((Date) objects[5]);
        asset.setEndDate((Date) objects[6]);
        asset.setCurrency((String) objects[7]);
        asset.setCost((BigDecimal) objects[8]);
        asset.setStatus(DataConverter.getStatus(objects[9]));
        asset.setUnits((BigDecimal) objects[10]);
        asset.setReferenceNumber((String) objects[11]);
        asset.setCategory((String) objects[12]);
        return asset;
    }

    /**
     * insertAsset
     * code, name, description, asset_type, start_date, end_date, currency, cost, status, units, reference_number,
     * category
     * deleteAsset
     * find by code
     * updateStatus
     * set status find by code
     * updateAsset
     * code, name, description, asset_type, start_date, end_date, currency, cost, status, units, reference_number,
     * category
     */
    @Override
    public Map<Integer, Object> getRowObjectMap(QueryBuilder builder, Asset type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertAsset".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, type.getDescription());
            map.put(4, type.getAssetType());
            map.put(5, type.getStartDate());
            map.put(6, type.getEndDate());
            map.put(7, type.getCurrency());
            map.put(8, type.getCost());
            map.put(9, Status.Drafted.toString());
            map.put(10, type.getUnits());
            map.put(11, type.getReferenceNumber());
            map.put(12, type.getCategory());
        } else if ("deleteAsset".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
        } else if ("updateAsset".equals(builder.getQueryName())) {
            map.put(1, type.getCode());
            map.put(2, type.getName());
            map.put(3, type.getDescription());
            map.put(4, type.getAssetType());
            map.put(5, type.getStartDate());
            map.put(6, type.getEndDate());
            map.put(7, type.getCurrency());
            map.put(8, type.getCost());
            map.put(9, Status.Drafted.toString());
            map.put(10, type.getUnits());
            map.put(11, type.getReferenceNumber());
            map.put(12, type.getCategory());
            map.put(13, type.getCode());
        } else if ("updateStatus".equals(builder.getQueryName())) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getCode());
        }
        return map;
    }

    /**
     * code, name, description, asset_type, start_date, end_date, currency, cost, status, units, reference_number,
     * category
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"Code", "Name", "Description", "Asset Type", "Start Date", "End Date", "Currency", "Cost",
                "Status", "Units", "Reference Number", "Category"};
    }

    /**
     * code, name, description, asset_type, start_date, end_date, currency, cost, status, units, reference_number,
     * category
     */
    @Override
    public Asset getExcelType(String type, Cell[] array) {
        Asset asset = new Asset();
        asset.setCode(DataConverter.getString(array[0]));
        asset.setName(DataConverter.getString(array[1]));
        asset.setDescription(DataConverter.getString(array[2]));
        asset.setAssetType(DataConverter.getString(array[3]));
        asset.setStartDate(DataConverter.getDate(array[4]));
        asset.setEndDate(DataConverter.getDate(array[5]));
        asset.setCurrency(DataConverter.getString(array[6]));
        asset.setCost(DataConverter.getBigDecimal(array[7]));
        asset.setStatus(DataConverter.getStatus(array[8]));
        asset.setUnits(DataConverter.getBigDecimal(array[9]));
        asset.setReferenceNumber(DataConverter.getString(array[10]));
        asset.setCategory(DataConverter.getString(array[11]));
        return asset;
    }

    /**
     * code, name, description, asset_type, start_date, end_date, currency, cost, status, units, reference_number,
     * category
     */
    @Override
    public Object[] getExcelRow(String type, Asset asset) {
        Object[] cellData = new Object[12];
        cellData[0] = asset.getCode();
        cellData[1] = asset.getName();
        cellData[2] = asset.getDescription();
        cellData[3] = asset.getAssetType();
        cellData[4] = asset.getStartDate();
        cellData[5] = asset.getEndDate();
        cellData[6] = asset.getCurrency();
        cellData[7] = asset.getCost();
        cellData[8] = asset.getStatus().toString();
        cellData[9] = asset.getUnits();
        cellData[10] = asset.getReferenceNumber();
        cellData[11] = asset.getCategory();
        return cellData;
    }

    private List<Asset> filteredByStatus(Status status, List<Asset> curList) {
        return curList.stream().filter(cur -> status.equals(cur.getStatus())).collect(Collectors.toList());
    }
}
