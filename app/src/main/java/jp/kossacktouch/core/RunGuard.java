package jp.kossacktouch.core;
/** One answer attempt per question and run, persisted by the Android adapter before clicking. */
public final class RunGuard {
 public String pending=""; public int lastConfirmed=0; public long sentAt=0; public boolean finalPending=false;
 public void mark(Question q,long now){if(!pending.isEmpty())throw new IllegalStateException("確定確認待ち");pending=q.key;sentAt=now;finalPending=q.number==5;}
 public boolean confirmNext(Question next){if(pending.isEmpty()||next==null||next.number!=lastConfirmed+2)return false;lastConfirmed=next.number-1;pending="";finalPending=false;return true;}
 public boolean canAnswer(Question q){return pending.isEmpty()&&q.number==lastConfirmed+1;}
}
