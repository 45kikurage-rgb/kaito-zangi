package jp.kossacktouch.core;
import java.util.*;
import java.util.regex.*;
/** Explicit registration controls only. A/B/C/D quiz controls never enter this selector. */
public final class Registration {
 public enum Stage { INTRO, SCHOOL_GUIDE, SUMMARY, TERMS, YEAR, SEX, PREFECTURE, SCHOOL_INITIAL, SCHOOL, UNKNOWN }
 public static final Set<String> INTRO=new HashSet<>(Arrays.asList("ちゃんと聞きます！","まだまだ毎日暑いから嬉しい！","お任せください！","なーーーにーーー！","地域別なら頑張ればいけるか","母校にポカリを届けたい！","ランキング上位目指すぞ！","参加登録する"));
 public static final Set<String> PREFECTURES=new HashSet<>(Arrays.asList("北海道","青森県","岩手県","宮城県","秋田県","山形県","福島県","茨城県","栃木県","群馬県","埼玉県","千葉県","東京都","神奈川県","新潟県","富山県","石川県","福井県","山梨県","長野県","岐阜県","静岡県","愛知県","三重県","滋賀県","京都府","大阪府","兵庫県","奈良県","和歌山県","鳥取県","島根県","岡山県","広島県","山口県","徳島県","香川県","愛媛県","高知県","福岡県","佐賀県","長崎県","熊本県","大分県","宮崎県","鹿児島県","沖縄県"));
 /** Visible, current questions override registration-related words from quiz text. */
 public static boolean quizPriority(boolean visibleQuestion,boolean latestQuestion,boolean allFourChoices){return visibleQuestion&&(latestQuestion||allFourChoices);}
 public static boolean forbidden(String label){return label.contains("変更")||label.contains("修正")||label.contains("戻る")||label.contains("やり直")||label.contains("取消")||label.contains("キャンセル");}
 public static boolean personal(Stage s){return s==Stage.YEAR||s==Stage.SEX||s==Stage.TERMS;}
 /** Personal choices and the known terms button require device-local advance approval. */
 public static boolean canProceed(Stage s,boolean preapproved){return !personal(s)||preapproved;}
 public static boolean introLabel(String label){
  String n=Text.norm(label);for(String s:INTRO)if(Text.norm(s).equals(n))return true;
  for(String s:new String[]{"わかりました！","初めてだけど頑張る！","初めてだけど頑張ります！","が、がんばる..","ランキング上位目指すぞ！","地域別なら頑張ればいけるかも？！","全国TOP10入りは厳しいかも..","スポーツが得意な友達に相談しよう"})if(Text.norm(s).equals(n))return true;
  // Long quick replies are visually truncated; these two distinctive phrases were observed in the videos.
  return n.startsWith("昨年同様のバージョンアップ")||n.startsWith("昨年同様のバジョンアップ")||n.startsWith("地域別なら頑張ればいけるかも");
 }
 /** Narrow ordinary-message prompts; a sent choice or incidental keyword is not a new stage. */
 public static Stage promptStage(String text){
  String t=Text.clean(text);
  if(t.contains("高校の頭文字")&&(t.contains("何")||t.contains("選")))return Stage.SCHOOL_INITIAL;
  if(t.contains("高校がある都道府県を選")||t.contains("住んでいる地域を選"))return Stage.PREFECTURE;
  if(t.contains("生まれた年を選")||t.contains("生年を選"))return Stage.YEAR;
  if(t.contains("性別を選"))return Stage.SEX;
  if(t.contains("高校を選")||t.contains("高等学校を選"))return Stage.SCHOOL;
  if(t.contains("情報")&&(t.contains("正しいですか")||t.contains("正しいでしょうか")))return Stage.SUMMARY;
  return Stage.UNKNOWN;
 }
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
  case SCHOOL_GUIDE:return t.equals("わかった！");
  case SUMMARY:return t.equals("登録する");
  case TERMS:return t.startsWith("参加規約に同意して進む");
  case INTRO:return introLabel(t);
  case YEAR:return t.matches("(?:19[0-9]{2}|20[0-2][0-9])年");
  case SEX:return Arrays.asList("男","女","男性","女性","その他","回答しない").contains(t);
  case PREFECTURE:return PREFECTURES.contains(t);
  case SCHOOL_INITIAL:return t.matches("[あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわ]");
  case SCHOOL:return Text.clean(t).matches("[^\\n]{1,60}(?:高等学校|高校)(?:[\\p{L}\\p{N}・ー]{0,30}キャンパス)?(?:[（(](?:公立|私立|国立)[）)])?")&&!t.contains("頭文字");
  default:return false;
 }}
 /** Only used after the UI proves the school-selection step, never for global stage inference. */
 public static boolean schoolCandidate(String label){
  String t=Text.clean(label);if(t.isEmpty()||t.length()>100||forbidden(t)||promptStage(t)!=Stage.UNKNOWN)return false;
  if(t.matches("[A-D]")||t.matches("[あかさたなはまやらわ]行")||t.matches("[0-9]+(?:年|年代)?")||t.matches("(?s).*[。!?！？].*")||t.contains("http")||t.contains("クーポン")||t.contains("バーコード")||t.contains("同意")||t.contains("規約")||t.contains("承諾")||t.contains("了承")||t.contains("利用条件")||t.contains("第")&&t.contains("問"))return false;
  for(Stage s:Stage.values())if(s!=Stage.SCHOOL&&s!=Stage.UNKNOWN&&eligible(s,t))return false;
  return !Arrays.asList("はい","いいえ","開始","続きから","次へ","確認","終了","閉じる","メニュー","送信","OK","回答","回答する","問題を見る","もう一度挑戦する","結果を見る","正解","不正解").contains(t);
 }
 public static String chooseSchoolCandidate(List<String> labels,Random random){List<String> safe=new ArrayList<>();for(String s:labels)if(schoolCandidate(s)&&!safe.contains(s))safe.add(s);return safe.isEmpty()?null:safe.get(random.nextInt(safe.size()));}
 /** A changed viewport is not evidence that a sent registration choice was accepted.
  * INTRO has successive conversational replies within one stage; retain its distinct-set progression. */
 public static boolean selectionPending(String pending,Stage stage,String currentKey){
  if(pending.equals(currentKey)||(stage!=Stage.INTRO&&!pending.isEmpty()&&pending.startsWith(stage.name()+":")))return true;
  // Scrolling may reveal already answered county/initial cards. Only this observed sequence is ordered.
  if(pending.startsWith("SCHOOL:"))return stage==Stage.SCHOOL_INITIAL||stage==Stage.PREFECTURE;
  return pending.startsWith("SCHOOL_INITIAL:")&&stage==Stage.PREFECTURE;
 }
 /** Search can reveal the next prompt while retaining a sent choice, but never overrides an active quiz. */
 public static boolean canSearch(Stage stage,boolean prompted,boolean hasPending,boolean quizActive){
  return !quizActive&&(hasPending||stage!=Stage.UNKNOWN&&prompted);
 }
 public static String rightIntro(Map<String,Integer> centres){String chosen=null;int right=Integer.MIN_VALUE;for(Map.Entry<String,Integer> e:centres.entrySet())if(eligible(Stage.INTRO,e.getKey())&&e.getValue()>right){right=e.getValue();chosen=e.getKey();}return chosen;}
 public static String choose(Stage stage,List<String> labels,Random random){List<String> safe=new ArrayList<>();for(String s:labels)if(eligible(stage,s)&&!safe.contains(s))safe.add(s);if(safe.isEmpty())return null;return safe.get(random.nextInt(safe.size()));}
}
