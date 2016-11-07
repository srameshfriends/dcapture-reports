package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.Payment;
import excel.accounting.entity.Status;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

/**
 * Payment Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class PaymentDao extends AbstractDao<Payment> implements RowColumnsToEntity<Payment> {
    @Override
    protected String getTableName() {
        return "entity.payment";
    }

    @Override
    protected String getSqlFileName() {
        return "payment";
    }

    @Override
    protected Payment getReferenceRow(String code) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, code);
        return getDataReader().findSingleRow(builder, this);
    }

    @Override
    public Payment getEntity(String queryName, Object[] columns) {
        Payment payment = new Payment();
        payment.setCode((String) columns[0]);
        payment.setDataType((String) columns[1]);
        payment.setDataCode((String) columns[2]);
        payment.setInstalment((Integer) columns[3]);
        payment.setPaymentDate((Date) columns[4]);
        payment.setDescription((String) columns[5]);
        payment.setCurrency((String) columns[6]);
        payment.setAmount((BigDecimal) columns[7]);
        payment.setAccount((String) columns[8]);
        payment.setExchangeRate((BigDecimal) columns[9]);
        payment.setExchangeUnit((Integer) columns[10]);
        payment.setStatus(DataConverter.getStatus(columns[11]));
        return payment;
    }

    public List<Payment> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<Payment> searchPayment(String searchText, Status status) {
        QueryBuilder queryBuilder = getQueryBuilder("searchPayment");
        InClauseQuery statusQuery = new InClauseQuery(status);
        queryBuilder.addInClauseQuery("$status", statusQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<String> findCodeList() {
        QueryBuilder queryBuilder = getQueryBuilder("findCodeList");
        return getDataReader().findString(queryBuilder);
    }
}
