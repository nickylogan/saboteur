package ai.proposed.utils;

import java.util.*;

public class RandomUtils {
  public static <T> Optional<T> choose(Collection<T> c) {
    return c.stream()
        .skip((int) (c.size() * Math.random()))
        .findFirst();
  }

  public static <T> void shuffleSort(List<T> list, Comparator<? super T> c) {
    Collections.shuffle(list);
    list.sort(c);
  }
}
