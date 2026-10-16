import jp.kossacktouch.core.*;import java.util.*;
/** Synthetic snapshots reproduce the supplied photos/video using LINE IDs from actual fixtures. */
public class Registration27Tests {
 static int count;static void check(boolean b){count++;if(!b)throw new AssertionError("registration27 "+count);}
 static UiModel.Node n(int p,String id,String text,boolean visible,boolean click){return new UiModel.Node(p,"jp.naver.line.androif:id/"+id,text,visible,true,click);}
 static List<UiModel.Node> county(String selected){List<UiModel.Node> ns=new ArrayList<>();ns.add(n(-1,"chat_ui_list","",true,false));ns.add(n(0,"chat_ui_row_flex_message_frame","",true,false));ns.add(n(1,"label","北海道・東北地方",true,false));ns.add(n(1,"label","山形県",true,true));ns.add(n(1,"label","愛知県",true,true));ns.add(n(0,"chat_ui_row_text_message","",true,false));ns.add(n(5,"chat_ui_message_text",selected,true,true));return ns;}
 static int prompt(List<UiModel.Node> ns,String text,boolean visible){int p=ns.size();ns.add(n(0,"chat_ui_row_text_message","",visible,false));ns.add(n(p,"chat_ui_message_text",text,visible,true));return p;}
 static int card(List<UiModel.Node> ns,String... labels){int p=ns.size();ns.add(n(0,"chat_ui_row_flex_message_frame","",true,false));for(String t:labels)ns.add(n(p,"label",t,true,true));return p;}
 public static void main(String[] args){
  for(String selected:Arrays.asList("愛知県","山形県")){
   List<UiModel.Node> ns=county(selected);UiModel m=new UiModel(ns);check(Registration.stage(m.registrationPrompt())==Registration.Stage.PREFECTURE);
   prompt(ns,"高校の頭文字は何かの？",true);m=new UiModel(ns);
   check(Registration.stage(m.registrationPrompt())==Registration.Stage.SCHOOL_INITIAL);check(m.registrationPromptAfterCard());
   check(m.registrationControls(Registration.Stage.PREFECTURE).isEmpty());check(m.registrationControls(Registration.Stage.SCHOOL_INITIAL).isEmpty());
   int p=card(ns,"あ","い");ns.set(p+1,n(p,"label","あ",false,true));ns.set(p+2,n(p,"label","い",false,true));m=new UiModel(ns);
   check(Registration.stage(m.registrationPrompt())==Registration.Stage.SCHOOL_INITIAL);check(m.registrationControls(Registration.Stage.SCHOOL_INITIAL).isEmpty());
   ns.set(p+1,n(p,"label","あ",true,true));m=new UiModel(ns);List<String> labels=new ArrayList<>(m.registrationControls(Registration.Stage.SCHOOL_INITIAL).keySet());
   check(labels.equals(Arrays.asList("あ")));check("あ".equals(Registration.choose(Registration.Stage.SCHOOL_INITIAL,labels,new Random(1))));
   ns.set(p+2,n(p,"label","い",true,true));m=new UiModel(ns);labels=new ArrayList<>(m.registrationControls(Registration.Stage.SCHOOL_INITIAL).keySet());
   check(labels.equals(Arrays.asList("あ","い")));for(int i=0;i<20;i++)check(labels.contains(Registration.choose(Registration.Stage.SCHOOL_INITIAL,labels,new Random(i))));
   check(m.registrationControls(Registration.Stage.PREFECTURE).isEmpty());
   prompt(ns,"高校を選んでくれい。",true);m=new UiModel(ns);check(m.registrationControls(Registration.Stage.SCHOOL_INITIAL).isEmpty());
   card(ns,"山形県立山形東高等学校（公立）");m=new UiModel(ns);check(m.registrationControls(Registration.Stage.SCHOOL).size()==1);
  }
  List<UiModel.Node> guide=county("愛知県");prompt(guide,"高校がある都道府県を選んでくれい。",true);guide.add(n(-1,"chat_ui_quick_reply_item_root","わかった！",true,true));check(new UiModel(guide).registrationControls(Registration.Stage.SCHOOL_GUIDE).size()==1);
  // Old prefecture prompt remains visible while the new initial prompt has scrolled out.
  for(int choiceCount:new int[]{1,2}){
   List<UiModel.Node> stale=county("愛知県");prompt(stale,"高校がある都道府県を選んでくれい。",true);prompt(stale,"高校の頭文字は何かの？",false);
   card(stale,choiceCount==1?new String[]{"あ"}:new String[]{"あ","い"});UiModel freshCard=new UiModel(stale);
   check(!freshCard.registrationPromptAfterCard());check(Registration.stage(freshCard.registrationPrompt())==Registration.Stage.UNKNOWN);
   check(freshCard.registrationControls(Registration.Stage.PREFECTURE).isEmpty());check(freshCard.registrationControls(Registration.Stage.SCHOOL_INITIAL).size()==choiceCount);
   // Real LINE often attaches click to a parent, not to the label.
   int label=stale.size()-1;UiModel.Node prior=stale.get(label);stale.set(label,n(prior.parent,"choice_wrapper","",true,true));stale.add(n(label,"label",prior.text,true,false));
   check(new UiModel(stale).registrationControls(Registration.Stage.SCHOOL_INITIAL).size()==choiceCount);
  }
  // Supplied 16427.mp4: a single campus choice after initial 「え」 was rejected by the old suffix rule.
  String campus="Ｎ高等学校仙台キャンパス（私\n立）";
  check(Registration.eligible(Registration.Stage.SCHOOL,campus));
  check(Registration.eligible(Registration.Stage.SCHOOL,"N高等学校仙台キャンパス（私立）"));
  for(String rejected:Arrays.asList("高校の頭文字は何かの？","高校を選んでください","N高等学校仙台キャンパスを選択","N高等学校仙台キャンパスについて","N高等学校仙台キャンパス（私立）へ戻る"))check(!Registration.eligible(Registration.Stage.SCHOOL,rejected));
  List<UiModel.Node> school=county("宮城県");prompt(school,"高校の頭文字は何かの？",true);card(school,"あ","え");int single=card(school,campus);UiModel schoolCard=new UiModel(school);
  check(Registration.stage(schoolCard.registrationPrompt())==Registration.Stage.SCHOOL);check(schoolCard.registrationControls(Registration.Stage.SCHOOL).size()==1);
  check(campus.equals(Registration.choose(Registration.Stage.SCHOOL,new ArrayList<>(schoolCard.registrationControls(Registration.Stage.SCHOOL).keySet()),new Random(2))));
  school.set(single+1,n(single,"label",campus,false,true));check(new UiModel(school).registrationControls(Registration.Stage.SCHOOL).isEmpty());
  school.set(single+1,n(single,"label",campus,true,false));check(new UiModel(school).registrationControls(Registration.Stage.SCHOOL).isEmpty());
  // A plain received/sent school-name bubble is not a candidate, even though its node is clickable.
  school=county("宮城県");prompt(school,"高校の頭文字は何かの？",true);prompt(school,campus,true);check(new UiModel(school).registrationControls(Registration.Stage.SCHOOL).isEmpty());
  // A stale or hidden prompt must not override a later quiz card.
  List<UiModel.Node> ns=county("愛知県");prompt(ns,"高校の頭文字は何かの？",true);card(ns,"第2問\n2026年の都道府県は？\n[A]一\n[B]二\n[C]三\n[D]四");UiModel m=new UiModel(ns);
  check(m.question!=null&&m.question.number==2);check(m.visibleQuestion());check(!m.registrationPromptAfterCard());check(Registration.quizPriority(m.visibleQuestion(),m.qRow==m.flex.size()-1,false));
  ns=county("愛知県");prompt(ns,"高校の頭文字は何かの？",false);m=new UiModel(ns);check(!m.registrationPromptAfterCard());check(Registration.stage(m.registrationPrompt())==Registration.Stage.PREFECTURE);
  for(String text:Arrays.asList("愛知県","山形県","山形東高等学校","都道府県","高校","第2問 都道府県について"))check(Registration.promptStage(text)==Registration.Stage.UNKNOWN);
  // New ordinary registration prompt outranks a still-visible old quiz card.
  ns=new ArrayList<>();ns.add(n(-1,"chat_ui_list","",true,false));card(ns,"第2問\n都道府県は？\n[A]一\n[B]二\n[C]三\n[D]四");prompt(ns,"高校の頭文字は何かの？",true);m=new UiModel(ns);
  check(m.visibleQuestion());check(m.registrationPromptAfterCard());check(Registration.stage(m.registrationPrompt())==Registration.Stage.SCHOOL_INITIAL);
  card(ns,"第3問\n学校は？\n[A]一\n[B]二\n[C]三\n[D]四");m=new UiModel(ns);check(m.question.number==3);check(!m.registrationPromptAfterCard());
  for(Registration.Stage st:new Registration.Stage[]{Registration.Stage.PREFECTURE,Registration.Stage.SCHOOL_INITIAL,Registration.Stage.SCHOOL,Registration.Stage.YEAR,Registration.Stage.SEX,Registration.Stage.TERMS,Registration.Stage.SUMMARY}){
   check(Registration.selectionPending(st.name()+":1",st,st.name()+":1"));check(Registration.selectionPending(st.name()+":1",st,st.name()+":2"));check(!Registration.selectionPending("",st,st.name()+":2"));
  }
  check(!Registration.selectionPending("PREFECTURE:1",Registration.Stage.SCHOOL_INITIAL,"SCHOOL_INITIAL:2"));
  check(Registration.selectionPending("INTRO:1",Registration.Stage.INTRO,"INTRO:1"));check(!Registration.selectionPending("INTRO:1",Registration.Stage.INTRO,"INTRO:2"));
  // Breadth-first fixture order: compare tree traversal order, not raw leaf indices.
  ns=new ArrayList<>();ns.add(n(-1,"chat_ui_list","",true,false));ns.add(n(0,"chat_ui_row_text_message","",true,false));ns.add(n(0,"chat_ui_row_flex_message_frame","",true,false));ns.add(n(1,"chat_ui_message_text","高校の頭文字は何かの？",true,true));ns.add(n(2,"label","あ",true,true));m=new UiModel(ns);check(!m.registrationPromptAfterCard());check(m.registrationControls(Registration.Stage.SCHOOL_INITIAL).size()==1);
  RevealSearch search=new RevealSearch();long now=0;int steps=0;for(;now<26000;now+=500){RevealSearch.Step s=search.missing(now,true);if(s==RevealSearch.Step.UP){check(s==RevealSearch.Step.UP);steps++;search.moved();}else check(s==(steps>=10?RevealSearch.Step.EXHAUSTED:RevealSearch.Step.WAIT));}check(steps==10);check(search.missing(26000,true)==RevealSearch.Step.EXHAUSTED);
  search=new RevealSearch();check(search.missing(0)==RevealSearch.Step.WAIT);check(search.missing(500)==RevealSearch.Step.WAIT);check(search.missing(1000)==RevealSearch.Step.DOWN);
  float[] path=RevealSearch.path(100,1000,48,RevealSearch.Step.UP);check(path[0]-path[1]==48);
  System.out.println("PASS "+count+" code27 registration checks: ordinary prompt freshness, old-card exclusion, one/two choices, bounded upward reveal and quiz preservation.");
 }
}
