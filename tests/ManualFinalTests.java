import jp.kossacktouch.core.*;
public class ManualFinalTests {
 static int count;static void check(boolean b){count++;if(!b)throw new AssertionError("manual final "+count);}
 static ManualFinal.Step tick(ManualFinal f,boolean stable,boolean shown,boolean opener,int inputs,boolean clickable,long now){return f.observe("5:current",stable,shown,opener,inputs,clickable,now);}
 public static void main(String[] args){
  ManualFinal f=new ManualFinal();check(!f.started());check(tick(f,false,false,true,0,false,0)==ManualFinal.Step.WAIT);check(!f.started());check(tick(f,true,false,true,0,false,1000)==ManualFinal.Step.OPEN_KEYBOARD);check(f.started());
  check(tick(f,true,false,true,0,false,2000)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,0,false,3000)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,0,false,4000)==ManualFinal.Step.DISPLAY);
  // Once handed off, queued observations cannot resume actions when the user closes the IME.
  check(tick(f,true,false,true,1,true,5000)==ManualFinal.Step.DISPLAY);
  f.reset();check(!f.started());check(tick(f,true,false,false,1,true,6000)==ManualFinal.Step.FOCUS_INPUT);check(tick(f,true,false,false,1,true,7000)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,1,true,8000)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,1,true,9000)==ManualFinal.Step.DISPLAY);
  f=new ManualFinal();check(tick(f,true,false,true,0,false,0)==ManualFinal.Step.OPEN_KEYBOARD);check(tick(f,true,false,true,0,false,9000)==ManualFinal.Step.WAIT);check(tick(f,true,false,true,0,false,10000)==ManualFinal.Step.TIMEOUT);check(tick(f,true,false,true,0,false,11000)==ManualFinal.Step.TIMEOUT);
  f=new ManualFinal();check(tick(f,true,true,false,0,false,0)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,0,false,500)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,0,false,1000)==ManualFinal.Step.DISPLAY);
  f=new ManualFinal();check(tick(f,true,true,false,0,false,0)==ManualFinal.Step.WAIT);check(tick(f,true,false,true,0,false,1000)==ManualFinal.Step.OPEN_KEYBOARD);check(tick(f,true,true,false,0,false,2000)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,0,false,3000)==ManualFinal.Step.DISPLAY);
  f=new ManualFinal();check(tick(f,true,true,false,0,false,0)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,0,false,4000)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,0,false,5000)==ManualFinal.Step.DISPLAY);
  f=new ManualFinal();check(tick(f,false,true,false,0,false,0)==ManualFinal.Step.WAIT);check(tick(f,false,true,false,0,false,1000)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,0,false,2000)==ManualFinal.Step.WAIT);check(tick(f,true,true,false,0,false,3000)==ManualFinal.Step.DISPLAY);
  f=new ManualFinal();check(f.observe("5:old",true,true,false,0,false,0)==ManualFinal.Step.WAIT);check(f.observe("5:new",true,true,false,0,false,1000)==ManualFinal.Step.WAIT);check(f.observe("5:new",true,true,false,0,false,2000)==ManualFinal.Step.DISPLAY);
  for(int inputs:new int[]{0,2,3}){f=new ManualFinal();check(tick(f,true,false,false,inputs,true,0)==ManualFinal.Step.MANUAL_OPEN);check(tick(f,true,false,true,1,true,1000)==ManualFinal.Step.MANUAL_OPEN);}
  f=new ManualFinal();check(tick(f,true,false,false,1,false,0)==ManualFinal.Step.MANUAL_OPEN);f.reset();check(tick(f,true,false,true,0,false,1000)==ManualFinal.Step.OPEN_KEYBOARD);
  // Already visible IME needs no editable/submit element, across all candidate counts.
  for(int inputs=0;inputs<=3;inputs++){f=new ManualFinal();check(tick(f,true,true,true,inputs,true,0)==ManualFinal.Step.WAIT);check(tick(f,true,true,true,inputs,true,1000)==ManualFinal.Step.DISPLAY);}
  String observed="アマチュアボクシングの「女子エリートWB新階級」における、「ライトフライ級の最大体重(kg)」をa、「ミドル級の最大体重(kg)」をbとする。1からaまでの自然数から異なる2つの数字をランダムで選んだ時、2数の差がbの正の約数となる確率は何%か。小数点以下を四捨五入して答えよ。";
  long pairs=0,matching=0;for(int a=1;a<=48;a++)for(int b=a+1;b<=48;b++){pairs++;if(75%(b-a)==0)matching++;}
  check(pairs==1128&&matching==191);check(MathEngine.solve(observed).answer.equals("17"));
  System.out.println("PASS "+count+" manual-Q5 checks: visible IME without editor/send elements, two observations, one keyboard request, terminal handoff, timeout/manual fallback, reset and supplied boxing question.");
 }
}
