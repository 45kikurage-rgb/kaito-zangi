import jp.kossacktouch.core.*;import java.util.*;
public class Diagnostic17Tests {
 static int count=0;static void check(boolean value){count++;if(!value)throw new AssertionError("diag17 "+count);}
 static UiModel screen(String text,String... buttons){List<UiModel.Node> nodes=new ArrayList<>();nodes.add(new UiModel.Node(-1,"p:id/chat_ui_row_flex_message_frame",text,true,true,false));for(String b:buttons)nodes.add(new UiModel.Node(-1,"p:id/chat_ui_quick_reply_item_root",b,true,true,true));return new UiModel(nodes);}
 public static void main(String[] args){
  for(String text:new String[]{"5問連続正解に7回失敗すると引換クーポンは貰えない", "5問連続正解者の多い高校順に全国高校ランキング", "キャンペーンでは5問連続正解するとクーポンがもらえる", "クーポンを表示するボタンの説明", "見事難問をクリアできるかな？"})check(!QuizFlow.complete(screen(text)));
  check(QuizFlow.complete(screen("正解！\n5問連続正解！")));
  check(QuizFlow.complete(screen("見事難問をクリア！")));
  check(QuizFlow.complete(screen("おめでとう", "クーポンをゲットする")));
  check(!QuizFlow.complete(screen("5問連続正解！","問題を見る")));
  check(Registration.eligible(Registration.Stage.INTRO,"初めてだけど頑張ります！"));
  check(Registration.eligible(Registration.Stage.SCHOOL_GUIDE,"わかった！"));
  check(!Registration.eligible(Registration.Stage.PREFECTURE,"わかった！"));
  check(!Registration.eligible(Registration.Stage.SCHOOL_GUIDE,"変更する"));
  Map<String,Integer> positions=new LinkedHashMap<>();positions.put("スポーツが得意な友達に相談しよう",100);positions.put("が、がんばる..",300);check("が、がんばる..".equals(Registration.rightIntro(positions)));
  positions.put("変更する",500);positions.put("送信",600);check("が、がんばる..".equals(Registration.rightIntro(positions)));
  positions.put("スポーツが得意な友達に相談しよう",400);check("スポーツが得意な友達に相談しよう".equals(Registration.rightIntro(positions)));
  check("登録する".equals(Registration.choose(Registration.Stage.SUMMARY,Arrays.asList("登録する","変更する"),new Random(1))));
  check("わかった！".equals(Registration.choose(Registration.Stage.SCHOOL_GUIDE,Arrays.asList("わかった！","一つ前に戻る"),new Random(1))));
  System.out.println("PASS "+count+" code17 regression checks: observed intro false completion, school guide, right intro and registration preservation.");
 }
}
