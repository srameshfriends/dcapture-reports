package excel.accounting.entity;

import excel.accounting.model.EntityRow;

import java.math.BigDecimal;

/**
 * Currency
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class Currency extends EntityRow {
    private String code, name, symbol;
    private Status status;
    private int decimalPrecision;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getDecimalPrecision() {
        return decimalPrecision;
    }

    public void setDecimalPrecision(int decimalPrecision) {
        this.decimalPrecision = decimalPrecision;
    }
}
