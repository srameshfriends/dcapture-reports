package excel.accounting.db;

/**
 * Has Data Processor
 */
public interface HasDataProcessor {

    DataReader getDataReader();

    Transaction createTransaction();
}
