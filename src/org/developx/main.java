package org.developx;

import org.developx.csv.WriteCsv;
import org.developx.filevisitor.ReportData;
import org.developx.filevisitor.XmlFileVisitor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class main {

    private final static String DEFAULT_PATH = "D:\\000_source\\sftm\\src\\main\\resources\\sql2";

    public static void main(String[] args) throws IOException {

        Path path = Path.of(args.length > 0 ? args[0] : DEFAULT_PATH);
        List<ReportData> reportDataList = new ArrayList<>();
        XmlFileVisitor simpleFileVisitor = new XmlFileVisitor(reportDataList);
        Files.walkFileTree(path, simpleFileVisitor);


        // 최종 결과 리포팅
        File file = new WriteCsv().run(reportDataList);
        Desktop.getDesktop().open(file);
    }
}
