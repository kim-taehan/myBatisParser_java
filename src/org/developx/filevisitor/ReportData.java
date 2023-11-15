package org.developx.filevisitor;

import org.developx.deletetable.DeleteTable;

import java.util.List;

public class ReportData {
    private final String fileName;
    private final String fullPath;
    private final String namespace;
    private final String sqlType;
    private final String sqlId;
    private final List<DeleteTable> deleteData;

    public ReportData(String fileName, String fullPath, String namespace, String sqlType, String sqlId, List<DeleteTable> deleteData) {
        this.fileName = fileName;
        this.fullPath = fullPath;
        this.namespace = namespace;
        this.sqlType = sqlType;
        this.sqlId = sqlId;
        this.deleteData = deleteData;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getSqlType() {
        return sqlType;
    }

    public String getSqlId() {
        return sqlId;
    }

    public List<DeleteTable> getDeleteData() {
        return deleteData;
    }

    @Override
    public String toString() {
        return "ReportData{" +
                "fileName='" + fileName + '\'' +
                ", fullPath='" + fullPath + '\'' +
                ", namespace='" + namespace + '\'' +
                ", sqlType='" + sqlType + '\'' +
                ", sqlId='" + sqlId + '\'' +
                ", deleteData=" + deleteData +
                '}';
    }
}
