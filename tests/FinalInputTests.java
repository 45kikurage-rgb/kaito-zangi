import jp.kossacktouch.core.*;import java.util.*;
public class FinalInputTests {
 static int c;static void check(boolean b){c++;if(!b)throw new AssertionError("final input "+c);}
 static NativeComposer.Node n(int p,String id,String text,boolean visible,boolean enabled,boolean edit,boolean password,boolean click,boolean web){return new NativeComposer.Node(p,id.isEmpty()?"":"jp.naver.line.androif:id/"+id,text,visible,enabled,edit,password,click,web);}
 static List<NativeComposer.Node> composer(){return new ArrayList<>(Arrays.asList(n(-1,"chat_ui_custom_expandable_input_container","",true,true,false,false,false,false),n(0,"different_input_id","",true,true,true,false,false,false),n(0,"different_button_id","送信",true,false,false,false,true,false)));}
 public static void main(String[] args){
  List<NativeComposer.Node> ns=composer();check(new NativeComposer(ns,"jp.naver.line.androif").inputs().equals(Arrays.asList(1)));check(new NativeComposer(ns,"jp.naver.line.androif").send()==-1);
  ns.set(1,n(0,"different_input_id","93",true,true,true,false,false,false));ns.set(2,n(0,"different_button_id","送信",true,true,false,false,true,false));check(new NativeComposer(ns,"jp.naver.line.androif").send()==2);check(FinalSubmission.draftSafe("93","93",true));check(!FinalSubmission.draftSafe("元の下書き","93",true));
  check(new NativeComposer(ns,"jp.naver.line.android").inputs().isEmpty());
  ns.add(n(-1,"toolbar_send","送信",true,true,false,false,true,false));check(new NativeComposer(ns,"jp.naver.line.androif").send()==2);
  ns.add(n(0,"duplicate_send","送信",true,true,false,false,true,false));check(new NativeComposer(ns,"jp.naver.line.androif").send()==-1);
  ns=composer();ns.add(n(0,"second_edit","",true,true,true,false,false,false));check(new NativeComposer(ns,"jp.naver.line.androif").inputs().size()==2);check(new NativeComposer(ns,"jp.naver.line.androif").send()==-1);
  for(int variant=0;variant<5;variant++){ns=composer();ns.set(1,n(0,"different_input_id","",variant!=0,variant!=1,variant!=2,variant==3,false,variant==4));check(new NativeComposer(ns,"jp.naver.line.androif").inputs().isEmpty());}
  ns=composer();ns.set(0,n(-1,"chat_ui_custom_expandable_input_container","",false,true,false,false,false,false));check(new NativeComposer(ns,"jp.naver.line.androif").inputs().isEmpty());
  ns=composer();ns.set(0,n(-1,"chat_ui_custom_expandable_input_container","",true,true,false,false,false,true));check(new NativeComposer(ns,"jp.naver.line.androif").inputs().isEmpty());
  ns=composer();ns.set(0,n(-1,"other_app_editor","",true,true,false,false,false,false));check(new NativeComposer(ns,"jp.naver.line.androif").inputs().isEmpty());
  ns=composer();ns.set(1,n(0,"chat_ui_input_edit","",true,true,true,false,false,false));check(new NativeComposer(ns,"jp.naver.line.androif").inputs().equals(Arrays.asList(1)));
  ns.set(2,n(0,"chat_ui_input_send","",true,true,false,false,true,false));check(new NativeComposer(ns,"jp.naver.line.androif").send()==2);
  ns.set(2,n(0,"different_button_id","録音",true,true,false,false,true,false));check(new NativeComposer(ns,"jp.naver.line.androif").send()==-1);
  String observed="アマチュアボクシングの「女子エリートWB新階級」における、「ライト級の最大体重(kg)」をa、「ウェルター級の最大体重(kg)」をbとする。1からaまでの自然数から異なる2つの数字をランダムで選んだ時、2数の差がbの正の約数ではない確率は何%か。小数点以下を四捨五入して答えよ。";
  check(MathEngine.solve(observed).answer.equals("91"));
  Question current=Question.parse("第5問\n女子エリートWB新階級のライト級とウェルター級をa,bとする。異なる2数の差の確率は何%か。");Question old=Question.parse("第5問\nハンドボールとホッケーをa,bとする。倍数の確率は何%か。");Question other=Question.parse("第3問\nボクシングで反則となる行為はどれか？\n[A]一\n[B]二\n[C]三\n[D]四");
  check(current!=null&&old!=null&&other!=null);FinalAnchor a=new FinalAnchor();check(!a.mismatch(old));a.bind(other);check(!a.mismatch(current));a.bind(current);check(!a.mismatch(current));check(a.mismatch(old));check(a.mismatch(other));check(a.mismatch(null));a.bind(old);check(a.mismatch(old));check(a.recoverOnce());check(!a.recoverOnce());check(!a.mismatch(current));a.reset();check(!a.mismatch(old));a.bind(old);check(!a.mismatch(old));check(a.mismatch(current));check(a.recoverOnce());
  RevealSearch r=new RevealSearch();check(r.missing(1000)==RevealSearch.Step.WAIT);check(r.missing(2000)==RevealSearch.Step.UP);r.moved();check(r.missing(3000)==RevealSearch.Step.WAIT);check(r.missing(4000)==RevealSearch.Step.DOWN);r.moved();r.found();check(r.pair()==2);check(r.missing(5000)==RevealSearch.Step.WAIT);check(r.missing(6000)==RevealSearch.Step.UP);
  System.out.println("PASS "+c+" final-input checks: native composer boundaries, changed IDs, draft/send validation, ambiguous and WebView rejection, current-Q5 anchor and bounded history recovery.");
 }
}
