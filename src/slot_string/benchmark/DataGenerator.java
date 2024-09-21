package slot_string.benchmark;

import java.util.*;

public class DataGenerator {
  private static final char[] CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-".toCharArray();

  private Input input;

  public Output generate(Input input) {
    this.input = input;

    List<String> texts = genTexts();
    Map<String, Object> slots = genSlotKeysValues();
    String template = genTemplate(texts, slots);
    return new Output(template, slots);
  }

  private String genTemplate(List<String> texts, Map<String, Object> slots) {
    StringBuilder sb = new StringBuilder(input.totalTextLength + (2 + input.slotKeyLength.max) * input.slotCount);
    Iterator<String> keys = slots.keySet().iterator();
    texts.forEach(text -> {
      sb.append(text);
      if (keys.hasNext()) {
        if (input.cStyleSlot) {
          sb.append("%s");
          keys.next();
        } else {
          sb.append('{').append(keys.next()).append('}');
        }
      }
    });
    return sb.toString();
  }

  private List<String> genTexts() {
    int segment = input.totalTextLength / input.slotCount;
    String[] texts = new String[input.slotCount + 1];
    int n = texts.length - 1;
    int remaining = 0;
    for (int i = 0; i < n; ++i) {
      int target = remaining + segment;
      int consume = input.random.nextInt(target + 1);
      texts[i] = genString(consume);
      remaining = target - consume;
    }
    texts[n] = genString(remaining);
    return Arrays.asList(texts);
  }

  private Map<String, Object> genSlotKeysValues() {
    HashMap<String, Object> ret = new HashMap<>();
    if (input.numericSlotKey) {
      for (int i = 0; i < input.slotCount; ++i) {
        ret.put(String.valueOf(i), genString(input.slotValueLength.value(input.random)));
      }
    } else {
      for (int i = 0; i < input.slotCount; ++i) {
        ret.put(genString(input.slotKeyLength.value(input.random)), genString(input.slotValueLength.value(input.random)));
      }
    }
    return ret;
  }

  private String genString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; ++i) {
      sb.append(CHARS[input.random.nextInt(CHARS.length)]);
    }
    return sb.toString();
  }

  public static class Range {
    public int min;
    public int max;

    public Range(int min, int max) {
      if (max < min) {
        this.min = max;
        this.max = min;
      } else {
        this.min = min;
        this.max = max;
      }
    }

    public int value(Random random) {
      return min + random.nextInt(max - min + 1);
    }

    @Override
    public String toString() {
      return "Range{" +
        "min=" + min +
        ", max=" + max +
        '}';
    }
  }

  public static class Input {
    public Random random;
    public int totalTextLength;
    public int slotCount;
    public Range slotKeyLength;
    public boolean numericSlotKey;
    public Range slotValueLength;
    public boolean cStyleSlot;

    public Input setRandom(Random random) {
      this.random = random;
      return this;
    }

    public Input setTotalTextLength(int totalTextLength) {
      this.totalTextLength = totalTextLength;
      return this;
    }

    public Input setSlotCount(int slotCount) {
      this.slotCount = slotCount;
      return this;
    }

    public Input setSlotKeyLength(Range slotKeyLength) {
      this.slotKeyLength = slotKeyLength;
      return this;
    }

    public Input setNumericSlotKey(boolean numericSlotKey) {
      this.numericSlotKey = numericSlotKey;
      return this;
    }

    public Input setSlotValueLength(Range slotValueLength) {
      this.slotValueLength = slotValueLength;
      return this;
    }

    public Input setcStyleSlot(boolean cStyleSlot) {
      this.cStyleSlot = cStyleSlot;
      return this;
    }

    @Override
    public String toString() {
      return "Input{" +
        "random=" + random +
        ", totalTextLength=" + totalTextLength +
        ", slotCount=" + slotCount +
        ", slotKeyLength=" + slotKeyLength +
        ", numericSlotKey=" + numericSlotKey +
        ", slotValueLength=" + slotValueLength +
        ", cStyleSlot=" + cStyleSlot +
        '}';
    }
  }

  public static class Output {
    public String template;
    public Map<String, Object> values;

    public Output(String template, Map<String, Object> values) {
      this.template = template;
      this.values = values;
    }
  }
}
