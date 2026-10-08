import jp.kossacktouch.core.*;
import java.util.*;
import java.nio.file.*;
public class CoreTests {
 static int checks=0;
 static void eq(String expected,String actual){checks++;if(!expected.equals(actual))throw new AssertionError("expected "+expected+" actual "+actual);}
 static void yes(boolean x){checks++;if(!x)throw new AssertionError("condition failed");}
 static void reject(Runnable x){checks++;try{x.run();}catch(IllegalArgumentException|IllegalStateException e){return;}throw new AssertionError("unsafe input accepted");}
 static long gcdRef(long a,long b){for(long d=Math.min(a,b);d>=1;d--)if(a%d==0&&b%d==0)return d;return 1;}
 static String pct(long c,long t){return Long.toString((200*c+t)/(2*t));}
 public static void main(String[]args)throws Exception {
  String example="アマチュアボクシングの「女子エリートWB新階級」における、「ライトフライ級の最大体重(kg)」をa、「フェザー級の最大体重(kg)」をbとする。1からaまでの自然数からランダムに1つの数字を選んだ時、その数字がbと互いに素になる確率は何%か。小数点以下を四捨五入して答えよ。";
  eq("63",MathEngine.solve(example).answer); // submitted example + official result 30/48
  eq("38",MathEngine.solve(example.replace("互いに素になる","互いに素ではない")).answer);
  eq("9.2",MathEngine.solve("バレーボールの人数をa、カバディの人数をbとする。3辺がa,b,xの三角形で面積が最大になるxを求めよ。小数第2位を四捨五入して答えよ。").answer);
  eq("91001",MathEngine.solve("ハンドボールの人数を初項a、アイスホッケーの人数を公差bとする等差数列の第100項から第200項までの和を求めよ。").answer);
  eq("18",MathEngine.solve("a=7,b=6の等差数列の第1項からの和が1000を初めて超える項を求めよ。").answer);
  eq("46",MathEngine.solve("バレーボールの人数をa、水球の人数をbとする。a人とb人を一列に並べるとき両端が同じスポーツの確率は何%か。小数点以下を四捨五入。").answer);
  eq("72",MathEngine.solve("ホッケーの人数をa、ビーチサッカーの人数をbとする。1から100までの自然数から1つ選ぶときaの倍数でもbの倍数でもない確率。小数点以下を四捨五入。").answer);
  eq("11",MathEngine.solve("アマチュアボクシング女子エリートWB新階級のライトフライ級の最大体重をa、フライ級の最大体重をbとする。1からaまでの自然数から異なる2つを選び、2数の差がbの正の約数となる確率。小数点以下を四捨五入。").answer);
  // Variable numbers tested against independent enumeration, not copied formulas.
  Random random=new Random(6382);
  for(int trial=0;trial<120;trial++){
   int a=2+random.nextInt(80),b=1+random.nextInt(90),n=1+random.nextInt(300);long c=0,d=0,total=(long)a*(a-1)/2;
   for(int k=1;k<=a;k++)if(gcdRef(k,b)==1)c++;
   eq(pct(c,a),MathEngine.coprime(a,b,false,0).answer);eq(pct(a-c,a),MathEngine.coprime(a,b,true,0).answer);
   for(int x=1;x<=a;x++)for(int y=x+1;y<=a;y++)if(b%(y-x)==0)d++;
   eq(pct(d,total),MathEngine.divisorDifference(a,b,false,0).answer);eq(pct(total-d,total),MathEngine.divisorDifference(a,b,true,0).answer);
   for(String mode:new String[]{"neither","a-not-b","b-not-a","both","either"}){long cnt=0;for(int k=1;k<=n;k++){boolean aa=k%a==0,bb=k%b==0;boolean ok=mode.equals("neither")?!aa&&!bb:mode.equals("a-not-b")?aa&&!bb:mode.equals("b-not-a")?bb&&!aa:mode.equals("both")?aa&&bb:aa||bb;if(ok)cnt++;}eq(pct(cnt,n),MathEngine.multiples(a,b,n,mode,0).answer);}
   int from=1+random.nextInt(40),to=from+random.nextInt(50);long sum=0;for(int k=from;k<=to;k++)sum+=a+(long)(k-1)*b;eq(Long.toString(sum),MathEngine.arithmeticRange(a,b,from,to).answer);
   int target=1+random.nextInt(10000),i=0;long accum=0;while(accum<=target){i++;accum+=a+(long)(i-1)*b;}eq(Integer.toString(i),MathEngine.arithmeticThreshold(a,b,target).answer);
   long same=0,different=0;for(int x=0;x<a+b;x++)for(int y=0;y<a+b;y++)if(x!=y){if((x<a)==(y<a))same++;else different++;}eq(pct(same,(long)(a+b)*(a+b-1)),MathEngine.ends(a,b,true,0).answer);eq(pct(different,(long)(a+b)*(a+b-1)),MathEngine.ends(a,b,false,0).answer);
   String variable="a="+a+",b="+b+"。1から"+n+"までの自然数から1つ選ぶ。aまたはbの倍数となる確率。小数点以下を四捨五入。";eq(MathEngine.multiples(a,b,n,"either",0).answer,MathEngine.solve(variable).answer);
  }
  // All preset names including substring collisions (e.g. ライトフライ級/フライ級).
  for(String an:MathEngine.BOXING.keySet())for(String bn:MathEngine.BOXING.keySet()){
   String q=example.replace("ライトフライ級","AAA").replace("フェザー級","BBB").replace("AAA",an).replace("BBB",bn);
   eq(MathEngine.coprime(MathEngine.BOXING.get(an),MathEngine.BOXING.get(bn),false,0).answer,MathEngine.solve(q).answer);
  }
  reject(()->MathEngine.solve("a=3,b=5。未知の問題。"));reject(()->MathEngine.solve(example.replace("四捨五入","切り捨て")));reject(()->MathEngine.solve(example.replace("1つ","2つ")));reject(()->MathEngine.solve(example.replace("女子エリートWB新階級","男子プロ")));reject(()->MathEngine.triangle(0,3,1));reject(()->MathEngine.arithmeticRange(2,3,100,10));
  reject(()->MathEngine.solve(example.replace("互いに素になる","互いに素にならない")));
  reject(()->MathEngine.solve("a=3,a=4,b=5。1から100までから1つ選ぶ。aまたはbの倍数となる確率。小数点以下を四捨五入。"));
  reject(()->MathEngine.solve("a=3,b=5。1から100までから1つ選ぶ。aの倍数ではなく、bの倍数ではない確率。小数点以下を四捨五入。"));
  String fixture=new String(Files.readAllBytes(Paths.get("tests/quiz-marathon.txt")),"UTF-8");Question q=Question.parse(fixture);yes(q!=null&&q.number==1);eq("67分",q.choices.get("C"));
  AnswerBank.Entry entry=new AnswerBank.Entry("marathon","100m走の世界記録の速度でフルマラソンを","67分",Arrays.asList("67分"),true);entry.sourceLetter="C";AnswerBank bank=new AnswerBank(Arrays.asList(entry));eq("C",bank.match(q));
  Map<String,String> options=new LinkedHashMap<>(q.choices);options.put("A","67分");eq("C",bank.match(new Question(1,q.body,options)));reject(()->new AnswerBank(Arrays.asList(entry,entry)));reject(()->bank.match(new Question(1,"未登録問題",q.choices)));
  RunGuard guard=new RunGuard();yes(guard.canAnswer(q));guard.mark(q,1000);yes(!guard.canAnswer(q));reject(()->guard.mark(q,1001));yes(!guard.confirmNext(new Question(3,"wrong",options)));yes(guard.confirmNext(new Question(2,"next",options)));yes(guard.pending.isEmpty());
  eq("登録する",Registration.choose(Registration.Stage.SUMMARY,Arrays.asList("変更する","登録する","戻る"),new Random(1)));
  eq("参加登録する",Registration.choose(Registration.Stage.INTRO,Arrays.asList("参加登録する","A","B","変更する"),new Random(2)));
  yes(Registration.choose(Registration.Stage.UNKNOWN,Arrays.asList("A","登録する"),new Random(3))==null);
  for(Registration.Stage st:Registration.Stage.values())for(String label:Arrays.asList("変更する","修正する","一つ前に戻る","キャンセル","A","B","C","D","LINEで送信"))yes(!Registration.eligible(st,label));
  for(int i=0;i<100;i++)yes(Registration.PREFECTURES.contains(Registration.choose(Registration.Stage.PREFECTURE,Arrays.asList("東京都","神奈川県","大阪府","変更する"),new Random(i))));
  yes(Registration.eligible(Registration.Stage.YEAR,"2000年"));yes(!Registration.eligible(Registration.Stage.YEAR,"2000年代"));
  yes(Registration.eligible(Registration.Stage.SCHOOL,"有馬高等学校（公立）"));yes(!Registration.eligible(Registration.Stage.SCHOOL,"高校の頭文字は何かの？"));yes(!Registration.eligible(Registration.Stage.SCHOOL,"あなたの高校を選択してください"));
  yes(Registration.eligible(Registration.Stage.TERMS,"参加規約に同意して進む（タップで同意したこととなります）"));
  eq("SUMMARY",Registration.stage("これらの情報は正しいですか？").name());
  System.out.println("PASS "+checks+" assertions. Includes submitted coprime example (63), 5 families, 120 seeded parameter sets, all 81 boxing pairs, and rejection/duplicate guards.");
 }
}
