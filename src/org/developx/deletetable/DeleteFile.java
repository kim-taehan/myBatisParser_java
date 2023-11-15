package org.developx.deletetable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeleteFile {

    private static DeleteFile INSTANCE = new DeleteFile();
    private final List<DeleteTable> deletedTableInfo;

    private DeleteFile() {
        Path datFilePath = Path.of(System.getProperty("user.dir") + "\\delete.dat");
        try (Stream<String> stream = Files.lines(datFilePath)) {
            deletedTableInfo = stream.map(DeleteTable::new).collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalStateException("delete dat 파일 읽기에 실패했습니다.");
        }
        System.out.println("Delete dat 파일 " + datFilePath + " : " + deletedTableInfo.size());
    }

    public static DeleteFile getInstance() {
        return INSTANCE;
    }

    public List<DeleteTable> getDeleteTable() {
        return deletedTableInfo;
    }
}
