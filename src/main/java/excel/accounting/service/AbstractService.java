package excel.accounting.service;

import excel.accounting.db.*;

/**
 * Abstract Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public abstract class AbstractService implements HasDataProcessor {
    private DataProcessor dataProcessor;
    private DataReader dataReader;

    @Override
    public void setDataProcessor(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
        dataReader = new DataReader(dataProcessor);
    }

    @Override
    public DataReader getDataReader() {
        return dataReader;
    }

    @Override
    public Transaction createTransaction() {
        return new Transaction(dataProcessor);
    }

    protected abstract String getSqlFileName();

    protected QueryBuilder getQueryBuilder(String queryName){
        return dataProcessor.getQueryBuilder(getSqlFileName(), queryName);
    }
}
