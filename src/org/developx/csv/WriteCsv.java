package org.developx.csv;

import org.developx.deletetable.DeleteTable;
import org.developx.filevisitor.ReportData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WriteCsv {

    public File run(List<ReportData> reportDataList) {
        LocalDateTime now = LocalDateTime.now();

        String fileName = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        Path datFilePath = Path.of(System.getProperty("user.dir") + "\\" + fileName);
        try(BufferedWriter bufferedWriter = Files.newBufferedWriter(datFilePath, Charset.forName("UTF-8"))){

            bufferedWriter.write("file name,namespace,SQL ID,SQL Type,report,full path");
            bufferedWriter.newLine();

            for (ReportData reportData : reportDataList) {
                writeBody(bufferedWriter, reportData);
            }
        } catch (IOException e) {
            System.out.println("csv 파일 생성에 실패했습니다."+ e);
        }
        return datFilePath.toFile();
    }

    private static void writeBody(BufferedWriter bufferedWriter, ReportData reportData) throws IOException {
        bufferedWriter.write(reportData.getFileName());
        bufferedWriter.write(",");
        bufferedWriter.write(reportData.getNamespace());
        bufferedWriter.write(",");
        bufferedWriter.write(reportData.getSqlId());
        bufferedWriter.write(",");
        bufferedWriter.write(reportData.getSqlType());
        bufferedWriter.write(",");
        bufferedWriter.write(getDeleteData(reportData.getDeleteData()));
        bufferedWriter.write(",");
        bufferedWriter.write(reportData.getFullPath());

        bufferedWriter.newLine();
    }

    private static String getDeleteData(List<DeleteTable> list) {
        StringBuilder sb = new StringBuilder();
        for (DeleteTable deleteTable : list) {
            sb.append(deleteTable);
            sb.append(" ");
        }
        return sb.toString();
    }
}
