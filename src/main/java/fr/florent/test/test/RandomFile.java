package fr.florent.test.test;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.text.WordUtils;

public class RandomFile {

  public static void main(String[] args) {
    try (Scanner scanner = new Scanner(System.in)) {
      Path src = Paths.get(args[0]);
      
      ArrayList<Path> paths = videoStream(src).collect(Collectors.toCollection(() -> new ArrayList<>()));
      
      printTags(paths);
      
      int nb = Integer.valueOf(args[1]);
      nb = paths.size() < nb ? paths.size() : nb;
      
      while (!paths.isEmpty()) {
        String[] filters = askFilter(scanner);
        openVideos(paths, filters, nb);
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void printTags(ArrayList<Path> paths) throws IOException {
    Map<String, Integer> tags = new HashMap<>();
    fillTags(paths, tags);
    System.out.println(sorted(tags));
  }

  private static void openVideos(ArrayList<Path> paths, String[] filters, int nb) throws IOException {
    ArrayList<Path> filtered = paths.stream().filter(f -> matchFilters(filters, f)).collect(Collectors.toCollection(() -> new ArrayList<>()));
    for (int i = 0; i < nb; i++) {
      openRandom(paths, filtered);
    }
  }

  private static String[] askFilter(Scanner scanner) {
    String[] filters = scanFilter(scanner).trim().split(" ");
    return filters;
  }

  private static String sorted(Map<String, Integer> tags) {
    String sortedTags = tags.entrySet().stream()
      .sorted(Comparator.comparing(Entry::getValue))
      .map(Entry::getKey)
      .collect(Collectors.joining(" "));
    sortedTags = WordUtils.wrap(sortedTags, 50);
    return sortedTags;
  }

  private static void fillTags(Collection<Path> paths, Map<String, Integer> tags) throws IOException {
    paths.stream()
      .map(f -> f.getParent().getFileName().toString().toLowerCase() + " " + f.getFileName().toString().toLowerCase())
      .map(n -> FilenameUtils.getBaseName(n))
      .map(n -> n.replaceAll("[-_\\(\\)\\[\\]\\.\\d{2,}]", " "))
      .map(n -> n.trim())
      .flatMap(n -> Arrays.stream(n.split(" ")))
      .filter(s -> s != null && !"".equals(s))
      .filter(s -> s.length() > 2)
      .map(n -> n.trim())
      .forEach(n -> {
        Integer count = tags.get(n);
        if (count == null) {
          count = 0;
          tags.put(n, count);
        }
        tags.put(n, count + 1);
      });
  }

  private static Stream<Path> videoStream(Path src) throws IOException {
    return Files.walk(src)
      .filter(Files::isRegularFile)
      .filter(RandomFile::isVideo);
  }

  private static boolean matchFilters(String[] filters, Path f) {
    String filename = f.getFileName().toString().toLowerCase();
    return filters.length <= 0
        || Arrays.stream(filters).allMatch(filter -> filename.contains(filter.toLowerCase()));
  }

  private static String scanFilter(Scanner scanner) {
      System.out.println();
      System.out.print("filter? ");
      return scanner.nextLine();
  }

  private static void openRandom(ArrayList<Path> paths, ArrayList<Path> filtered) throws IOException {
    if (filtered.isEmpty()) {
      return;
    }
    
    Random rand = new Random();
    int i = rand.nextInt(filtered.size());
    Path path = filtered.get(i);
    
    Desktop.getDesktop().open(path.toFile());
    paths.remove(path);
  }
  
  public static boolean isVideo(Path p) {
    String n = p.getFileName().toString();
    return n.endsWith(".avi") || n.endsWith(".mp4") || n.endsWith(".mkv") || n.endsWith(".ogg")
        || n.endsWith(".AVI") || n.endsWith(".MP4") || n.endsWith(".MKV") || n.endsWith(".OGG");
  }

}
