package excel.accounting.service;

import excel.accounting.entity.Asset;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.AssetDao;
import excel.accounting.shared.DataConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Asset Service
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class AssetService extends AbstractService implements ExcelTypeConverter<Asset> {
    private AssetDao assetDao;

    private AssetDao getAssetDao() {
        if (assetDao == null) {
            assetDao = (AssetDao) getBean("assetDao");
        }
        return assetDao;
    }

    private void updateStatus(Status requiredStatus, Status changedStatus, List<Asset> assetList) {
        List<Asset> filteredList = filteredByStatus(requiredStatus, assetList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (Asset asset : filteredList) {
            asset.setStatus(changedStatus);
        }
        /*QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Asset asset : filteredList) {
            transaction.addBatch(getColumnsMap("updateStatus", asset));
        }
        executeBatch(transaction);*/
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
        /*QueryBuilder queryBuilder = getQueryBuilder("insertAsset");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Asset asset : assetList) {
            transaction.addBatch(getColumnsMap("insertAsset", asset));
        }
        executeBatch(transaction);*/
    }

    public void updateAsset(List<Asset> assetList) {
        /*QueryBuilder queryBuilder = getQueryBuilder("updateAsset");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Asset asset : assetList) {
            transaction.addBatch(getColumnsMap("updateAsset", asset));
        }
        executeBatch(transaction);*/
    }

    public void deleteAsset(List<Asset> assetList) {
        List<Asset> filteredList = filteredByStatus(Status.Drafted, assetList);
        if (filteredList.isEmpty()) {
            return;
        }
        /*QueryBuilder queryBuilder = getQueryBuilder("deleteAsset");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Asset asset : filteredList) {
            transaction.addBatch(getColumnsMap("deleteAsset", asset));
        }
        executeBatch(transaction);*/
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
        asset.setReference(DataConverter.getString(array[10]));
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
        cellData[10] = asset.getReference();
        cellData[11] = asset.getCategory();
        return cellData;
    }

    private List<Asset> filteredByStatus(Status status, List<Asset> curList) {
        return curList.stream().filter(cur -> status.equals(cur.getStatus())).collect(Collectors.toList());
    }
}
