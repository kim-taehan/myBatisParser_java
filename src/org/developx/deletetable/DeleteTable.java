package org.developx.deletetable;

import java.util.Objects;

import static org.developx.deletetable.DeleteType.*;

public class DeleteTable {

    public final DeleteType deleteType;
    public final String table;
    public final String column;

    private DeleteTable(DeleteType deleteType, String table, String column) {
        this.table = table.toLowerCase();
        this.column = column.toLowerCase();
        this.deleteType = deleteType;
    }

    public DeleteTable(String datLine) {
        String[] split = datLine.split("\\|");
        deleteType = valueOf(split[0].toUpperCase());
        table = split[1].toLowerCase();
        column = deleteType == TABLE ? "" : split[2].toLowerCase();
    }

    public static DeleteTable fromTable(String tableName) {
        return new DeleteTable(TABLE, tableName, "");
    }

    public static DeleteTable fromColumn(String tableName, String columnName) {
        return new DeleteTable(COLUMN, tableName, columnName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteTable that = (DeleteTable) o;
        return deleteType == that.deleteType && Objects.equals(table, that.table) && Objects.equals(column, that.column);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deleteType, table, column);
    }

    @Override
    public String toString() {
        return deleteType == TABLE ? String.format("table(%s)", table) : String.format("column(%s:%s)", table, column);
    }
}
