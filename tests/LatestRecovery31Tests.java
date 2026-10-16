import jp.kossacktouch.core.LatestRecovery;
import jp.kossacktouch.core.RevealSearch;
import static jp.kossacktouch.core.LatestRecovery.Action.*;
public class LatestRecovery31Tests {
 static int count;static void check(boolean ok){count++;if(!ok)throw new AssertionError("latest recovery31 "+count);}
 public static void main(String[] args){
  LatestRecovery r=new LatestRecovery();
  check(r.observe(false,0)==NONE);check(r.observe(true,100)==REQUEST);
  r.requested(100);check(r.observe(true,100)==WAIT);check(r.observe(true,6099)==WAIT);
  r.requested(6000);check(r.observe(true,6100)==EXHAUSTED);check(r.observe(true,7000)==EXHAUSTED);
  check(r.observe(false,7000)==NONE);check(r.observe(true,7500)==REQUEST);
  r.requested(7500);r.reset();check(r.observe(true,8000)==REQUEST);
  // Returning to latest and fallback swipes consume the same finite movement budget.
  RevealSearch search=new RevealSearch();long now=0;
  for(int i=0;i<10;i++){
   check(search.missing(now)==RevealSearch.Step.WAIT);
   check(search.missing(now+1000)==RevealSearch.Step.UP);
   if(i%2==0){check(r.observe(true,now)==REQUEST);r.requested(now);check(r.observe(true,now+600)==WAIT);check(r.observe(false,now+1000)==NONE);}
   search.moved();search.found();now+=1500;
  }
  check(search.missing(now)==RevealSearch.Step.EXHAUSTED);
  System.out.println("PASS "+count+" code31 latest recovery checks: no repeat request, completion observation, timeout, reset and shared finite search budget.");
 }
}
