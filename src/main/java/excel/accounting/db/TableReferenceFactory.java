package excel.accounting.db;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Table Reference Factory
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class TableReferenceFactory {
    private static TableReferenceFactory foreignKeyConstraint;
    private Map<String, List<TableReference>> relationMap;

    private TableReferenceFactory() {
        relationMap = new HashMap<>();
    }

    public void addReference(final String query) {
        if (isForeignKeyReferred(query)) {
            TableReference tableReference = getTableReference(query);
            List<TableReference> referenceList = relationMap.get(tableReference.getReferenceTable());
            if (referenceList == null) {
                referenceList = new ArrayList<>();
                relationMap.put(tableReference.getReferenceTable(), referenceList);
            }
            referenceList.add(tableReference);
        }
    }

    public static TableReferenceFactory instance() {
        if (foreignKeyConstraint == null) {
            foreignKeyConstraint = new TableReferenceFactory();
        }
        return foreignKeyConstraint;
    }

    private TableReference getTableReference(final String query) {
        String table = StringUtils.substringBetween(query, "alter table", "add foreign key");
        table = table.trim();
        String column = StringUtils.substringBetween(query, "add foreign key", "references");
        column = StringUtils.substringBetween(column, "(", ")");
        //
        int indexOf = query.indexOf("references");
        String subQuery = query.substring(indexOf + 10, query.length());
        subQuery = subQuery.trim();
        indexOf = subQuery.indexOf("(");
        String referenceTable = subQuery.substring(0, indexOf);
        String referenceColumn = StringUtils.substringBetween(subQuery, "(", ")");
        return new TableReference(table.trim(), column.trim(), referenceTable.trim(), referenceColumn.trim());
    }

    private boolean isForeignKeyReferred(String query) {
        return query.contains("alter table") && query.contains("add foreign key") && query.contains("references");
    }
}
