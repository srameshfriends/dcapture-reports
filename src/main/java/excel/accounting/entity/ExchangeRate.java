package excel.accounting.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Exchange Rate
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ExchangeRate {
    private String code, fetchFrom, currency, exchangeCurrency;
    private Date asOfDate;
    private Status status;
    private int unit;
    private BigDecimal sellingRate, buyingRate;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFetchFrom() {
        return fetchFrom;
    }

    public void setFetchFrom(String fetchFrom) {
        this.fetchFrom = fetchFrom;
    }

    public Date getAsOfDate() {
        return asOfDate;
    }

    public void setAsOfDate(Date asOfDate) {
        this.asOfDate = asOfDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExchangeCurrency() {
        return exchangeCurrency;
    }

    public void setExchangeCurrency(String exchangeCurrency) {
        this.exchangeCurrency = exchangeCurrency;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public BigDecimal getSellingRate() {
        return sellingRate;
    }

    public void setSellingRate(BigDecimal sellingRate) {
        this.sellingRate = sellingRate;
    }

    public BigDecimal getBuyingRate() {
        return buyingRate;
    }

    public void setBuyingRate(BigDecimal buyingRate) {
        this.buyingRate = buyingRate;
    }
}
