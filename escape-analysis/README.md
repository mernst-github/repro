# Continuating pinning in constructor vs escape analysis

Calling Continuation.pin()/unpin() in a constructor confuses EA.
Depending on the placement of pin vs the field allocation, escape
analysis fails or crashes altogether.

Also repro's in 24, not in 23.

```
docker run -it --platform=linux/amd64 -v .:/host shipilev/openjdk:latest-fastdebug  (resp. :latest)
cd /host && javac Repro.java && java --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/jdk.internal.vm=ALL-UNNAMED -Xlog:gc Repro
[0.081s][info][gc] Using G1
################# Expect allocations #################
[0.876s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 48M->1M(126M) 56.310ms
[0.886s][info][gc] GC(1) Pause Young (Normal) (G1 Evacuation Pause) 10M->1M(126M) 2.156ms
[0.899s][info][gc] GC(2) Pause Young (Normal) (G1 Evacuation Pause) 26M->1M(126M) 2.227ms
[0.914s][info][gc] GC(3) Pause Young (Normal) (G1 Evacuation Pause) 51M->1M(126M) 2.841ms
[0.932s][info][gc] GC(4) Pause Young (Normal) (G1 Evacuation Pause) 63M->1M(266M) 3.726ms
[0.959s][info][gc] GC(5) Pause Young (Normal) (G1 Evacuation Pause) 74M->1M(266M) 3.779ms
[0.984s][info][gc] GC(6) Pause Young (Normal) (G1 Evacuation Pause) 90M->1M(266M) 4.071ms
[1.013s][info][gc] GC(7) Pause Young (Normal) (G1 Evacuation Pause) 107M->1M(266M) 3.999ms
[1.049s][info][gc] GC(8) Pause Young (Normal) (G1 Evacuation Pause) 129M->1M(562M) 5.793ms
[1.104s][info][gc] GC(9) Pause Young (Normal) (G1 Evacuation Pause) 151M->1M(562M) 6.117ms
[1.152s][info][gc] GC(10) Pause Young (Normal) (G1 Evacuation Pause) 177M->1M(562M) 6.045ms
[1.207s][info][gc] GC(11) Pause Young (Normal) (G1 Evacuation Pause) 208M->1M(562M) 6.891ms
[1.272s][info][gc] GC(12) Pause Young (Normal) (G1 Evacuation Pause) 240M->1M(842M) 9.061ms
################# Expect silence(release) or crash(fastdebug) #################
 299  Phi  === 298 287 254  [[ 417 411 406 399 392 ]]  #memory  Memory: @BotPTR *+bot, idx=Bot; !jvms: 0x00007fff77001400::invokeStatic @ bci:13 0x00007fff77001000::invokeExact_MT @ bci:18 Repro$Crash::<init> @ bci:7 (line 34) Repro::main @ bci:51 (line 49)
 411  CallStaticJava  === 495 112 299 8 9 (292 1 26 83 118 118 1 1 1 1 388 ) [[ 412 ]] # Static uncommon_trap(reason='intrinsic_or_type_checked_inlining' action='none' debug_id='0')  void ( int ) C=0.000100 0x00007fff77001800::invokeStatic @ bci:13 0x00007fff77001000::invokeExact_MT @ bci:18 Repro$Crash::<init> @ bci:13 (line 35) Repro::main @ bci:51 (line 49) !jvms: 0x00007fff77001800::invokeStatic @ bci:13 0x00007fff77001000::invokeExact_MT @ bci:18 Repro$Crash::<init> @ bci:13 (line 35) Repro::main @ bci:51 (line 49)
#
# A fatal error has been detected by the Java Runtime Environment:
#
#  Internal Error (src/hotspot/share/opto/escape.cpp:4767), pid=89, tid=106
#  assert(false) failed: EA: missing memory path
#
# JRE version: OpenJDK Runtime Environment (25.0) (fastdebug build 25-testing-builds.shipilev.net-openjdk-jdk-b2689-20241230-0204)
# Java VM: OpenJDK 64-Bit Server VM (fastdebug 25-testing-builds.shipilev.net-openjdk-jdk-b2689-20241230-0204, mixed mode, sharing, tiered, compressed oops, compressed class ptrs, g1 gc, linux-amd64)
# Problematic frame:
# V  [libjvm.so+0xc6fac2]  ConnectionGraph::split_unique_types(GrowableArray<Node*>&, GrowableArray<ArrayCopyNode*>&, GrowableArray<MergeMemNode*>&, Unique_Node_List&)+0x39e2
#
# No core dump will be written. Core dumps have been disabled. To enable core dumping, try "ulimit -c unlimited" before starting Java again
#
# An error report file with more information is saved as:
# /host/hs_err_pid89.log
#
# Compiler replay data is saved as:
# /host/replay_pid89.log
#
# If you would like to submit a bug report, please visit:
#   https://bugreport.java.com/bugreport/crash.jsp
#
Aborted
```
