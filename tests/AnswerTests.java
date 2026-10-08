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
   // The quiz's original A-D is never trusted: move each answer through all positions.
   List<String> values=new ArrayList<>(q.choices.values());
   for(int offset=0;offset<4;offset++){Map<String,String> shuffled=new LinkedHashMap<>();for(int j=0;j<4;j++)shuffled.put("ABCD".substring(j,j+1),values.get((j+offset)%4));int pos=("ABCD".indexOf(expected[i])-offset+4)%4;eq("ABCD".substring(pos,pos+1),bank.match(new Question(q.number,"問題文：\n"+q.body,shuffled)));}
   Map<String,String> duplicate=new LinkedHashMap<>(q.choices);duplicate.put(expected[i].equals("A")?"B":"A",q.choices.get(expected[i]));reject(()->bank.match(new Question(q.number,q.body,duplicate)));
   Map<String,String> missing=new LinkedHashMap<>(q.choices);missing.put(expected[i],"未登録の答え");reject(()->bank.match(new Question(q.number,q.body,missing)));
  }
  Question mollk=Question.parse(video[4]);eq("A",bank.match(new Question(4,mollk.body.replace("ニュースポーツの","ニュースポーツ"),mollk.choices)));
  reject(()->bank.match(new Question(4,mollk.body.replace("6番","16番"),mollk.choices)));
  Question weight=Question.parse(video[0]);reject(()->bank.match(new Question(1,weight.body.replace("16ポンド","12ポンド"),weight.choices)));
  Map<String,String> near=new LinkedHashMap<>(weight.choices);near.put("D","約125倍");reject(()->bank.match(new Question(1,weight.body,near)));
  reject(()->bank.match(new Question(1,"未登録の問題",weight.choices)));
  reject(()->bank.match(new Question(1,weight.body+"\n"+mollk.body,weight.choices)));
  // Multiline choice text is retained, not truncated at the first line.
  Question wrapped=Question.parse(video[2].replace("ビタミンCを含むパプリカ","ビタミンCを含む\nパプリカ"));eq("C",bank.match(wrapped));
  check(Question.parse("第1問\n問題\n【A】x\n【A】y\n【B】z\n【C】u\n【D】v")==null);
  // Data integrity coverage only, not evidence of all 50 physical quiz screens.
  int checked=0;
  for(AnswerBank.Entry e:AnswerFixture.entries()){
   if(e.visualRequired)continue;String body=e.stem;
   for(List<String> g:e.keywords)if(!AnswerBank.containsGroups(body,Arrays.asList(g)))body+="\n"+g.get(0);
   for(List<String> g:e.context)if(!AnswerBank.containsGroups(body,Arrays.asList(g)))body+="\n"+g.get(0);
   Map<String,String> c=new LinkedHashMap<>();c.put("A","未登録1");c.put("B",e.answer);c.put("C","未登録2");c.put("D","未登録3");eq("B",bank.match(new Question(1,"問題："+body,c)));checked++;
  }
  eq("48",Integer.toString(checked));
  for(AnswerBank.Entry e:AnswerFixture.entries())if(e.visualRequired){Map<String,String> c=new LinkedHashMap<>();c.put("A",e.answer);c.put("B","不明1");c.put("C","不明2");c.put("D","不明3");reject(()->bank.match(new Question(1,e.stem,c)));}
  System.out.println("PASS "+n+" answer checks: 5 observed video questions, moved choices, wording variants, wrapped choices, ambiguity/unknown/changed conditions, 48 data-integrity cases and 2 visual guards.");
 }
}
