import jp.kossacktouch.core.*;import java.util.*;
public class Progress18Tests {
 static int count;static void check(boolean ok){count++;if(!ok)throw new AssertionError("progress18 "+count);}
 static NativeComposer.Node node(int parent,String id,String label,boolean edit,boolean click){return new NativeComposer.Node(parent,"jp.naver.line.andronj:id/"+id,label,true,true,edit,false,click,false);}
 public static void main(String[] args){
  // Actual Android16/Nothing input hierarchy from the owner's code17 log; send label is a fixture, not a device observation.
  List<NativeComposer.Node> ns=new ArrayList<>(Arrays.asList(node(-1,"chat_ui_message","",false,false),node(0,"chat_ui_message_container","",false,false),node(1,"chat_ui_message_edit","",true,true),node(0,"unknown_send","送信",false,true)));
  NativeComposer c=new NativeComposer(ns,"jp.naver.line.andronj");check(c.inputs().equals(Arrays.asList(2)));check(c.send()==3);
  check(new NativeComposer(ns,"jp.naver.line.android").inputs().isEmpty());
  ns.add(node(-1,"toolbar","送信",false,true));check(new NativeComposer(ns,"jp.naver.line.andronj").send()==3);
  ns.add(node(0,"duplicate","送信",false,true));check(new NativeComposer(ns,"jp.naver.line.andronj").send()==-1);
  ns.remove(ns.size()-1);ns.set(3,node(0,"unknown_send","",false,true));check(new NativeComposer(ns,"jp.naver.line.andronj").send()==-1);
  check(!FinalSubmission.draftSafe("既存下書き","14",false));check(FinalSubmission.draftSafe("14","14",true));
  ElementMonitor m=new ElementMonitor();check(!m.observe("ready",0));check(!m.observe("ready",499));check(m.observe("ready",500));check(!m.observe("changed",1000));check(m.observe("changed",1500));
  RevealSearch r=new RevealSearch();int swipes=0,polls=0;for(long t=0;t<26000;t+=500){RevealSearch.Step step=r.missing(t);polls++;if(swipes>=10){check(step==RevealSearch.Step.EXHAUSTED);continue;}check(step!=RevealSearch.Step.EXHAUSTED);if(step==RevealSearch.Step.DOWN||step==RevealSearch.Step.UP){check(step==(swipes%2==0?RevealSearch.Step.DOWN:RevealSearch.Step.UP));r.moved();swipes++;}}
  check(polls==52);check(swipes==10);check(r.missing(26000)==RevealSearch.Step.EXHAUSTED);
  r.reset();check(r.missing(30000)==RevealSearch.Step.WAIT);check(r.missing(30500)==RevealSearch.Step.WAIT);check(r.missing(31000)==RevealSearch.Step.DOWN);
  Question q=Question.parse("第5問\nハンドボールとホッケーをa,bとする。倍数の確率は何%か。"),old=Question.parse("第3問\nボクシングで反則となる行為はどれか？\n[A]一\n[B]二\n[C]三\n[D]四");check(q!=null&&old!=null);
  FinalAnchor a=new FinalAnchor();a.bind(q);check(a.mismatch(null));check(!a.result(null,false,true));check(!a.result(null,true,false));check(a.result(null,true,true));check(a.result(q,true,true));check(!a.result(old,true,true));
  check(CouponFlow.next("",true,false).equals("クーポンをゲットする"));check(CouponFlow.next("get_clicked",true,false).isEmpty());check(CouponFlow.next("get_clicked",false,true).equals("クーポンを表示する"));check(CouponFlow.next("display_clicked",true,true).isEmpty());
  System.out.println("PASS "+count+" code18 checks: observed cloned-LINE input, guarded send, faster polling, extended reveal and submitted-Q5 result progression.");
 }
}
