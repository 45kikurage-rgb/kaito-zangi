import jp.kossacktouch.core.*;
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
  System.out.println("PASS "+checks+" camera text checks: stable readings, gaps, new/unreadable/multiple questions, missing choices, changed conditions and local arithmetic. Android optical OCR is not exercised here.");
 }
}
