import jp.kossacktouch.core.*;import java.util.*;
public class ForegroundTests {
 static int count;static void check(boolean b){count++;if(!b)throw new AssertionError("foreground/last question "+count);}
 static ForegroundWindow.Window w(int t,int l,boolean a,boolean f,String p){return new ForegroundWindow.Window(t,l,a,f,p);}
 static final String LINE="jp.naver.line.androif";
 static UiModel.Node n(int p,String id,String text,boolean v){return new UiModel.Node(p,LINE+":id/"+id,text,v,true,false);}
 static void row(List<UiModel.Node> ns,String text,boolean v){int p=ns.size();ns.add(n(-1,"chat_ui_row_flex_message_frame","",v));ns.add(n(p,"text",text,v));}
 public static void main(String[] args){
  ForegroundWindow.Window line=w(1,0,true,true,LINE),ime=w(2,4,true,false,"keyboard.without.special.package.words"),overlay=w(4,9,true,false,"jp.kossacktouch.app"),bar=w(3,5,false,false,"com.android.systemui");
  check(ForegroundWindow.select(Arrays.asList(line))==0);
  check(ForegroundWindow.select(Arrays.asList(ime,w(1,0,false,true,LINE),overlay,bar))==1);
  check(ForegroundWindow.select(Arrays.asList(w(1,0,false,true,LINE),w(1,3,true,true,"com.android.chrome"),ime))<0);
  check(ForegroundWindow.select(Arrays.asList(line,w(3,8,true,true,"com.android.permissioncontroller")))<0);
  check(ForegroundWindow.select(Arrays.asList(line,w(1,6,true,true,"android")))<0);
  check(ForegroundWindow.select(Arrays.asList(line,w(4,8,true,true,"other.accessibility.service")))<0);
  check(ForegroundWindow.select(Arrays.asList(w(1,0,false,false,LINE),ime))<0);
  check(ForegroundWindow.select(Arrays.asList(ime))<0);
  check(ForegroundWindow.select(Collections.emptyList())<0);
  check(ForegroundWindow.select(Arrays.asList(line,w(1,0,true,true,LINE)))<0);
  check(ForegroundWindow.select(Arrays.asList(line,w(1,2,true,true,"jp.naver.line.android")))==1);
  check(ForegroundWindow.select(Arrays.asList(w(1,0,true,true,"fake.line.app"),ime))<0);
  String old="第5問\n「サッカー」「ビーチサッカー」の倍数の確率は何%か。";
  String current="第5問\n「ホッケー」「ビーチサッカー」の各1チーム(スターティングメンバー)の選手をランダムで1列に並べる。両端が同じスポーツの選手になる確率は何%か。小数点以下を四捨五入して答えよ。";
  List<UiModel.Node> ns=new ArrayList<>();row(ns,old,true);row(ns,"第4問\nどれ？\n[A]一\n[B]二\n[C]三\n[D]四",true);row(ns,current,true);
  UiModel m=new UiModel(ns);check(m.qRow==2);check(m.question.number==5);check(m.question.body.contains("両端が同じ"));check(m.visibleQuestion());check(MathEngine.solve(m.question.body).answer.equals("54"));
  FinalAnchor anchor=new FinalAnchor();anchor.bind(m.question);check(!anchor.mismatch(m.question));check(anchor.mismatch(Question.parse(old)));
  ns=new ArrayList<>();row(ns,current,true);row(ns,current,false);m=new UiModel(ns);check(m.qRow==1);check(!m.visibleQuestion());
  row(ns,"第5問\n未取得の問題\n[A]",true);m=new UiModel(ns);check(m.qRow==2);check(m.question==null);
  ns=new ArrayList<>();row(ns,current,true);row(ns,"5問連続正解！\nクーポンを表示する",true);m=new UiModel(ns);check(m.question.number==5);check(QuizFlow.complete(m));
  for(int number=1;number<=5;number++){Question q=Question.parse("第"+number+"問\n本文\n[A]一\n[B]二\n[C]三\n[D]四");check(q!=null&&q.number==number);}
  System.out.println("PASS "+count+" foreground-window/last-question checks: arbitrary IME packages, real app and dialog rejection, clone switch, chronology, duplicate visibility, video Q5 calculation.");
 }
}
