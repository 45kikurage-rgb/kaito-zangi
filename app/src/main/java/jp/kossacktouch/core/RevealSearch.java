package jp.kossacktouch.core;
/** Bound every search to ten movements and a 26-second search window. */
public final class RevealSearch {
 public enum Step { WAIT, UP, DOWN, EXHAUSTED }
 public static final long TIMEOUT_MS=26000;
 public static final int MAX_SWIPES=10;
 public static final long END_HOLD_MS=300;
 private int misses,swipes;private long lastMissing=-1,started=-1,missingSince=-1;
 public Step missing(long now){return missing(now,false);}
 /** A known registration prompt waits for its newer choices below; do not return to old answers. */
 public Step missing(long now,boolean registration){
  if(started<0)started=now;
  if(now-started>=TIMEOUT_MS||swipes>=MAX_SWIPES)return Step.EXHAUSTED;
  if(lastMissing>=0&&now-lastMissing<ElementMonitor.INTERVAL_MS)return Step.WAIT;
  if(lastMissing<0||now-lastMissing>1500){misses=0;missingSince=now;}
  lastMissing=now;if(missingSince<0)missingSince=now;
  if(++misses<2||now-missingSince<1000)return Step.WAIT;
  return registration?Step.UP:(swipes%2==0?Step.DOWN:Step.UP);
 }
 public void moved(){if(swipes>=MAX_SWIPES)throw new IllegalStateException("探索上限");swipes++;misses=0;missingSince=-1;}
 public int pair(){return swipes/2+1;}
 public int movement(){return swipes+1;}
 public void found(){misses=0;lastMissing=-1;missingSince=-1;}
 public void reset(){found();swipes=0;started=-1;}
 /** Upward: one measured float height in pixels, clamped to the safe lane. Downward: unchanged. */
 public static float[] path(float top,float bottom,float upwardDistancePx,Step step){
  if(bottom-top<4||upwardDistancePx<=0||step!=Step.UP&&step!=Step.DOWN)throw new IllegalArgumentException("探索領域不明");
  float lo=top+1,hi=bottom-1,h=hi-lo;
  if(step==Step.DOWN)return new float[]{lo,hi};
  float start=lo+h*.75f;
  float distance=Math.min(upwardDistancePx,start-lo);
  return new float[]{start,start-distance};
 }
 /** Unit-density fallback; Android callers pass the measured float height explicitly. */
 public static float[] path(float top,float bottom,Step step){return path(top,bottom,48,step);}

 public static long duration(Step step){return step==Step.UP?350:150;}
 /** UP moves at no more than half Android's minimum fling velocity (px/s). */
 public static long duration(Step step,float distancePx,float minimumFlingVelocity){
  if(step!=Step.UP)return duration(step);
  if(!Float.isFinite(distancePx)||!Float.isFinite(minimumFlingVelocity)||distancePx<=0||minimumFlingVelocity<=0)return 2000;
  return Math.max(duration(step),(long)Math.ceil(distancePx*2000d/minimumFlingVelocity));
 }
}
