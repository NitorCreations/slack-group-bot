package hh.nitor.slackbot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NameCompare {
  private static final Logger logger = LoggerFactory.getLogger(NameCompare.class);
  private static final float THRESHOLD = 0.8f;

  /**
   * Relative similarity between names using damerauLevenshtein algorithm.
   *
   * @param name1 to compare
   * @param name2 to compare
   * @return value between 0-1 where 1 means they are the same.
   */
  public float compareNames(String name1, String name2) {
    StringMetric metric = StringMetrics.damerauLevenshtein();
    float result = metric.compare(name1, name2);
    logger.info("{}, {}, {}", name1, name2, result);
    return result;
  }

  /**
   * Compares a single name to a list of names.
   *
   * @param name to compare to list
   * @param names list
   * @return List of similar names. Returns empty list if expected name is part of search list.
   */
  public List<String> compareToList(String name, List<String> names) {
    if (names.contains(name)) {
      return new ArrayList<>();
    }
    logger.info("Collecting similar group names into a list...");
    List<String> hits = names.stream()
        .filter(n -> {
          float result = compareNames(n, name);
          return result >= THRESHOLD;
        })
        .collect(Collectors.toList());

    return hits;
  }
}