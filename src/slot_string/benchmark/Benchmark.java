package slot_string.benchmark;

import slot_string.SlotString;
import slot_string.SlotStringLite;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Random;

public class Benchmark {

  public static void main(String[] args) {
    System.out.println("[D] " + System.getProperty("java.vendor")
      + " " + System.getProperty("java.runtime.name")
      + " (build " + System.getProperty("java.runtime.version")
      + ", " + System.getProperty("java.vm.info") + ")");
    System.out.println("[D] SDK: " + System.getProperty("java.specification.version")
      + " (Class Version: " + System.getProperty("java.class.version") + ")");
    System.out.println("[D] VM Options: "
      + String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments()));
    System.out.println("[D] Benchmark Configurations:");
    System.out.println("  - SEED: " + Configuration.SEED);
    System.out.println("  - ITERATIONS: " + Configuration.ITERATIONS);
    System.out.println("  - TOTAL_TEXT_LENGTH: " + Configuration.TOTAL_TEXT_LENGTH);
    System.out.println("  - SLOT_COUNT: " + Configuration.SLOT_COUNT);
    System.out.println("  - SLOT_KEY_LENGTH: " + Configuration.SLOT_KEY_LENGTH);
    System.out.println("  - SLOT_VALUE_LENGTH: " + Configuration.SLOT_VALUE_LENGTH);
    System.out.println("[D] SlotStringLite.format() : use StringBuffer");
    System.out.println("[I] 准备测试数据");

    DataGenerator dataGenerator = new DataGenerator();
    // 索引占位符
    DataGenerator.Output numericSlots = dataGenerator.generate(genConfig(true));
    // 命名占位符
    DataGenerator.Output namedSlots = dataGenerator.generate(genConfig(false));
    // C风格占位符
    DataGenerator.Output cStyleSlots = dataGenerator.generate(genConfig(true).setcStyleSlot(true));

    long time;
    DataGenerator.Output targetOutput;

    System.out.println("[I] 开始 索引占位符替换 性能基准测试");

    targetOutput = numericSlots;
    time = benchmarkMessageFormat(targetOutput.template, targetOutput.values.values().toArray());
    System.out.println("  - MessageFormat.format() 耗时（单位 ns/iter）：" + time);
    time = benchmarkSlotStringLite(targetOutput.template, targetOutput.values);
    System.out.println("  - SlotStringLite.format() 耗时（单位 ns/iter）：" + time);
    time = benchmarkMultipleStringReplace(targetOutput.template, targetOutput.values);
    System.out.println("  - 多重 String.replace() 耗时（单位 ns/iter）：" + time);
    time = benchmarkSlotStringQformat(targetOutput.template, targetOutput.values);
    System.out.println("  - SlotString.qformat() 耗时（单位 ns/iter）：" + time);
    time = benchmarkSlotStringFormat(targetOutput.template, targetOutput.values);
    System.out.println("  - SlotString.format() 耗时（单位 ns/iter）：" + time);
    System.out.println("  - String.format() 不支持该特性");

    System.out.println("[I] 开始 命名占位符替换 性能基准测试");

    targetOutput = namedSlots;
    System.out.println("  - MessageFormat.format() 不支持该特性");
    time = benchmarkSlotStringLite(targetOutput.template, targetOutput.values);
    System.out.println("  - SlotStringLite.format() 耗时（单位 ns/iter）：" + time);
    time = benchmarkMultipleStringReplace(targetOutput.template, targetOutput.values);
    System.out.println("  - 多重 String.replace() 耗时（单位 ns/iter）：" + time);
    time = benchmarkSlotStringQformat(targetOutput.template, targetOutput.values);
    System.out.println("  - SlotString.qformat() 耗时（单位 ns/iter）：" + time);
    time = benchmarkSlotStringFormat(targetOutput.template, targetOutput.values);
    System.out.println("  - SlotString.format() 耗时（单位 ns/iter）：" + time);
    System.out.println("  - String.format() 不支持该特性");

    System.out.println("[I] 开始 C 风格占位符替换 性能基准测试");

