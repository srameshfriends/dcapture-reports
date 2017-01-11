package excel.accounting.service;

import excel.accounting.db.*;
import excel.accounting.shared.AbstractControl;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public abstract class AbstractService extends AbstractControl {

    protected abstract String getSqlFileName();

    private void executeBatch(List<SqlQuery> queryList) {
        SqlTransaction transaction = getSqlProcessor().createSqlTransaction();
        try {
            transaction.executeBatch(queryList);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void executeCommit(List<SqlQuery> queryList) {
        SqlTransaction transaction = getSqlProcessor().createSqlTransaction();
        try {
            transaction.executeCommit(queryList);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void insert(List<?> dataList) {
        SqlProcessor forwardTool = getApplicationControl().getSqlProcessor();
        executeCommit(dataList.stream().map(forwardTool::insertQuery).collect(Collectors.toList()));
    }

    protected void update(List<?> dataList) {
        SqlProcessor forwardTool = getApplicationControl().getSqlProcessor();
        executeBatch(dataList.stream().map(forwardTool::updateQuery).collect(Collectors.toList()));
    }

    protected void delete(List<?> dataList) {
        SqlProcessor forwardTool = getApplicationControl().getSqlProcessor();
        executeBatch(dataList.stream().map(forwardTool::deleteQuery).collect(Collectors.toList()));
    }
}
