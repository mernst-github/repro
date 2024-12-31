# VM Crash in loop optimization

```
% javac Repro.java
% docker run -it --platform=linux/amd64 -v .:/host shipilev/openjdk:24-fastdebug
root@4892ff9ccf91:/# cd /host
root@4892ff9ccf91:/host# java -Xbatch -XX:-TieredCompilation Repro
#
# A fatal error has been detected by the Java Runtime Environment:
#
#  Internal Error (src/hotspot/share/opto/loopnode.cpp:3196), pid=27, tid=44
#  Error: assert(!loop->_body.contains(in)) failed
#
# JRE version: OpenJDK Runtime Environment (24.0.1) (fastdebug build 24.0.1-testing-builds.shipilev.net-openjdk-jdk24-b2-20241223-2211)
# Java VM: OpenJDK 64-Bit Server VM (fastdebug 24.0.1-testing-builds.shipilev.net-openjdk-jdk24-b2-20241223-2211, mixed mode, sharing, compressed oops, compressed class ptrs, g1 gc, linux-amd64)
# Problematic frame:
# V  [libjvm.so+0x136e7e4]  OuterStripMinedLoopNode::transform_to_counted_loop(PhaseIterGVN*, PhaseIdealLoop*)+0x964
#
# No core dump will be written. Core dumps have been disabled. To enable core dumping, try "ulimit -c unlimited" before starting Java again
#
# An error report file with more information is saved as:
# /host/hs_err_pid27.log
#
# Compiler replay data is saved as:
# /host/replay_pid27.log
#
# If you would like to submit a bug report, please visit:
#   https://bugreport.java.com/bugreport/crash.jsp
#
Aborted
root@4892ff9ccf91:/host#
```

Includes logs from runs with `shipilev/openjdk:24-fastdebug` and `shipilev/openjdk:latest-fastdebug`.
Product builds seem to work fine, bad assertion only?
`23-fastdebug` works fine, too.

It is very sensitive wrt input values and order, slight changes make it pass.
I'm guessing it triggers certain loop profile counters?
