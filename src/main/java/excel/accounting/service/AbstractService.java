package excel.accounting.service;

import excel.accounting.db.*;
import excel.accounting.shared.AbstractControl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public abstract class AbstractService<E> extends AbstractControl {

    protected void executeBatch(List<SqlQuery> queryList) {
        try {
            SqlTransaction transaction = getSqlTransaction();
            transaction.executeBatch(queryList);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void executeCommit(List<SqlQuery> queryList) {
        try {
            SqlTransaction transaction = getSqlTransaction();
            transaction.executeCommit(queryList);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void executeBatch(SqlQuery query) {
        try {
            SqlTransaction transaction = getSqlTransaction();
            transaction.executeBatch(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void executeCommit(SqlQuery query) {
        try {
            SqlTransaction transaction = getSqlTransaction();
            transaction.executeCommit(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void insert(E object) {
        try {
            SqlTransaction transaction = getSqlTransaction();
            SqlQuery query = transaction.insertQuery(object);
            executeCommit(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void insertList(List<E> insertList) {
        try {
            SqlTransaction transaction = getSqlTransaction();
            List<SqlQuery> queryList = new ArrayList<>();
            for (Object object : insertList) {
                queryList.add(transaction.insertQuery(object));
            }
            executeCommit(queryList);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void delete(E object) {
        try {
            SqlTransaction transaction = getSqlTransaction();
            SqlQuery sqlQuery = transaction.deleteQuery(object);
            executeCommit(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void deleteList(List<E> deleteList) {
        try {
            SqlTransaction transaction = getSqlTransaction();
            List<SqlQuery> queryList = new ArrayList<>();
            for (Object object : deleteList) {
                queryList.add(transaction.deleteQuery(object));
            }
            executeCommit(queryList);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
