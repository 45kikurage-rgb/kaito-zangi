package jp.kossacktouch.core;
/** Once Q5 is confirmed, scrolling history never authorizes a different question or result. */
public final class FinalAnchor {
 private String key="";private boolean recovery;
 public void bind(Question q){if(key.isEmpty()&&q!=null&&q.number==5)key=q.key;}
 public boolean mismatch(Question q){return !key.isEmpty()&&(q==null||!key.equals(q.key));}
 public boolean recoverOnce(){if(recovery)return false;recovery=true;return true;}
 public void reset(){key="";recovery=false;}
}
