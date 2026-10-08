import jp.kossacktouch.core.*;import java.util.*;
public class AnswerTests {
 static int n=0;static void check(boolean b){n++;if(!b)throw new AssertionError("answer check "+n);}
 static void eq(String a,String b){check(a.equals(b));}
 static void reject(Runnable r){n++;try{r.run();}catch(IllegalArgumentException e){return;}throw new AssertionError("unsafe answer accepted "+n);}
 public static void main(String[] args){
  AnswerBank bank=AnswerFixture.bank();eq("50",Integer.toString(bank.size()));
  String[] video=VideoFixture.questions(),expected=VideoFixture.expected();
  for(int i=0;i<video.length;i++){
   Question q=Question.parse(video[i]);check(q!=null);eq(expected[i],bank.match(q));
   // Choice wording/order is irrelevant: registered keywords map directly to the official A-D.
   List<String> values=new ArrayList<>(q.choices.values());
   for(int offset=0;offset<4;offset++){Map<String,String> changed=new LinkedHashMap<>();for(int j=0;j<4;j++)changed.put("ABCD".substring(j,j+1),values.get((j+offset)%4));eq(expected[i],bank.match(new Question(q.number,"問題文：\n"+q.body,changed)));}
   Map<String,String> missing=new LinkedHashMap<>(q.choices);missing.remove(expected[i]);reject(()->bank.match(new Question(q.number,q.body,missing)));
  }
  Question mollk=Question.parse(video[4]);eq("A",bank.match(new Question(4,mollk.body.replace("ニュースポーツの","ニュースポーツ"),mollk.choices)));
  reject(()->bank.match(new Question(4,mollk.body.replace("6番","16番"),mollk.choices)));
  Question weight=Question.parse(video[0]);reject(()->bank.match(new Question(1,weight.body.replace("16ポンド","12ポンド"),weight.choices)));
  reject(()->bank.match(new Question(1,"未登録の問題",weight.choices)));
  reject(()->bank.match(new Question(1,weight.body+"\n"+mollk.body,weight.choices)));
  Question wrapped=Question.parse(video[2].replace("ビタミンCを含むパプリカ","全く別の文章\nでもボタンはC"));eq("C",bank.match(wrapped));
  check(Question.parse("第1問\n問題\n【A】x\n【A】y\n【B】z\n【C】u\n【D】v")==null);
  int checked=0;
  for(AnswerBank.Entry e:AnswerFixture.entries()){
   String body=e.stem;
   for(List<String> g:e.keywords)if(!AnswerBank.containsGroups(body,Arrays.asList(g)))body+="\n"+g.get(0);
   for(List<String> g:e.context)if(!AnswerBank.containsGroups(body,Arrays.asList(g)))body+="\n"+g.get(0);
   Map<String,String> c=new LinkedHashMap<>();c.put("A","任意1");c.put("B","任意2");c.put("C","任意3");c.put("D","任意4");eq(e.sourceLetter,bank.match(new Question(1,"問題："+body,c)));checked++;
  }
  eq("50",Integer.toString(checked));
  Question walk=Question.parse("第1問 (スポーツ全般)\n陸上競技の競歩で、この印は何を意味する？\n【A】スピードが速すぎるため一時停止しなければならない\n【B】他選手にわざと接触したため注意喚起をしている\n【C】膝が曲がっているため即失格となる\n【D】両足が地面から離れているため注意喚起をしている");eq("D",bank.match(walk));
  System.out.println("PASS "+n+" keyword-to-letter checks: question text only, 6 observed screens, wording variants, missing button, ambiguous/unknown/changed conditions, and all 50 registered mappings.");
 }
}
