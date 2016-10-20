package excel.accounting.service;

import excel.accounting.db.DataProcessor;
import excel.accounting.db.QueryBuilder;

/**
 * Abstract Service
 * @author Ramesh
 * @since Oct 2016
 */
public abstract class AbstractService {
    private DataProcessor dataProcessor;

    public void setDataProcessor(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    protected DataProcessor getDataProcessor() {
        return dataProcessor;
    }

    protected QueryBuilder getQueryBuilder(String queryName){
        return getDataProcessor().getQueryBuilder(getSqlFileName(), queryName);
    }

    protected abstract String getSqlFileName();


}
