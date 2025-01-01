import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class Repro {
  static final int COUNT = 100000;
  static final MemorySegment segment = Arena.global().allocate(JAVA_LONG, COUNT);

  public static void main(String[] args) {
    var lt = 0;
    var eq = 0;
    while (eq < COUNT) {
      var tmp = segment.getAtIndex(JAVA_LONG, eq);
      segment.setAtIndex(JAVA_LONG, eq, segment.getAtIndex(JAVA_LONG, lt));
      segment.setAtIndex(JAVA_LONG, lt, tmp);
      eq++;
      lt++;
    }
  }
}
