import jp.kossacktouch.core.*;
public class InteractionTests {
 static int c;static void check(boolean b){c++;if(!b)throw new AssertionError("interaction "+c);}
 public static void main(String[] args){
  ElementMonitor m=new ElementMonitor();check(!m.observe("q1:no-button",1000));check(!m.observe("q1:no-button",1250));check(!m.observe("q1:A",2000));check(m.observe("q1:A",3000));
  m.reset();check(!m.observe("q1:A",4000));check(m.observe("q1:A",5000));check(!m.observe("q2:B",6000));check(m.observe("q2:B",7000));check(!m.observe("q2:B",11000));check(m.observe("q2:B",12000));
  m.reset();check(!m.observe("q5:input=empty",13000));check(m.observe("q5:input=empty",14000));check(!m.observe("q5:input=8",15000));check(m.observe("q5:input=8",16000));
  RevealSearch r=new RevealSearch();long now=1000;
  for(int pair=1;pair<=5;pair++){
   check(r.pair()==pair);check(r.missing(now)==RevealSearch.Step.WAIT);check(r.missing(now+500)==RevealSearch.Step.WAIT);check(r.missing(now+1000)==RevealSearch.Step.DOWN);r.moved();
   check(r.missing(now+2000)==RevealSearch.Step.WAIT);check(r.missing(now+3000)==RevealSearch.Step.UP);r.moved();now+=4000;
  }
  check(r.missing(now)==RevealSearch.Step.EXHAUSTED);check(r.missing(now+1000)==RevealSearch.Step.EXHAUSTED);check(r.missing(27000)==RevealSearch.Step.EXHAUSTED);
  r.reset();check(r.missing(20000)==RevealSearch.Step.WAIT);check(r.missing(22000)==RevealSearch.Step.WAIT);r.found();check(r.missing(23000)==RevealSearch.Step.WAIT);check(r.missing(24000)==RevealSearch.Step.DOWN);r.moved();r.found();check(r.missing(25000)==RevealSearch.Step.WAIT);check(r.missing(26000)==RevealSearch.Step.UP);
  for(float height:new float[]{400,900,1600}){
   float[] up=RevealSearch.path(100,100+height,48,RevealSearch.Step.UP),down=RevealSearch.path(100,100+height,48,RevealSearch.Step.DOWN);
   check(up[0]>up[1]);check(Math.abs((up[0]-up[1])-48f)<.01f);
   check(down[0]==101f);check(down[1]==99f+height);check(down[0]<down[1]);
  }
  // 48dp converted by Android at different densities must produce the exact pixel distance.
  for(float density:new float[]{1,1.5f,2,3,4}){
   float pixels=Math.round(48*density);
   float[] up=RevealSearch.path(0,2400,pixels,RevealSearch.Step.UP);
   check(Math.abs(up[0]-up[1]-pixels)<.01f);check(up[1]>=1&&up[0]<=2399);
  }
  // Overlay/IME lane may be shorter than one float height: both points stay inside it.
  float[] narrow=RevealSearch.path(100,200,144,RevealSearch.Step.UP);
  check(narrow[1]==101f);check(narrow[0]>narrow[1]);check(narrow[0]-narrow[1]<144);
  float[] narrowDown=RevealSearch.path(100,200,144,RevealSearch.Step.DOWN);
  check(narrowDown[0]==101f&&narrowDown[1]==199f);
  float[] legacyDown=RevealSearch.path(100,1000,RevealSearch.Step.DOWN);
  check(legacyDown[0]==101f&&legacyDown[1]==999f);
  float[] defaultUp=RevealSearch.path(100,1000,RevealSearch.Step.UP);
  check(Math.abs(defaultUp[0]-defaultUp[1]-48f)<.01f);

  check(RevealSearch.duration(RevealSearch.Step.DOWN)==150);check(RevealSearch.duration(RevealSearch.Step.UP)==350);
  // A rejected dispatch consumes no step; reset starts a new DOWN-first search after fresh observations.
  r.reset();check(r.missing(50000)==RevealSearch.Step.WAIT);check(r.missing(51000)==RevealSearch.Step.DOWN);
  check(r.missing(51500)==RevealSearch.Step.DOWN);r.moved();r.reset();
  check(r.missing(52000)==RevealSearch.Step.WAIT);check(r.missing(53000)==RevealSearch.Step.DOWN);
  r.moved();check(r.missing(53500)==RevealSearch.Step.WAIT);check(r.missing(54500)==RevealSearch.Step.UP);
  System.out.println("PASS "+c+" interaction checks: half-second confirmation, DOWN-first search, ten movements/26 seconds, density-correct float-height UP swipes, safe-lane clamp, unchanged DOWN distance and speed.");
 }
}
