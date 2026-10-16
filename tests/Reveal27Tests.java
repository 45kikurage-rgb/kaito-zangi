import jp.kossacktouch.core.*;
public class Reveal27Tests {
 static int count;static void check(boolean ok){count++;if(!ok)throw new AssertionError("reveal27 "+count);}
 public static void main(String[] args){
  check(RevealSearch.MAX_SWIPES==10);check(RevealSearch.END_HOLD_MS==300);
  for(float density:new float[]{1,1.5f,2,3,4}){
   float distance=48*density,minimum=50*density;
   long duration=RevealSearch.duration(RevealSearch.Step.UP,distance,minimum);
   check(duration==1920);check(distance*1000/duration<=minimum*.5f);
   check(RevealSearch.duration(RevealSearch.Step.DOWN,distance,minimum)==150);
  }
  for(float distance:new float[]{1,7.5f,24,48,144})for(float velocity:new float[]{20,50,150,800}){
   long duration=RevealSearch.duration(RevealSearch.Step.UP,distance,velocity);
   check(duration>=350);check(distance*1000d/duration<=velocity*.5d);
  }
  for(float invalid:new float[]{0,-1,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY}){
   check(RevealSearch.duration(RevealSearch.Step.UP,48,invalid)==2000);
   check(RevealSearch.duration(RevealSearch.Step.UP,invalid,50)==2000);
   check(RevealSearch.duration(RevealSearch.Step.DOWN,invalid,invalid)==150);
  }
  RevealSearch search=new RevealSearch();int moves=0;long now=0;
  while(now<=26000){RevealSearch.Step step=search.missing(now);if(step==RevealSearch.Step.EXHAUSTED)break;
   if(step==RevealSearch.Step.DOWN||step==RevealSearch.Step.UP){check(search.movement()==moves+1);check(step==(moves%2==0?RevealSearch.Step.DOWN:RevealSearch.Step.UP));search.moved();moves++;}
   now+=500;
  }
  check(moves==10);check(now<26000);check(search.missing(now)==RevealSearch.Step.EXHAUSTED);
  boolean bounded=false;try{search.moved();}catch(IllegalStateException expected){bounded=true;}check(bounded);
  // Slow valid UP strokes can hit the time budget before the count budget; no extra move is authorized.
  search=new RevealSearch();check(search.missing(0,true)==RevealSearch.Step.WAIT);check(search.missing(1000,true)==RevealSearch.Step.DOWN);search.moved();
  check(search.missing(26000,true)==RevealSearch.Step.EXHAUSTED);
  search.reset();check(search.missing(30000)==RevealSearch.Step.WAIT);check(search.missing(31000)==RevealSearch.Step.DOWN);
  // Start and each completion require new observations; registration follows the same ten-step sequence.
  search=new RevealSearch();ElementMonitor monitor=new ElementMonitor();now=0;
  for(int i=0;i<10;i++){
   monitor.reset();check(!monitor.observe("registration:"+i,now));check(monitor.observe("registration:"+i,now+500));
   check(search.missing(now,true)==RevealSearch.Step.WAIT);
   check(search.missing(now+500,true)==RevealSearch.Step.WAIT);
   check(search.missing(now+1000,true)==(i%2==0?RevealSearch.Step.DOWN:RevealSearch.Step.UP));
   search.moved();now+=2000;
  }
  check(search.missing(now,true)==RevealSearch.Step.EXHAUSTED);
  // Revealed choices restart freshness checks without losing the total search budget.
  search=new RevealSearch();check(search.missing(0,true)==RevealSearch.Step.WAIT);check(search.missing(1000,true)==RevealSearch.Step.DOWN);search.moved();search.found();
  check(search.missing(2000,true)==RevealSearch.Step.WAIT);check(search.missing(3000,true)==RevealSearch.Step.UP);
  for(float screen:new float[]{640,1280,1920,2400,3200}){
   float top=screen*.1f,bottom=screen*.9f,half=screen*.5f;
   float[] down=RevealSearch.path(top,bottom,48,half,RevealSearch.Step.DOWN);
   check(Math.abs(down[1]-down[0]-half)<.001f);check(down[0]>top&&down[1]<bottom);
   float[] up=RevealSearch.path(top,bottom,48,half,RevealSearch.Step.UP);check(up[0]-up[1]==48);
   down=RevealSearch.path(top,top+100,48,half,RevealSearch.Step.DOWN);check(down[1]-down[0]==98);check(down[1]<top+100);
  }
  for(Registration.Stage stage:Registration.Stage.values())for(boolean prompted:new boolean[]{false,true})for(boolean pending:new boolean[]{false,true}){
   check(!Registration.canSearch(stage,prompted,pending,true));
   check(Registration.canSearch(stage,prompted,pending,false)==(pending||stage!=Registration.Stage.UNKNOWN&&prompted));
  }
  check(Registration.selectionPending("SCHOOL:old",Registration.Stage.SCHOOL_INITIAL,"SCHOOL_INITIAL:new"));
  check(Registration.selectionPending("SCHOOL:old",Registration.Stage.PREFECTURE,"PREFECTURE:new"));
  check(Registration.selectionPending("SCHOOL_INITIAL:old",Registration.Stage.PREFECTURE,"PREFECTURE:new"));
  check(!Registration.selectionPending("PREFECTURE:old",Registration.Stage.SCHOOL_INITIAL,"SCHOOL_INITIAL:new"));
  check(!Registration.selectionPending("SCHOOL_INITIAL:old",Registration.Stage.SCHOOL,"SCHOOL:new"));
  for(Registration.Stage stage:new Registration.Stage[]{Registration.Stage.YEAR,Registration.Stage.SEX,Registration.Stage.TERMS,Registration.Stage.SUMMARY})check(!Registration.selectionPending("SCHOOL:old",stage,stage.name()+":new"));
  check(RevealSearch.brakeAtEnd(RevealSearch.Step.UP));check(!RevealSearch.brakeAtEnd(RevealSearch.Step.DOWN));
  // A real terminal MOVE is bounded inside each swipe, even after integer pixel quantization.
  for(float density:new float[]{1,1.5f,2,3,4})for(float screen:new float[]{320,640,1280,2400}){
   float distance=RevealSearch.upwardDistance(density,screen);
   check(distance<=48*density&&distance<=screen*.1f);
   float tail=RevealSearch.brakeDistance(distance);
   check(tail>=1&&tail<=4&&tail<distance);
   for(float sign:new float[]{-1,1}){
    float end=400+sign*distance,split=end-sign*tail;
    check(Math.abs(split-400)+Math.abs(end-split)==distance);
    check(Math.abs(Math.round(end)-Math.round(split))>=1);
   }
   long duration=RevealSearch.brakeDuration(tail,50*density);
   check(duration>=600&&tail*1000d/duration<=50*density*.25);
  }
  for(float v:new float[]{10,20,50,150,800})check(4*1000d/RevealSearch.brakeDuration(4,v)<=v*.25);
  boolean rejects=false;try{RevealSearch.brakeDistance(1);}catch(IllegalArgumentException expected){rejects=true;}check(rejects);
  System.out.println("PASS "+count+" code27 reveal checks: half-minimum-fling velocity, invalid-input fallback, half-screen DOWN, 300ms endpoint hold contract, ten total movements and time/step bounds.");
 }
}