    targetOutput = cStyleSlots;
    System.out.println("  - MessageFormat.format() 不支持该特性");
    System.out.println("  - SlotStringLite.format() 不支持该特性");
    System.out.println("  - 多重 String.replace() 不支持该特性");
    System.out.println("  - SlotString.qformat() 不支持该特性");
    System.out.println("  - SlotString.format() 不支持该特性");
    time = benchmarkStringFormat(targetOutput.template, targetOutput.values.values().toArray());
    System.out.println("  - String.format() 耗时（单位 ns/iter）：" + time);
  }

  private static DataGenerator.Input genConfig(boolean numericKey) {
    return new DataGenerator.Input()
      .setRandom(new Random(Configuration.SEED))
      .setTotalTextLength(Configuration.TOTAL_TEXT_LENGTH)
      .setSlotCount(Configuration.SLOT_COUNT)
      .setSlotKeyLength(Configuration.SLOT_KEY_LENGTH)
      .setSlotValueLength(Configuration.SLOT_VALUE_LENGTH)
      .setNumericSlotKey(numericKey);
  }

  private static long benchmarkSlotStringLite(String template, Map<String, Object> values) {
    String never = null;
    long startTime = System.nanoTime();
    for (int i = 0; i < Configuration.ITERATIONS; ++i) {
      never = SlotStringLite.format(template, values);
    }
    long endTime = System.nanoTime();
    if (System.currentTimeMillis() == Long.MIN_VALUE + 1L) {
      System.out.println(never);
    }
    return (endTime - startTime) / Configuration.ITERATIONS;
  }

  private static long benchmarkMessageFormat(String template, Object[] values) {
    String never = null;
    long startTime = System.nanoTime();
    for (int i = 0; i < Configuration.ITERATIONS; ++i) {
      never = MessageFormat.format(template, values);
    }
    long endTime = System.nanoTime();
    if (System.currentTimeMillis() == Long.MIN_VALUE + 1L) {
      System.out.println(never);
    }
    return (endTime - startTime) / Configuration.ITERATIONS;
  }

  private static long benchmarkMultipleStringReplace(String template, Map<String, Object> values) {
    long startTime = System.nanoTime();
    StringBuilder sb = new StringBuilder();
    String res = template;
    for (int i = 0; i < Configuration.ITERATIONS; ++i) {
      for (Map.Entry<String, Object> entry : values.entrySet()) {
        StringBuilder key = sb.append('{').append(entry.getKey()).append('}');
        Object value = entry.getValue();
        if (value == null) {
          res = res.replace(key, "");
        } else if (value instanceof BigDecimal) {
          res = res.replace(key, ((BigDecimal) value).toPlainString());
        } else {
          res = res.replace(key, value.toString());
        }
        sb.setLength(0);
      }
    }
    long endTime = System.nanoTime();
    if (System.currentTimeMillis() == Long.MIN_VALUE + 1L) {
      System.out.println(res);
    }
    return (endTime - startTime) / Configuration.ITERATIONS;
  }

  private static long benchmarkSlotStringQformat(String template, Map<String, Object> values) {
    String never = null;
    long startTime = System.nanoTime();
    SlotString ss = new SlotString();
    for (int i = 0; i < Configuration.ITERATIONS; ++i) {
      never = ss.qformat(template, values);
    }
    long endTime = System.nanoTime();
    if (System.currentTimeMillis() == Long.MIN_VALUE + 1L) {
      System.out.println(never);
    }
    return (endTime - startTime) / Configuration.ITERATIONS;
  }

  private static long benchmarkSlotStringFormat(String template, Map<String, Object> values) {
    String never = null;
    long startTime = System.nanoTime();
    SlotString ss = new SlotString(template);
    for (int i = 0; i < Configuration.ITERATIONS; ++i) {
      never = ss.format(values);
    }
    long endTime = System.nanoTime();
    if (System.currentTimeMillis() == Long.MIN_VALUE + 1L) {
      System.out.println(never);
    }
    return (endTime - startTime) / Configuration.ITERATIONS;
  }

  private static long benchmarkStringFormat(String template, Object[] values) {
    String never = null;
    long startTime = System.nanoTime();
    for (int i = 0; i < Configuration.ITERATIONS; ++i) {
      never = String.format(template, values);
    }
    long endTime = System.nanoTime();
    if (System.currentTimeMillis() == Long.MIN_VALUE + 1L) {
      System.out.println(never);
    }
    return (endTime - startTime) / Configuration.ITERATIONS;
  }
}
