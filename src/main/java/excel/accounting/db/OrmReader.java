package excel.accounting.db;

/**
 * Orm Reader
 */
public class OrmReader {
    /*public <T> T findByPrimaryKey(Class<T> tableClass, Object value) throws SQLException {
        OrmTable table = processor.getTable(tableClass);
        if (table == null) {
            throw new SQLException(tableClass.toString() + " this is not a valid entity");
        }
        OrmColumn primaryColumn = table.getPrimaryColumn();
        String selectQuery = processor.getQueryTool().selectQuery(table);
        selectQuery = selectQuery + " where " + primaryColumn.getName() + " = ?";
        try {
            Connection con = getConnection();
            PreparedStatement stmt = con.prepareStatement(selectQuery);
            if (value instanceof Number) {
                stmt.setInt(1, (Integer) value);
            } else {
                stmt.setString(1, value.toString());
            }
            ResultSet rs = stmt.executeQuery();
            int columnCount = rs.getMetaData().getColumnCount();

            if (rs.next()) {
                for (int index = 0; index < columnCount; index++) {
                    rs.get
                }
                value = rs.getRef(1);
            }
            close(rs, stmt, con);
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return value;
    }*/
}
