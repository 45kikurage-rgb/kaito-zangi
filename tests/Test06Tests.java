import jp.kossacktouch.core.*;import java.util.*;
public class Test06Tests {
 static int c=0;static void check(boolean b){c++;if(!b)throw new AssertionError("test06 "+c);}
 static UiModel.Node node(int parent,String id,String text,boolean visible,boolean click){return new UiModel.Node(parent,"jp.naver.line.androif:id/"+id,text,visible,true,click);}
 static List<UiModel.Node> base(String title){return new ArrayList<>(Arrays.asList(node(-1,"header_title",title,true,false),node(-1,"chat_ui_main_content_area","",true,false),node(1,"chat_ui_list","",true,false)));}
 static UiModel numeric(String guide,String body){List<UiModel.Node> ns=base("超良問ドリル");ns.add(node(2,"chat_ui_row_flex_message_frame",guide,true,false));ns.add(node(2,"chat_ui_row_flex_message_frame","第5問\n"+body,true,false));return new UiModel(ns);}
 public static void main(String[] args){
  List<UiModel.Node> ns=base("超良問ドリル");
  for(String p:Arrays.asList("jp.naver.line.android","jp.naver.line.androie","jp.naver.line.androif","jp.naver.line.clone001"))check(LineIdentity.target(p,ns));
  for(String p:Arrays.asList("com.android.chrome","com.fake.line","jp.naver.line.","jp.naver.line.androif/", "evil.jp.naver.line.android"))check(!LineIdentity.target(p,ns));
  check(!LineIdentity.target("jp.naver.line.android",base("別のトーク")));
  ns.add(node(-1,"header_title","別のトーク",true,false));check(!LineIdentity.target("jp.naver.line.android",ns));
  ns=base("超良問ドリル");ns.remove(1);check(!LineIdentity.target("jp.naver.line.android",ns));
  check(Registration.personal(Registration.Stage.YEAR));check(Registration.personal(Registration.Stage.SEX));check(Registration.personal(Registration.Stage.TERMS));check(!Registration.personal(Registration.Stage.PREFECTURE));check(!Registration.personal(Registration.Stage.SCHOOL));
  check(Registration.eligible(Registration.Stage.SCHOOL,"青山学院横浜英和高等学校（私\n立）"));check(!Registration.eligible(Registration.Stage.SCHOOL,"高校を変更する"));
  for(String s:Arrays.asList("ちゃんと聞きます！","わかりました！","初めてだけど頑張る！","が、がんばる..","全国TOP10入りは厳しいかも..","地域別なら頑張ればいけるかも？！","昨年同様のバージョンアップだすけ！","ランキング上位目指すぞ！"))check(Registration.eligible(Registration.Stage.INTRO,s));
  for(String s:Arrays.asList("友だちを追加","変更する","規約に同意","広告を見る","送信","A","B"))check(!Registration.eligible(Registration.Stage.INTRO,s));
  ns=base("超良問ドリル");int old=ns.size();ns.add(node(2,"chat_ui_row_flex_message_frame","",true,false));ns.add(node(old,"","東京都",true,true));
  int fresh=ns.size();ns.add(node(2,"chat_ui_row_flex_message_frame","",true,false));ns.add(node(fresh,"","神奈川県",true,true));ns.add(node(fresh,"","大阪府",false,true));ns.add(node(fresh,"","キャンセル",true,true));ns.add(node(fresh,"","北海道",true,false));
  UiModel m=new UiModel(ns);check(m.registrationControls(Registration.Stage.PREFECTURE).keySet().equals(new LinkedHashSet<>(Arrays.asList("神奈川県"))));
  for(int i=0;i<100;i++)check(Registration.choose(Registration.Stage.PREFECTURE,new ArrayList<>(m.registrationControls(Registration.Stage.PREFECTURE).keySet()),new Random(i)).equals("神奈川県"));
  ns.add(node(fresh,"","神奈川県",true,true));check(new UiModel(ns).registrationControls(Registration.Stage.PREFECTURE).isEmpty());
  String guide="ついに最終問題！\n★回答方法★\n答えは数字をそのまま入力してください。小数点を含む答えが出る場合があります。スペースなど答えに関係のない文字が含まれているとチャレンジ失敗となります。";
  String q="アマチュアボクシングの「女子エリートWB新階級」における、「フェザー級の最大体重(kg)」をa、「ライト級の最大体重(kg)」をbとする。1からaまでの自然数から異なる2つの数字をランダムで選んだ時、2数の差がbの正の約数ではない確率は何％か。小数点以下を四捨五入して答えよ。";
  m=numeric(guide,q);check(m.question!=null&&m.question.number==5);check(FinalSubmission.messageFormat(m));check(MathEngine.solve(m.question.body).answer.equals("67"));
  String reportedGuide="★回答方法★\n答えは整数または小数で入力してください（小数点「.」の使用可）。スペースなど、答えに関係のない文字が含まれていると間違いと判断されチャレンジ失敗となります。";
  String reported="「サッカー」「ビーチサッカー」の1チームのスターティングメンバーの数をそれぞれa,bとする。1から100までの数字をランダムに1つ選んだ時、その数字がaの倍数であり、なおかつbの倍数でない確率が何%かを求めよ。";
  UiModel reportedUi=numeric(reportedGuide,reported);check(reportedUi.question!=null&&reportedUi.question.number==5);check(MathEngine.solve(reportedUi.question.body).answer.equals("8"));check(FinalSubmission.messageFormat(reportedUi));check(FinalSubmission.route(false,0,false,FinalSubmission.messageFormat(reportedUi),1,true)==FinalSubmission.Route.MESSAGE);
  check(!FinalSubmission.messageFormat(numeric(reportedGuide.replace("整数または小数", "文章"),reported)));
  check(!FinalSubmission.messageFormat(numeric("",q)));check(!FinalSubmission.messageFormat(numeric("★回答方法★\n専用回答ボタンを押す",q)));
  check(FinalSubmission.route(true,1,true,true,1,true)==FinalSubmission.Route.FORM);
  check(FinalSubmission.route(true,0,false,true,1,true)==FinalSubmission.Route.WAIT);
  check(FinalSubmission.route(true,2,true,true,1,true)==FinalSubmission.Route.WAIT);
  check(FinalSubmission.route(false,0,false,true,1,false)==FinalSubmission.Route.MESSAGE);
  check(FinalSubmission.route(false,0,false,true,0,false)==FinalSubmission.Route.WAIT);
  check(FinalSubmission.route(false,0,false,true,2,true)==FinalSubmission.Route.STOP);
  check(FinalSubmission.route(false,0,false,false,1,true)==FinalSubmission.Route.STOP);
  for(String a:Arrays.asList("67","8.5","-1","0"))check(FinalSubmission.numeric(a));
  for(String a:Arrays.asList("67%","67 ","67\n","1e3","答え67",""))check(!FinalSubmission.numeric(a));
  check(FinalSubmission.draftSafe("","67",false));check(!FinalSubmission.draftSafe("67","67",false));check(FinalSubmission.draftSafe("67","67",true));check(!FinalSubmission.draftSafe("こんにちは","67",true));
  RunGuard g=new RunGuard();g.lastConfirmed=4;g.mark(m.question,5000);check(!g.canAnswer(m.question));RunGuard resumed=new RunGuard();resumed.restore(m.question.key,6000);check(!resumed.canAnswer(m.question));
  List<UiModel.Node> done=base("超良問ドリル");done.add(node(2,"chat_ui_row_flex_message_frame","第5問\n"+q,true,false));done.add(node(2,"chat_ui_row_flex_message_frame","正解！\n5問連続正解！",true,false));check(QuizFlow.complete(new UiModel(done)));check(!FinalSubmission.messageFormat(new UiModel(done)));
  System.out.println("PASS "+c+" test06 checks: cloned LINE guards, visible-only registration, personal confirmation, numeric message/form routing, observed 67, completion and no duplicate submission.");
 }
}
