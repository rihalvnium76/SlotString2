package slot_string;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlotStringLite {
  private static final Pattern SLOT = Pattern.compile("(?<!\\{)\\{[^{}\r\n\t]*}(?!})|\\{\\{|}}");

  public static String format(String template, Map<String, Object> values) {
    if (template == null || template.isEmpty()) {
      return "";
    }
    if (values == null) {
      values = Collections.emptyMap();
    }

    Matcher matcher = SLOT.matcher(template);
    StringBuffer sb = new StringBuffer(template.length());
    while (matcher.find()) {
      String slot = matcher.group();
      if (slot.length() == 2 && slot.charAt(0) == slot.charAt(1)) {
        // Escape
        matcher.appendReplacement(sb, String.valueOf(slot.charAt(0)));
        continue;
      }
      String key = slot.substring(1, slot.length() - 1);
      Object value = values.get(key);
      if (value == null) {
        matcher.appendReplacement(sb, "");
      } else if (value instanceof BigDecimal) {
        matcher.appendReplacement(sb, ((BigDecimal) value).toPlainString());
      } else {
        matcher.appendReplacement(sb, value.toString());
      }
    }
    matcher.appendTail(sb);

    return sb.toString();
  }

  public static void main(String[] args) {
    Map<String, Object> values1 = new HashMap<>();
    values1.put("A", "1");
    values1.put("B", 2);
    values1.put("C", new BigDecimal("1.2E+3"));
    System.out.println(format("0 {A} {B} {C} {{D}} {{{{E}}}} {{{{{{F}}}}}} {} 9", values1));
    System.out.println(format("{A}{{B}}{}", Collections.singletonMap("A", "{{1}}")));
  }
}
