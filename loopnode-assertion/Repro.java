import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class Repro {
  static final int COUNT = 100000;
  static final MemorySegment segment = Arena.global().allocate(JAVA_LONG, COUNT);

  public static void main(String[] args) {
    var i = 0;
    var j = 0;
    while (i < COUNT) {
      segment.setAtIndex(JAVA_LONG, i++, 0);
      segment.setAtIndex(JAVA_LONG, j++, 0);
    }
  }
}
