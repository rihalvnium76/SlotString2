package slot_string.benchmark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DataGenerator {
  private static final char[] CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-".toCharArray();

  private Input input;

  public Output generate(Input input) {
    this.input = input;

    var texts = genTexts();
    var slots = genSlotKeysValues();
    var template = genTemplate(texts, slots);
    return new Output(template, slots);
  }

  private String genTemplate(List<String> texts, Map<String, Object> slots) {
    var sb = new StringBuilder(input.totalTextLength + (2 + input.slotKeyLength.max) * input.slotCount);
    var keys = slots.keySet().iterator();
    texts.forEach(text -> {
      sb.append(text);
      if (keys.hasNext()) {
        sb.append('{').append(keys.next()).append('}');
      }
    });
    return sb.toString();
  }

  private List<String> genTexts() {
    var segment = input.totalTextLength / input.slotCount;
    var texts = new String[input.slotCount + 1];
    int n = texts.length - 1;
    int remaining = 0;
    for (int i = 0; i < n; ++i) {
      int target = remaining + segment;
      int consume = input.random.nextInt(target + 1);
      texts[i] = genString(consume);
      remaining = target - consume;
    }
    texts[n] = genString(remaining);
    return List.of(texts);
  }

  private Map<String, Object> genSlotKeysValues() {
    var ret = new HashMap<String, Object>();
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
    var sb = new StringBuilder(length);
    for (int i = 0; i < length; ++i) {
      sb.append(CHARS[input.random.nextInt(CHARS.length)]);
    }
    return sb.toString();
  }

  public record Range(int min, int max) {
    public Range {
      if (max < min) {
        int t = min;
        min = max;
        max = t;
      }
    }

    public int value(Random random) {
      return min + random.nextInt(max - min + 1);
    }
  }

  public static class Input {
    public Random random;
    public int totalTextLength;
    public int slotCount;
    public Range slotKeyLength;
    public boolean numericSlotKey;
    public Range slotValueLength;

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

    @Override
    public String toString() {
      return "Input{" +
        "random=" + random +
        ", totalTextLength=" + totalTextLength +
        ", slotCount=" + slotCount +
        ", slotKeyLength=" + slotKeyLength +
        ", numericSlotKey=" + numericSlotKey +
        ", slotValueLength=" + slotValueLength +
        '}';
    }
  }

  public record Output(String template, Map<String, Object> values) {}
}
