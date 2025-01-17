import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

class Repro {
  public static final MethodHandle pin;
  public static final MethodHandle unpin;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      Class<?> Continuation = lookup.findClass("jdk.internal.vm.Continuation");
      pin = lookup.findStatic(Continuation, "pin", MethodType.methodType(void.class));
      unpin = lookup.findStatic(Continuation, "unpin", MethodType.methodType(void.class));
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  static class FailsEA {
    final Object o;

    public FailsEA() throws Throwable {
      o = new Object();
      pin.invokeExact();
      unpin.invokeExact();
    }
  }

  static class Crashes {
    final Object o;

    public Crashes() throws Throwable {
      pin.invokeExact();
      unpin.invokeExact();
      o = new Object();
    }
  }

  public static void main(String[] args) throws Throwable {
    System.out.println("################# Expect allocations #################");
    int iterations = 100_000_000;
    for (int i = 0; i < iterations; ++i) {
      new FailsEA();
    }
    System.out.println("################# Expect silence(release) or crash(fastdebug) #################");
    for (int i = 0; i < iterations; ++i) {
      new Crashes();
    }
    System.out.println("################# Done. #################");
  }
}
