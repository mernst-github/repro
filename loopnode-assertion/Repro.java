import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Repro {
  static final MemorySegment segment;

  static {
    final List<String> lines;
    try {
      lines = Files.readAllLines(Path.of("data.txt"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    segment = Arena.global().allocate(JAVA_LONG, lines.size());
    for (int i = 0; i < lines.size(); i++) {
      set(i, Long.parseLong(lines.get(i)));
    }
  }

  static long get(long index) {
    return segment.getAtIndex(JAVA_LONG, index);
  }

  static void set(long index, long value) {
    segment.setAtIndex(JAVA_LONG, index, value);
  }

  static void swap(long left, long right) {
    var tmp = get(left);
    set(left, get(right));
    set(right, tmp);
  }

  static void partition(long left, long right) {
    var pivot = get((left + right) / 2);
    var lt = left;
    var eq = left;
    var gt = right;
    while (eq <= gt) {
      if (get(eq) < pivot) {
        swap(eq++, lt++);
      } else if (get(eq) > pivot) {
        swap(eq, gt--);
      } else {
        eq++;
      }
    }
  }

  public static void main(String[] args) {
    partition(0, segment.byteSize() / JAVA_LONG.byteSize() - 1);
  }
}
