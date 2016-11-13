package excel.accounting.entity;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Exchange Rate
 *
 * @author Ramesh
 * @since Oct, 2016
 */
@Table(name = "exchange_rate")
public class ExchangeRate extends DocumentRecord {

    @Column(name = "fetch_from")
    private String fetchFrom;

    @JoinColumn(name = "currency", table = "currency")
    private String currency;

    @JoinColumn(name = "exchange_currency", table = "currency")
    private String exchangeCurrency;

    @Column(name = "asof_date")
    private Date asOfDate;

    private int unit;

    @Column(name = "selling_rate")
    private BigDecimal sellingRate;

    @Column(name = "buying_rate")
    private BigDecimal buyingRate;

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
