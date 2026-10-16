package jp.kossacktouch.core;
/** Wait for scroll activity to stop after the gesture releases its finger.
 * All timestamps must use the same monotonic clock. Only WAIT accepts scroll events.
 * Terminal results persist until reset/start, so a late callback cannot revive a timed-out run. */
public final class SwipeSettling {
 public enum Result { IDLE, WAIT, SETTLED, TIMEOUT }
 public static final long MIN_WAIT_MS=600;
 public static final long QUIET_MS=400;
 public static final long TIMEOUT_MS=2000;
 private Result result=Result.IDLE;
 private long released,lastScroll;
 public void start(long now){released=now;lastScroll=now;result=Result.WAIT;}
 public void onScroll(long now){if(result==Result.WAIT&&now>=released)lastScroll=Math.max(lastScroll,now);}
 public Result poll(long now){
  if(result!=Result.WAIT)return result;
  // A delayed poll cannot establish that settling was observed within the allowed window.
  if(now-released>=TIMEOUT_MS)return result=Result.TIMEOUT;
  if(now-released>=MIN_WAIT_MS&&now-lastScroll>=QUIET_MS)return result=Result.SETTLED;
  return Result.WAIT;
 }
 public void reset(){result=Result.IDLE;released=0;lastScroll=0;}
}
