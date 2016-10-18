package excel.accounting.model;

/**
 * Column Name
 */
public class ColumnName {
    private final String name;
    private final ColumnType columnType;
    private String title;

    public ColumnName(String name) {
        this(name, ColumnType.TextField);
    }

    public ColumnName(String name, ColumnType columnType) {
        this(name, columnType, name);
    }

    public ColumnName(String name, ColumnType columnType, String title) {
        this.name = name;
        this.columnType = columnType;
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
