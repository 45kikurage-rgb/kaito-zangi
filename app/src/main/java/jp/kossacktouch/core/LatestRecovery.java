package jp.kossacktouch.core;
/** A native latest-position click is a request, not proof that navigation completed. */
public final class LatestRecovery {
 public enum Action { NONE, REQUEST, WAIT, EXHAUSTED }
 public static final long TIMEOUT_MS=6000;
 private long requestedAt=-1;
 public Action observe(boolean buttonVisible,long now){
  if(!buttonVisible){reset();return Action.NONE;}
  if(requestedAt<0)return Action.REQUEST;
  return now-requestedAt>=TIMEOUT_MS?Action.EXHAUSTED:Action.WAIT;
 }
 public void requested(long now){if(requestedAt<0)requestedAt=now;}
 public void reset(){requestedAt=-1;}
}
