package jp.kossacktouch.core;
/** Two consecutive one-second observations; input changes and action completion restart confirmation. */
public final class ElementMonitor {
 public static final long INTERVAL_MS=1000;
 private String key="";private int count;private long observed=-1;
 public boolean observe(String next,long now){
  if(observed>=0&&now-observed<INTERVAL_MS)return false;
  if(!next.equals(key)||observed<0||now-observed>2500)count=0;
  key=next;observed=now;return ++count>=2;
 }
 public void reset(){key="";count=0;observed=-1;}
}
