package jp.kossacktouch.core;
/** A missing target twice authorizes one swipe, with at most three up/down pairs. */
public final class RevealSearch {
 public enum Step { WAIT, UP, DOWN, EXHAUSTED }
 private int misses,swipes;private long lastMissing=-1;
 public Step missing(long now){if(lastMissing>=0&&now-lastMissing<ElementMonitor.INTERVAL_MS)return Step.WAIT;if(lastMissing<0||now-lastMissing>1500)misses=0;lastMissing=now;if(++misses<2)return Step.WAIT;return swipes>=6?Step.EXHAUSTED:swipes%2==0?Step.UP:Step.DOWN;}
 public void moved(){if(swipes>=6)throw new IllegalStateException("探索上限");swipes++;misses=0;}
 public int pair(){return swipes/2+1;}
 public void found(){misses=0;lastMissing=-1;}
 public void reset(){found();swipes=0;}
 /** Distances are exactly 1:2 inside the measured LINE chat area, with one-pixel edge margins. */
 public static float[] path(float top,float bottom,Step step){
  if(bottom-top<4||step!=Step.UP&&step!=Step.DOWN)throw new IllegalArgumentException("探索領域不明");
  float lo=top+1,hi=bottom-1,h=hi-lo;
  return step==Step.UP?new float[]{lo+h*.75f,lo+h*.25f}:new float[]{lo,hi};
 }
 public static long duration(Step step){return step==Step.UP?350:150;}
}
