package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.Payment;
import excel.accounting.entity.Status;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Payment Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class PaymentDao extends AbstractDao<Payment> {
    @Override
    protected String getTableName() {
        return "entity.payment";
    }

    @Override
    protected String getSqlFileName() {
        return "payment";
    }

    public List<Payment> searchPayment(String searchText, Status status) {
        return null;
    }
}
