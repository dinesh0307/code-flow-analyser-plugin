package com.dinesh.codeflowanalyser.parser;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileReader {

    public List<Path> readJavaFiles(String rootDirectory) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(rootDirectory))) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> !isTestFile(path))
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }

    public List<Path> readConfigFiles(String rootDirectory) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(rootDirectory))) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".properties") || path.toString().endsWith(".xml"))
                    .collect(Collectors.toList());
        }
    }

    private boolean isTestFile(Path path) {
        return path.toString().contains("src/test/java");
    }
}