package excel.accounting.entity;

import excel.accounting.db.ColumnIndex;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * Currency
 *
 * @author Ramesh
 * @since Oct, 2016
 */

@Table(name = "currency")
@ColumnIndex(columns = {"symbol", "decimalPrecision"})
public class Currency extends MasterRecord {
    @Column(name = "symbol")
    private String symbol;
    @Column(name = "decimal_precision")
    private int decimalPrecision;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getDecimalPrecision() {
        return decimalPrecision;
    }

    public void setDecimalPrecision(int decimalPrecision) {
        this.decimalPrecision = decimalPrecision;
    }
}
