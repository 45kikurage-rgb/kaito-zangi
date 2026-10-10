import jp.kossacktouch.core.*;
import java.util.*;
public class CameraTests {
 static int checks;static void check(boolean b){checks++;if(!b)throw new AssertionError("camera "+checks);}
 static void empty(CameraAnswer.Reading r){check(r.answer.isEmpty());}
 public static void main(String[] args){
  CameraAnswer camera=new CameraAnswer(AnswerFixture.bank());String[] qs=VideoFixture.questions(),answers=VideoFixture.expected();long time=10000;
  for(int i=0;i<qs.length;i++){camera.reset();empty(camera.observe(qs[i],time));check(camera.observe(qs[i],time+1000).answer.equals(answers[i]));time+=3000;}
  String q=qs[0];camera.reset();empty(camera.observe(q,0));empty(camera.observe(q,500));check(camera.observe(q,1000).answer.equals(answers[0]));
  empty(camera.observe("",2000));empty(camera.observe(q,3000));check(camera.observe(q,4000).answer.equals(answers[0]));
  empty(camera.observe(qs[1],5000));check(camera.observe(qs[1],6000).answer.equals(answers[1]));
  empty(camera.observe(q+"\n"+qs[1],7000));empty(camera.observe(q,8000));check(camera.observe(q,9000).answer.equals(answers[0]));
  empty(camera.observe(q,12000));check(camera.observe(q,13000).answer.equals(answers[0]));
  String missing=q.replace("【D】","D ");camera.reset();empty(camera.observe(missing,0));empty(camera.observe(missing,1000));
  String changed=q.replace("16ポンド","12ポンド");camera.reset();empty(camera.observe(changed,0));empty(camera.observe(changed,1000));
  String fifth="第5問\n「ホッケー」「カーリング」の1チームのスターティングメンバーの数をそれぞれa,bとする。初項a、公差bの等差数列の和が初めて10000を超えるのは、第何項まで足したときか。";
  camera.reset();empty(camera.observe(fifth,0));check(camera.observe(fifth,1000).answer.equals("69"));
  // Numeric OCR change clears old answer and must be confirmed afresh.
  String unknown=fifth.replace("10000","1000x");empty(camera.observe(unknown,2000));empty(camera.observe(unknown,3000));
  camera.reset();empty(camera.observe(fifth.replace("第5問","第５ 問"),0));check(camera.observe(fifth,1000).answer.equals("69"));
  camera.reset();empty(camera.observe("第5問\n未知の式",0));empty(camera.observe("第5問\n未知の式",1000));
  camera.reset();empty(camera.observe("第5問",0));empty(camera.observe("第5問",1000));
  camera.reset();String clipped=fifth.substring(0,fifth.indexOf("、第何項"));empty(camera.observe(clipped,0));empty(camera.observe(clipped,1000));
  camera.reset();empty(camera.observe(fifth+"\n第6問\n別の問題",0));empty(camera.observe(fifth+"\n第6問\n別の問題",1000));
  // Exercise the complete bundled bank through the camera text route, not only LINE matching.
  int entries=0;
  for(AnswerBank.Entry e:AnswerFixture.entries()){
   String body=e.stem;
   for(List<String> group:e.keywords)if(!AnswerBank.containsGroups(body,Arrays.asList(group)))body+="\n"+group.get(0);
   for(List<String> group:e.context)if(!AnswerBank.containsGroups(body,Arrays.asList(group)))body+="\n"+group.get(0);
   String full="第"+(entries%4+1)+"問\n"+body+"\n【A】任意1\n【B】任意2\n【C】任意3\n【D】任意4";
   camera.reset();empty(camera.observe(full,0));check(camera.observe(full,1000).answer.equals(e.sourceLetter));entries++;
  }
  check(entries==50);
  // An unknown question after a valid answer must remain blank on repeated observations.
  camera.reset();empty(camera.observe(q,0));check(camera.observe(q,1000).answer.equals(answers[0]));
  String unsupported="第2問\n未登録の問題ですか？\n【A】1\n【B】2\n【C】3\n【D】4";
  empty(camera.observe(unsupported,2000));empty(camera.observe(unsupported,3000));
  camera.reset();empty(camera.observe(q,0));check(camera.observe(q,2500).answer.equals(answers[0]));
  empty(camera.observe(q,5001));check(camera.observe(q,6001).answer.equals(answers[0]));
  // Lifecycle reset invalidates confirmation even if the same problem returns immediately.
  camera.reset();empty(camera.observe(q,0));check(camera.observe(q,1000).answer.equals(answers[0]));
  camera.reset();empty(camera.observe(q,2000));check(camera.observe(q,3000).answer.equals(answers[0]));
  System.out.println("PASS "+checks+" camera text checks: stable readings, gaps, new/unreadable/multiple questions, missing choices, changed conditions and local arithmetic. Android optical OCR is not exercised here.");
 }
}
