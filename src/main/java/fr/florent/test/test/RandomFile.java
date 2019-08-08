package fr.florent.test.test;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.text.WordUtils;

public class RandomFile {

  public static void main(String[] args) {
    try (Scanner scanner = new Scanner(System.in)) {
      Path src = Paths.get(args[0]);
      
      Map<String, Integer> tags = new HashMap<>();
      Set<Path> played = new HashSet<>();
      
      fillTags(src, tags);
      
      String sortedTags = sorted(tags);
      System.out.println(sortedTags);
      
      while (true) {
        String[] filters = askFilter(scanner);
        openVideos(args, src, played, filters);
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void openVideos(String[] args, Path src, Set<Path> played, String[] filters) throws IOException {
    Path[] paths = videoStream(src)
      .filter(f -> matchFilters(filters, f))
      .toArray(s -> new Path[s]);
    
//      System.out.println("Count: " + paths.length);
//      Arrays.stream(paths).forEach(System.out::println);
    
    int nb = Integer.valueOf(args[1]);
    nb = paths.length < nb ? paths.length : nb;
    
    for (int i = 0; i < nb; i++) {
      openRandom(paths, played);
    }
  }

  private static String[] askFilter(Scanner scanner) {
    String[] filters = scanFilter(scanner).trim().split(" ");
    return filters;
  }

  private static String sorted(Map<String, Integer> tags) {
    String sortedTags = tags.entrySet().stream()
//        .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue())) // reverse sort
      .sorted(Comparator.comparing(Entry::getValue))
      .map(Entry::getKey)
      .collect(Collectors.joining(" "));
    sortedTags = WordUtils.wrap(sortedTags, 50);
    return sortedTags;
  }

  private static void fillTags(Path src, Map<String, Integer> tags) throws IOException {
    videoStream(src)
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

  private static void openRandom(Path[] paths, Set<Path> alreadyPicked) throws IOException {
    // TODO manage all movies in a set
    // and remove played movies from the set
//    if (paths.length <= alreadyPicked.size()) {
//      return;
//    }
    
    Random rand = new Random();
    Path path = null;
    while (path == null) {
      int i = rand.nextInt(paths.length);
      Path candidate = paths[i];
      if (!alreadyPicked.contains(candidate)) {
        path = candidate;
      }
    }
    
//    System.out.println(path.getFileName());
    Desktop.getDesktop().open(path.toFile());
    alreadyPicked.add(path);
  }
  
  public static boolean isVideo(Path p) {
    String n = p.getFileName().toString();
    return n.endsWith(".avi") || n.endsWith(".mp4") || n.endsWith(".mkv") || n.endsWith(".ogg")
        || n.endsWith(".AVI") || n.endsWith(".MP4") || n.endsWith(".MKV") || n.endsWith(".OGG");
  }

}
