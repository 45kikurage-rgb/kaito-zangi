import jp.kossacktouch.core.*;import java.util.*;
/** Synthetic trees reproduce candidate/back-card layouts; screenshots do not establish actual IDs. */
public class SchoolChoice29Tests {
 static int count;static void check(boolean v){count++;if(!v)throw new AssertionError("school29 "+count);}
 static UiModel.Node n(int p,String id,String t,boolean visible,boolean clickable){return new UiModel.Node(p,"jp.naver.line.androif:id/"+id,t,visible,true,clickable);}
 static void msg(List<UiModel.Node> a,String t){int p=a.size();a.add(n(0,"chat_ui_row_text_message","",true,false));a.add(n(p,"chat_ui_message_text",t,true,true));}
 static int card(List<UiModel.Node> a,String... labels){int p=a.size();a.add(n(0,"chat_ui_row_flex_message_frame","",true,false));for(String label:labels)a.add(n(p,"button",label,true,true));return p;}
 static List<UiModel.Node> base(String initial){List<UiModel.Node> a=new ArrayList<>();a.add(n(-1,"chat_ui_list","",true,false));msg(a,"高校の頭文字は何かの？");card(a,"あ","い","く","け");msg(a,initial);return a;}
 public static void main(String[] args){
  String support="岩手県立気仙光陵支援学校高等部（公立）";
  String[] schools={"クラーク記念国際高等学校単位制名古屋校（私立）","クラーク記念国際高等学校名古屋校（私立）","クラーク記念国際高等学校愛知校（私立）"};
  for(boolean separate:new boolean[]{false,true}){
   List<UiModel.Node> a=base("け");card(a,support);if(separate)card(a,"一つ前に戻る");else a.add(n(-1,"chat_ui_quick_reply_item_root","一つ前に戻る",true,true));
   UiModel m=new UiModel(a);check(m.schoolSelectionContext());check(m.registrationControls(Registration.Stage.SCHOOL).size()==1);check(m.registrationControls(Registration.Stage.SCHOOL_INITIAL).isEmpty());
   check(support.equals(Registration.chooseSchoolCandidate(new ArrayList<>(m.registrationControls(Registration.Stage.SCHOOL).keySet()),new Random(1))));
   check(!Registration.selectionPending("SCHOOL_INITIAL:123",Registration.Stage.SCHOOL,"SCHOOL:456"));check(Registration.selectionPending("SCHOOL:123",Registration.Stage.SCHOOL,"SCHOOL:456"));
  }
  for(boolean eachCard:new boolean[]{false,true}){
   List<UiModel.Node> a=base("く");if(eachCard)for(String school:schools)card(a,school);else card(a,schools);card(a,"一つ前に戻る");
   UiModel m=new UiModel(a);check(m.schoolSelectionContext());check(m.registrationControls(Registration.Stage.SCHOOL).size()==3);for(int seed=0;seed<20;seed++)check(Arrays.asList(schools).contains(Registration.chooseSchoolCandidate(new ArrayList<>(m.registrationControls(Registration.Stage.SCHOOL).keySet()),new Random(seed))));
  }
  // New distinct stages or unknown latest cards cannot expose an older school list for selection.
  for(String blocker:Arrays.asList("2000年","男性","秋田県","あ","A","同意する","クーポンを表示する")){
   List<UiModel.Node> a=base("く");card(a,schools);card(a,blocker);card(a,"一つ前に戻る");check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).isEmpty());
  }
  List<UiModel.Node> a=base("く");card(a,schools);int newest=card(a,"新しい説明です。");a.set(newest+1,n(newest,"label","新しい説明です。",true,false));card(a,"一つ前に戻る");check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).isEmpty());
  a=base("く");card(a,schools[0]);msg(a,"高校の頭文字は何かの？");msg(a,"け");card(a,support);card(a,"一つ前に戻る");check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).keySet().equals(new LinkedHashSet<>(Arrays.asList(support))));
  a=base("け");int p=card(a,support);a.set(p+1,n(p,"button",support,false,true));card(a,"一つ前に戻る");check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).isEmpty());
  a=base("け");msg(a,support);card(a,"一つ前に戻る");check(!new UiModel(a).schoolSelectionContext());
  a=base("く");card(a,schools[0]);card(a,schools[0]);card(a,"一つ前に戻る");check(new UiModel(a).registrationControls(Registration.Stage.SCHOOL).isEmpty());
  System.out.println("PASS "+count+" code29 school-card checks: observed one/three names, synthetic separate Return card, latest contiguous candidates, stage progression and stale/ordinary/duplicate rejection.");
 }
}
