package jp.kossacktouch.core;
import java.util.*;
import java.util.regex.*;
/** Explicit registration controls only. A/B/C/D quiz controls never enter this selector. */
public final class Registration {
 public enum Stage { INTRO, SUMMARY, TERMS, YEAR, SEX, PREFECTURE, SCHOOL_INITIAL, SCHOOL, UNKNOWN }
 public static final Set<String> INTRO=new HashSet<>(Arrays.asList("ちゃんと聞きます！","まだまだ毎日暑いから嬉しい！","お任せください！","なーーーにーーー！","地域別なら頑張ればいけるか","母校にポカリを届けたい！","ランキング上位目指すぞ！","参加登録する"));
 public static final Set<String> PREFECTURES=new HashSet<>(Arrays.asList("北海道","青森県","岩手県","宮城県","秋田県","山形県","福島県","茨城県","栃木県","群馬県","埼玉県","千葉県","東京都","神奈川県","新潟県","富山県","石川県","福井県","山梨県","長野県","岐阜県","静岡県","愛知県","三重県","滋賀県","京都府","大阪府","兵庫県","奈良県","和歌山県","鳥取県","島根県","岡山県","広島県","山口県","徳島県","香川県","愛媛県","高知県","福岡県","佐賀県","長崎県","熊本県","大分県","宮崎県","鹿児島県","沖縄県"));
 public static boolean forbidden(String label){return label.contains("変更")||label.contains("修正")||label.contains("戻る")||label.contains("やり直")||label.contains("取消")||label.contains("キャンセル");}
 public static Stage stage(String context){
  if(context.contains("参加規約")&&context.contains("同意して進む"))return Stage.TERMS;
  if(context.contains("情報")&&(context.contains("正しいですか")||context.contains("正しいでしょうか")))return Stage.SUMMARY;
  if(context.contains("頭文字")&&context.contains("高校"))return Stage.SCHOOL_INITIAL;
  if(context.contains("生まれた年")||context.contains("生年を選"))return Stage.YEAR;
  if(context.contains("性別")&&!context.contains("情報は"))return Stage.SEX;
  if(context.contains("都道府県")||context.contains("住んでいる地域")||context.contains("北海道・東北地方"))return Stage.PREFECTURE;
  if(context.contains("高等学校")||context.contains("高校を選"))return Stage.SCHOOL;
  if(context.contains("キャンペーン")||context.contains("参加登録")||context.contains("超良問スポーツ")||context.contains("諸君"))return Stage.INTRO;
  return Stage.UNKNOWN;
 }
 public static boolean eligible(Stage stage,String label){if(forbidden(label))return false;String t=label.trim();switch(stage){
  case SUMMARY:return t.equals("登録する");
  case TERMS:return t.startsWith("参加規約に同意して進む");
  case INTRO:return INTRO.contains(t);
  case YEAR:return t.matches("(?:19[0-9]{2}|20[0-2][0-9])年");
  case SEX:return Arrays.asList("男","女","男性","女性","その他","回答しない").contains(t);
  case PREFECTURE:return PREFECTURES.contains(t);
  case SCHOOL_INITIAL:return t.matches("[あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわ]");
  case SCHOOL:return t.matches("[^\\n]{1,60}(?:高等学校|高校)(?:[ 　]*[（(](?:公立|私立|国立)[）)])?")&&!t.contains("頭文字");
  default:return false;
 }}
 public static String choose(Stage stage,List<String> labels,Random random){List<String> safe=new ArrayList<>();for(String s:labels)if(eligible(stage,s)&&!safe.contains(s))safe.add(s);if(safe.isEmpty())return null;return safe.get(random.nextInt(safe.size()));}
}
