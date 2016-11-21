package excel.accounting.service;

import excel.accounting.dao.CurrencyDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.EntityToRowColumns;
import excel.accounting.db.Transaction;
import excel.accounting.entity.ExchangeRate;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.ExchangeRateDao;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.EntitySequence;
import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
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
    private ExchangeRateDao exchangeRateDao;
    private CurrencyDao currencyDao;

    private ExchangeRateDao getExchangeRateDao() {
        if (exchangeRateDao == null) {
            exchangeRateDao = (ExchangeRateDao) getBean("exchangeRateDao");
        }
        return exchangeRateDao;
    }

    private CurrencyDao getCurrencyDao() {
        if (currencyDao == null) {
            currencyDao = (CurrencyDao) getBean("currencyDao");
        }
        return currencyDao;
    }

    @Override
    protected String getSqlFileName() {
        return "exchange-rate";
    }

    private boolean insertValidate(ExchangeRate exchangeRate) {
        return !(exchangeRate.getCode() == null || exchangeRate.getAsOfDate() == null);
    }

    private boolean confirmValidate(ExchangeRate exchangeRate) {
        return !(exchangeRate.getCurrency() == null || 0 < exchangeRate.getUnit() ||
                exchangeRate.getExchangeCurrency() == null || exchangeRate.getSellingRate() == null ||
                exchangeRate.getBuyingRate() == null || exchangeRate.getFetchFrom() == null);
    }

    private void updateStatus(List<ExchangeRate> dataList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExchangeRate item : dataList) {
            transaction.addBatch(getColumnsMap("updateStatus", item));
        }
        executeBatch(transaction);
    }

    public void setAsDrafted(List<ExchangeRate> dataList) {
        List<ExchangeRate> filteredList = filteredByStatus(Status.Confirmed, dataList);
        if (filteredList.isEmpty()) {
            setMessage("Only confirmed exchange rate set as drafted");
            return;
        }
        for (ExchangeRate exchangeRate : filteredList) {
            String errorMessage = getExchangeRateDao().isEntityReferenceUsed(exchangeRate.getCode());
            if (errorMessage != null) {
                setMessage(errorMessage);
                return;
            }
            exchangeRate.setStatus(Status.Drafted);
        }
        updateStatus(filteredList);
    }

    public void setAsConfirmed(List<ExchangeRate> dataList) {
        List<ExchangeRate> filteredList = filteredByStatus(Status.Drafted, dataList);
        if (filteredList.isEmpty()) {
            setMessage("Error : Only drafted exchange rate allowed to confirm");
            return;
        }
        List<ExchangeRate> validList = new ArrayList<>();
        filteredList.stream().filter(this::confirmValidate).forEach(exchangeRate -> {
            exchangeRate.setStatus(Status.Confirmed);
            validList.add(exchangeRate);
        });
        if (validList.isEmpty()) {
            setMessage("Error : valid exchange rate not found");
            return;
        }
        updateStatus(validList);
    }

    public boolean insertExchangeRate(List<ExchangeRate> dataList) {
        setMessage("");
        int sequence = getExchangeRateDao().findLastSequence();
        List<ExchangeRate> validList = new ArrayList<>();
        for (ExchangeRate exchangeRate : dataList) {
            if (insertValidate(exchangeRate)) {
                validList.add(exchangeRate);
            }
        }
        if (validList.isEmpty()) {
            setMessage("Valid exchange rate not found");
            return false;
        }
        List<String> currencyList = new ArrayList<>(); // getCurrencyDao().findCodeList();
        //
        QueryBuilder queryBuilder = getQueryBuilder("insertExchangeRate");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExchangeRate exchangeRate : validList) {
            sequence += 1;
            exchangeRate.setCode(EntitySequence.getExchangeRateCode(sequence));
            if (exchangeRate.getCurrency() != null && !currencyList.contains(exchangeRate.getCurrency())) {
                exchangeRate.setCurrency(null);
            }
            transaction.addBatch(getColumnsMap("insertExchangeRate", exchangeRate));
        }
        executeBatch(transaction);
        return true;
    }

    public void updateExchangeRate(List<ExchangeRate> itemList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateExchangeRate");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExchangeRate item : itemList) {
            transaction.addBatch(getColumnsMap("updateExchangeRate", item));
        }
        executeBatch(transaction);
    }

    public void deleteExchangeRate(List<ExchangeRate> itemList) {
        List<ExchangeRate> filteredList = filteredByStatus(Status.Drafted, itemList);
        if (filteredList.isEmpty()) {
            setMessage("Drafted exchange rate not found to delete");
            return;
        }
        QueryBuilder queryBuilder = getQueryBuilder("deleteExchangeRate");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (ExchangeRate item : filteredList) {
            transaction.addBatch(getColumnsMap("deleteExchangeRate", item));
        }
        executeBatch(transaction);
    }

    @Override
    public Map<Integer, Object> getColumnsMap(final String queryName, ExchangeRate type) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertExchangeRate".equals(queryName)) {
            map.put(1, type.getCode());
            map.put(2, type.getFetchFrom());
            map.put(3, type.getAsOfDate());
            map.put(4, type.getCurrency());
            map.put(5, type.getExchangeCurrency());
            map.put(6, type.getUnit());
            map.put(7, type.getSellingRate());
            map.put(8, type.getBuyingRate());
            map.put(9, Status.Drafted.toString());
        } else if ("deleteExchangeRate".equals(queryName)) {
            map.put(1, type.getCode());
        } else if ("updateStatus".equals(queryName)) {
            map.put(1, type.getStatus().toString());
            map.put(2, type.getCode());
        } else if ("updateExchangeRate".equals(queryName)) {
            map.put(1, type.getUnit());
            map.put(2, type.getSellingRate());
            map.put(3, type.getBuyingRate());
            map.put(4, type.getCode());
        }
        return map;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"Code", "Reference From", "Date", "Currency", "Exchange Currency", "Unit", "Selling Rate",
                "Buying Rate", "Status"};
    }

    @Override
    public ExchangeRate getExcelType(String type, Cell[] array) {
        ExchangeRate item = new ExchangeRate();
        item.setCode(DataConverter.getString(array[0]));
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

    @Override
    public Object[] getExcelRow(String type, ExchangeRate item) {
        Object[] cellData = new Object[9];
        cellData[0] = item.getCode();
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
