package excel.accounting.service;

import excel.accounting.db.QueryBuilder;
import excel.accounting.db.EntityToRowColumns;
import excel.accounting.db.Transaction;
import excel.accounting.entity.ExchangeRate;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.ExchangeRateDao;
import excel.accounting.shared.DataConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exchange Rate Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public class ExchangeRateService extends AbstractService implements
        EntityToRowColumns<ExchangeRate>, ExcelTypeConverter<ExchangeRate> {
    private ExchangeRateDao exchangeRateRelation;

    public ExchangeRateDao getExchangeRateDao() {
        if (exchangeRateRelation == null) {
            exchangeRateRelation = (ExchangeRateDao) getDao("exchangeRateDao");
        }
        return exchangeRateRelation;
    }

    @Override
    protected String getSqlFileName() {
        return "exchange-rate";
    }

    public List<ExchangeRate> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, getExchangeRateDao());
    }

    public List<Integer> findIdList() {
        QueryBuilder queryBuilder = getQueryBuilder("findIdList");
        return getDataReader().findInteger(queryBuilder);
    }

    private void updateStatus(Status requiredStatus, Status changedStatus, List<ExchangeRate> itemList) {
        List<ExchangeRate> filteredList = filteredByStatus(requiredStatus, itemList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (ExchangeRate item : filteredList) {
            item.setStatus(changedStatus);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExchangeRate item : filteredList) {
            transaction.addBatch(getColumnsMap("updateStatus", item));
        }
        transaction.executeBatch();
    }

    public void setAsDrafted(List<ExchangeRate> itemList) {
        updateStatus(Status.Confirmed, Status.Drafted, itemList);
    }

    public void setAsConfirmed(List<ExchangeRate> itemList) {
        updateStatus(Status.Drafted, Status.Confirmed, itemList);
    }

    public void setAsClosed(List<ExchangeRate> itemList) {
        updateStatus(Status.Confirmed, Status.Closed, itemList);
    }

    public void insertExchangeRate(List<ExchangeRate> itemList) {
        QueryBuilder queryBuilder = getQueryBuilder("insertExchangeRate");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExchangeRate item : itemList) {
            transaction.addBatch(getColumnsMap("insertExchangeRate", item));
        }
        transaction.executeBatch();
    }

    /*
    * expense_date, description, currency, amount
    */
    public void updateExchangeRate(List<ExchangeRate> itemList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateExchangeRate");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExchangeRate item : itemList) {
            transaction.addBatch(getColumnsMap("updateExchangeRate", item));
        }
        transaction.executeBatch();
    }

    public void deleteExchangeRate(List<ExchangeRate> itemList) {
        List<ExchangeRate> filteredList = filteredByStatus(Status.Drafted, itemList);
        if (filteredList.isEmpty()) {
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deleteExchangeRate");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExchangeRate item : filteredList) {
            transaction.addBatch(getColumnsMap("deleteExchangeRate", item));
        }
        transaction.executeBatch();
    }

    /**
     * insertExchangeRate
     * fetch_from, asof_date, currency, exchange_currency, unit, selling_rate, buying_rate, status
     * deleteExchangeRate
     * find by id
     * updateStatus
     * set status find by id
     * updateExchangeRate
     * fetch_from, asof_date, currency, exchange_currency, unit, selling_rate, buying_rate By Id
     */
    @Override
    public Map<Integer, Object> getColumnsMap(final String queryName, ExchangeRate type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertExchangeRate".equals(queryName)) {
            map.put(1, type.getFetchFrom());
            map.put(2, type.getAsOfDate());
            map.put(3, type.getCurrency());
            map.put(4, type.getExchangeCurrency());
            map.put(5, type.getUnit());
            map.put(6, type.getSellingRate());
            map.put(7, type.getBuyingRate());
            map.put(8, Status.Drafted.toString());
        } else if ("deleteExchangeRate".equals(queryName)) {
            map.put(1, type.getId());
        } else if ("updateStatus".equals(queryName)) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getId());
        } else if ("updateExchangeRate".equals(queryName)) {
            map.put(1, type.getFetchFrom());
            map.put(2, type.getAsOfDate());
            map.put(3, type.getCurrency());
            map.put(4, type.getExchangeCurrency());
            map.put(5, type.getUnit());
            map.put(6, type.getSellingRate());
            map.put(7, type.getBuyingRate());
            map.put(8, type.getId());
        }
        return map;
    }

    /**
     * code, fetch_from, asof_date, currency, exchange_currency, unit, selling_rate, buying_rate, status
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"Code", "Reference From", "Date", "Currency", "Exchange Currency", "Unit", "Selling Rate",
                "Buying Rate", "Status"};
    }

    /**
     * id, fetch_from, asof_date, currency, exchange_currency, unit, selling_rate, buying_rate, status
     */
    @Override
    public ExchangeRate getExcelType(String type, Cell[] array) {
        ExchangeRate item = new ExchangeRate();
        item.setId(DataConverter.getInteger(array[0]));
        item.setFetchFrom(DataConverter.getString(array[1]));
        item.setAsOfDate(DataConverter.getDate(array[2]));
        item.setCurrency(DataConverter.getString(array[3]));
        item.setExchangeCurrency(DataConverter.getString(array[4]));
        item.setUnit(DataConverter.getInteger(array[5]));
        item.setSellingRate(DataConverter.getBigDecimal(array[6]));
        item.setBuyingRate(DataConverter.getBigDecimal(array[7]));
        item.setStatus(DataConverter.getStatus(array[8]));
        return item;
    }

    /**
     * id, expense_date, description, currency, amount, status
     */
    @Override
    public Object[] getExcelRow(String type, ExchangeRate item) {
        Object[] cellData = new Object[9];
        cellData[0] = item.getId();
        cellData[1] = item.getFetchFrom();
        cellData[2] = item.getAsOfDate();
        cellData[3] = item.getCurrency();
        cellData[4] = item.getExchangeCurrency();
        cellData[5] = item.getUnit();
        cellData[6] = item.getSellingRate();
        cellData[7] = item.getBuyingRate();
        cellData[8] = item.getStatus().toString();
        return cellData;
    }

    private List<ExchangeRate> filteredByStatus(Status status, List<ExchangeRate> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
