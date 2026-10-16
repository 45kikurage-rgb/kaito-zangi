import jp.kossacktouch.core.*;import java.util.*;
/** Synthetic snapshots based on the submitted support-school image; no device success is implied. */
public class Registration28Tests {
 static int count;static void check(boolean b){count++;if(!b)throw new AssertionError("registration28 "+count);}
 static UiModel.Node n(int p,String id,String text,boolean v,boolean e,boolean c){return new UiModel.Node(p,"jp.naver.line.androif:id/"+id,text,v,e,c);}
 static int msg(List<UiModel.Node> a,String t,boolean v){int p=a.size();a.add(n(0,"chat_ui_row_text_message","",v,true,false));a.add(n(p,"chat_ui_message_text",t,v,true,true));return p;}
 static int card(List<UiModel.Node> a,String... labels){int p=a.size();a.add(n(0,"chat_ui_row_flex_message_frame","",true,true,false));for(String t:labels)a.add(n(p,"choice",t,true,true,true));return p;}
 static List<UiModel.Node> base(){List<UiModel.Node> a=new ArrayList<>();a.add(n(-1,"chat_ui_list","",true,true,false));msg(a,"高校の頭文字は何かの？",true);card(a,"あ","い","く");msg(a,"く",true);return a;}
 public static void main(String[] args){
  for(String school:Arrays.asList("秋田県立栗田支援学校高等部（公立）","N高等学校仙台キャンパス（私立）","第一学園専攻科","Academy North","未来学舎")){
   List<UiModel.Node> a=base();int p=card(a,school,"一つ前に戻る");UiModel m=new UiModel(a);
   check(m.schoolSelectionContext());check(m.registrationControls(Registration.Stage.SCHOOL).keySet().equals(new LinkedHashSet<>(Arrays.asList(school))));
   check(school.equals(Registration.chooseSchoolCandidate(new ArrayList<>(m.registrationControls(Registration.Stage.SCHOOL).keySet()),new Random(1))));
   check(m.registrationControls(Registration.Stage.SCHOOL_INITIAL).isEmpty());check(Registration.selectionPending("SCHOOL:1",Registration.Stage.SCHOOL,"SCHOOL:2"));
   a.set(2,n(1,"chat_ui_message_text","高校の頭文字は何かの？",false,true,true));check(new UiModel(a).schoolSelectionContext());
   a.set(p+1,n(p,"choice",school,false,true,true));check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).isEmpty());
   a.set(p+1,n(p,"choice",school,true,false,true));check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).isEmpty());
   a.set(p+1,n(p,"choice",school,true,true,false));check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).isEmpty());
   a.set(p+1,n(p,"choice","",true,true,true));a.add(n(p+1,"label",school,true,true,false));check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).size()==1);
  }
  List<UiModel.Node> a=base();card(a,"第一学舎","第二学舎","一つ前に戻る");UiModel m=new UiModel(a);check(m.registrationControls(Registration.Stage.SCHOOL).size()==2);for(int i=0;i<20;i++)check(Arrays.asList("第一学舎","第二学舎").contains(Registration.chooseSchoolCandidate(new ArrayList<>(m.registrationControls(Registration.Stage.SCHOOL).keySet()),new Random(i))));
  for(String bad:Arrays.asList("一つ前に戻る","変更する","キャンセル","同意する","利用規約に同意する","了承して進む","承諾する","利用条件を見る","はい","いいえ","A","Ｂ","C","D","1","69","あ","く","あ行","か行","さ行","1990年","1980年代","男性","女性","その他","回答しない","秋田県","参加登録する","参加規約に同意して進む","登録する","わかった！","問題を見る","もう一度挑戦する","クーポンをゲットする","クーポンを表示する","https://example.com","説明文です。","高校の頭文字は何かの？")){
   check(!Registration.schoolCandidate(bad));a=base();card(a,bad,"一つ前に戻る");check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).isEmpty());
  }
  a=base();card(a,"第一学舎","第一学舎","一つ前に戻る");check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).isEmpty());
  a=base();msg(a,"第一学舎",true);a.add(n(-1,"chat_ui_quick_reply_item_root","一つ前に戻る",true,true,true));check(!new UiModel(a).schoolSelectionContext());
  a=base();int p=card(a,"第一学舎");a.set(p,n(0,"chat_ui_row_flex_message_frame","",true,true,true));a.set(p+1,n(p,"label","第一学舎",true,true,false));a.add(n(-1,"chat_ui_quick_reply_item_root","一つ前に戻る",true,true,true));check(!new UiModel(a).schoolSelectionContext());
  a=new ArrayList<>();a.add(n(-1,"chat_ui_list","",true,true,false));card(a,"第一学舎","一つ前に戻る");msg(a,"高校の頭文字は何かの？",true);msg(a,"く",true);check(!new UiModel(a).schoolSelectionContext());
  // No displayed reply/new card sequence: an unknown button never becomes a school globally.
  a=new ArrayList<>();a.add(n(-1,"chat_ui_list","",true,true,false));msg(a,"高校の頭文字は何かの？",true);card(a,"第一学舎","一つ前に戻る");check(!new UiModel(a).schoolSelectionContext());
  a=base();card(a,"第一学舎","一つ前に戻る");msg(a,"生まれた年を選んでください",true);check(!new UiModel(a).schoolSelectionContext());
  a=base();card(a,"第1問\nどれ？\n[A]一\n[B]二\n[C]三\n[D]四","一つ前に戻る");check(!new UiModel(a).schoolSelectionContext());
  // Dedicated quick replies use the same context, without accepting a received/sent bubble.
  a=base();a.add(n(-1,"chat_ui_quick_reply_item_root","第一学舎",true,true,true));a.add(n(-1,"chat_ui_quick_reply_item_root","一つ前に戻る",true,true,true));check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).size()==1);
  // Explicit school prompt is sufficient; a separate back control need not be visible.
  a=base();msg(a,"高校を選んでください",true);card(a,"第一学舎");check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).size()==1);
  System.out.println("PASS "+count+" code28 school-choice checks: name-independent contextual candidates, observed support school, one/two choices, hidden prompt, dedicated controls and stale/quiz/duplicate rejection.");
 }
}
