package excel.accounting.db;

/**
 * Has Data Processor
 */
public interface HasDataProcessor {
    void setDataProcessor(DataProcessor dataProcessor);

    DataReader getDataReader();

    Transaction createTransaction();
}
