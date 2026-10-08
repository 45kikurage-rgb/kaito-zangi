package jp.kossacktouch.core;
import java.text.Normalizer;
import java.util.Locale;
public final class Text {
 private Text() {}
 public static String norm(String s) { return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).replaceAll("[\\s\\p{Z}「」『』【】〈〉<>、。，,：:・ー－―‐!?！？]", ""); }
 public static String clean(String s) { return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFKC).replaceAll("[\\s\\p{Z}]", ""); }
}
