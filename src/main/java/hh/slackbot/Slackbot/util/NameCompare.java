package hh.slackbot.slackbot.util;

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

  public NameCompare() {}

  public float compareNames(String name1, String name2) {
    StringMetric metric = StringMetrics.damerauLevenshtein();
    float result = metric.compare(name1, name2);
    logger.info("{}, {}, {}", name1, name2, result);
    return result;
  }

  public List<String> compareToList(String name, List<String> names) {
    List<String> hits = names.stream()
        .filter(n -> {
          float result = compareNames(n, name);
          return result >= THRESHOLD;
        })
        .collect(Collectors.toList());

    if (!hits.contains(name)) {
      return hits;
    }
    return new ArrayList<>();
  }
  
}
