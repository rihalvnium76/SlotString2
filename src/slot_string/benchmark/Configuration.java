package slot_string.benchmark;

public class Configuration {
  public static final long SEED = 479593221892830234L;
  public static final int ITERATIONS = 100000;
  public static final int TOTAL_TEXT_LENGTH = 6000;
  public static final int SLOT_COUNT = 30;
  public static final DataGenerator.Range SLOT_KEY_LENGTH = new DataGenerator.Range(3, 22);
  public static final DataGenerator.Range SLOT_VALUE_LENGTH = new DataGenerator.Range(4, (int) (TOTAL_TEXT_LENGTH * 1.5f));
}
