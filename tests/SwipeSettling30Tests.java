import jp.kossacktouch.core.SwipeSettling;
import static jp.kossacktouch.core.SwipeSettling.Result.*;
public class SwipeSettling30Tests {
 static int count;static void check(boolean ok){count++;if(!ok)throw new AssertionError("swipe settling30 "+count);}
 public static void main(String[] args){
  check(SwipeSettling.MIN_WAIT_MS==600);check(SwipeSettling.QUIET_MS==400);check(SwipeSettling.TIMEOUT_MS==2000);
  SwipeSettling s=new SwipeSettling();check(s.poll(0)==IDLE);s.onScroll(100);check(s.poll(1000)==IDLE);
  s.start(1000);check(s.poll(1000)==WAIT);check(s.poll(1399)==WAIT);check(s.poll(1400)==WAIT);check(s.poll(1599)==WAIT);check(s.poll(1600)==SETTLED);
  // Once consumed, the result is stable even if a late event arrives.
  s.onScroll(1700);check(s.poll(1700)==SETTLED);check(s.poll(9000)==SETTLED);
  s.start(10000);s.onScroll(10300);check(s.poll(10600)==WAIT);check(s.poll(10699)==WAIT);check(s.poll(10700)==SETTLED);
  // Each scroll restarts the quiet window, not the total deadline.
  s.start(20000);for(int elapsed=200;elapsed<=1800;elapsed+=200){s.onScroll(20000+elapsed);check(s.poll(20000+elapsed)==WAIT);}
  check(s.poll(21999)==WAIT);check(s.poll(22000)==TIMEOUT);s.onScroll(22010);check(s.poll(23000)==TIMEOUT);
  // Quiet at the last millisecond is permitted; reaching the deadline is always bounded.
  s.start(30000);s.onScroll(31599);check(s.poll(31998)==WAIT);check(s.poll(31999)==SETTLED);
  s.start(40000);s.onScroll(41600);check(s.poll(42000)==TIMEOUT);
  s.start(50000);check(s.poll(52500)==TIMEOUT);check(s.poll(50600)==TIMEOUT);
  // Manual intervention/reset cancels the generation and ignores its remaining scroll events.
  s.start(60000);s.onScroll(60100);s.reset();s.onScroll(60200);check(s.poll(61000)==IDLE);
  s.start(70000);s.onScroll(69999);check(s.poll(70599)==WAIT);check(s.poll(70600)==SETTLED);
  // A new start discards the previous run's quiet-window and terminal state.
  s.start(80000);s.onScroll(80500);s.start(80600);check(s.poll(81199)==WAIT);check(s.poll(81200)==SETTLED);
  s.start(90000);s.onScroll(90500);s.onScroll(90400);check(s.poll(90899)==WAIT);check(s.poll(90900)==SETTLED);
  s.reset();check(s.poll(0)==IDLE);s.start(0);check(s.poll(599)==WAIT);check(s.poll(600)==SETTLED);
  System.out.println("PASS "+count+" code30 settling checks: release delay, quiet-window reset, continuous-scroll timeout, delayed polling, stable terminal results and new-run reset.");
 }
}
