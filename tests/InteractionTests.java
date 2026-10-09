import jp.kossacktouch.core.*;
public class InteractionTests {
 static int c;static void check(boolean b){c++;if(!b)throw new AssertionError("interaction "+c);}
 public static void main(String[] args){
  ElementMonitor m=new ElementMonitor();check(!m.observe("q1:no-button",1000));check(!m.observe("q1:no-button",1500));check(!m.observe("q1:A",2000));check(m.observe("q1:A",3000));
  m.reset();check(!m.observe("q1:A",4000));check(m.observe("q1:A",5000));check(!m.observe("q2:B",6000));check(m.observe("q2:B",7000));check(!m.observe("q2:B",11000));check(m.observe("q2:B",12000));
  m.reset();check(!m.observe("q5:input=empty",13000));check(m.observe("q5:input=empty",14000));check(!m.observe("q5:input=8",15000));check(m.observe("q5:input=8",16000));
  RevealSearch r=new RevealSearch();long now=1000;
  for(int pair=1;pair<=3;pair++){
   check(r.pair()==pair);check(r.missing(now)==RevealSearch.Step.WAIT);check(r.missing(now+500)==RevealSearch.Step.WAIT);check(r.missing(now+1000)==RevealSearch.Step.UP);r.moved();
   check(r.missing(now+2000)==RevealSearch.Step.WAIT);check(r.missing(now+3000)==RevealSearch.Step.DOWN);r.moved();now+=4000;
  }
  check(r.missing(now)==RevealSearch.Step.WAIT);check(r.missing(now+1000)==RevealSearch.Step.EXHAUSTED);
  r.reset();check(r.missing(20000)==RevealSearch.Step.WAIT);check(r.missing(22000)==RevealSearch.Step.WAIT);r.found();check(r.missing(23000)==RevealSearch.Step.WAIT);check(r.missing(24000)==RevealSearch.Step.UP);r.moved();r.found();check(r.missing(25000)==RevealSearch.Step.WAIT);check(r.missing(26000)==RevealSearch.Step.DOWN);
  for(float height:new float[]{400,900,1600}){
   float[] up=RevealSearch.path(100,100+height,RevealSearch.Step.UP),down=RevealSearch.path(100,100+height,RevealSearch.Step.DOWN);
   check(up[0]>up[1]);check(down[0]<down[1]);check(Math.abs((down[1]-down[0])-2*(up[0]-up[1]))<.01f);check(down[0]>100&&down[1]<100+height);
  }
  check(RevealSearch.duration(RevealSearch.Step.DOWN)<RevealSearch.duration(RevealSearch.Step.UP));
  System.out.println("PASS "+c+" interaction checks: 1-second two-observation confirmation, interrupted observations, missing-target threshold, three pairs, and 1:2 swipe distance.");
 }
}
