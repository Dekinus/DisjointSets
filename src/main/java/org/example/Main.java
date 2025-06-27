package org.example;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Main {
    public static void main(String[] args) {

        LocalTime startTime = LocalTime.now();

        UnionFind uf = new UnionFind();
        //Map<индекс колонки, Map<Элемент, Строка(List<элементов>)>
        Map<Integer, Map<String, List<String>>> columnMap = new HashMap<>();
        String filePath = args[0];
        Set<String> lines = new HashSet<>();
        try (
                FileReader fr = new FileReader(filePath);
                BufferedReader br = new BufferedReader(fr)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                var isValid = processLine(line, columnMap);
                if (isValid) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка чтения файла");
        }

        for (Map<String, List<String>> column : columnMap.values()) {
            for (List<String> group : column.values()) {
                String first = group.get(0);
                for (int i = 1; i < group.size(); i++) {
                    uf.union(first, group.get(i));
                }
            }
        }

        Map<String, List<String>> result = lines.stream()
                .collect(Collectors.groupingBy(
                        uf::find,
                        toList()
                ));

        long count = result.values().stream()
                .filter(e -> e.size() > 1)
                .count();

        var sortedResult = result.values().stream()
                .sorted(((i, j) -> Integer.compare(j.size(), i.size())))
                .toList();

        writeResult(filePath, sortedResult, count);

        long endTime = ChronoUnit.SECONDS.between(startTime, LocalTime.now());
        System.out.println("Время работы (сек) = " + endTime);

    }

    private static class UnionFind {

        private final Map<String, String> parent = new HashMap<>();
        private final Map<String, Integer> rank = new HashMap<>();
        public int numOfGroups = 0;

        public String find(String s) {
            if (!parent.containsKey(s)) {
                parent.put(s, s);
                rank.put(s, 0);
                numOfGroups++;
            }
            if (!s.equals(parent.get(s))) {
                parent.put(s, find(parent.get(s)));
            }
            return parent.get(s);
        }

        public void union(String x, String y) {
            String rootX = find(x);
            String rootY = find(y);
            if (!rootX.equals(rootY)) {
                if (rank.get(rootX) > rank.get(rootY)) {
                    parent.put(rootY, rootX);
                } else if (rank.get(rootX) < rank.get(rootY)) {
                    parent.put(rootX, rootY);
                } else {
                    parent.put(rootY, rootX);
                    rank.put(rootX, rank.get(rootX) + 1);
                }
                numOfGroups--;
            }
        }
    }

    public static boolean processLine(String line, Map<Integer, Map<String, List<String>>> columnMap) {

        String[] parts = line.split(";");
        if (!Arrays.stream(parts)
                .allMatch(s -> s.isEmpty() ||
                        (s.startsWith("\"")) && s.endsWith("\"") && !s.substring(1, s.length() - 1).equals("\""))
        ) {
            return false;
        }

        for (int i = 0; i < parts.length; i++) {
            String value = parts[i].trim();
            if (!"\"\"".equals(value) && !value.isEmpty()) {
                columnMap.computeIfAbsent(i, k -> new HashMap<>())
                        .computeIfAbsent(value, k -> new ArrayList<>())
                        .add(line);
            }
        }
        return true;
    }

    public static void writeResult(String filePath, List<List<String>> result, long count) {

        Path oldFile = Path.of(filePath);
        String newFilePath = filePath.replace(oldFile.getFileName().toString(), "result.txt");
        Path newFile = Path.of(newFilePath);

        if (!Files.exists(newFile)) {
            try {
                Files.createFile(newFile);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Ошибка создания файла");
            }
        }

        try (
                FileOutputStream fileOutputStream = new FileOutputStream(newFile.toFile());
                PrintStream out = new PrintStream(fileOutputStream)
        ) {
            out.println(count);
            for (int i = 0; i < result.size(); i++) {
                out.println("Группа " + (i + 1));
                out.println(String.join("\n", result.get(i)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Файл для записи не найден");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка записи файла");
        }
    }
}
