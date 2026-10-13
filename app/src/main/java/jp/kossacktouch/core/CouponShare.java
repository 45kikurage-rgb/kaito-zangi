package jp.kossacktouch.core;
import java.util.*;
/** Native bottom-toolbar share only, after the caller verified coupon-link navigation. */
public final class CouponShare {
 private CouponShare(){}
 public static final class Node {
  public final int parent,left,top,right,bottom;public final String id,label;public final boolean web,visible,enabled,clickable;
  public Node(int p,String id,String label,boolean web,boolean v,boolean e,boolean c,int l,int t,int r,int b){parent=p;this.id=id;this.label=label;this.web=web;visible=v;enabled=e;clickable=c;left=l;top=t;right=r;bottom=b;}
 }
 private static boolean inWeb(List<Node> ns,int i){for(int d=0;i>=0&&i<ns.size()&&d<48;d++,i=ns.get(i).parent)if(ns.get(i).web)return true;return false;}
 private static boolean shareLabel(Node n){String s=n.label.trim().replace(" ボタン","").replace("ボタン","").trim();return s.equals("共有")||s.equals("シェア")||s.equalsIgnoreCase("share")||s.equalsIgnoreCase("share button");}
 public static int select(List<Node> ns,int width,int height){
  if(width<=0||height<=0)return -1;
  int webBottom=-1;for(Node n:ns)if(n.web&&n.visible&&n.right>n.left&&n.bottom>n.top&&n.bottom-n.top>=height*.25)webBottom=Math.max(webBottom,n.bottom);
  if(webBottom<0)return -1;
  Set<Integer> matches=new HashSet<>();
  for(int i=0;i<ns.size();i++){Node n=ns.get(i);if(!n.visible||!n.enabled||!shareLabel(n)||inWeb(ns,i))continue;
   int target=i;for(int d=0;!ns.get(target).clickable&&d<2;d++){int p=ns.get(target).parent;if(p<0||p>=ns.size())break;target=p;}
   Node t=ns.get(target);
   if(!t.visible||!t.enabled||!t.clickable||inWeb(ns,target)||t.left<0||t.right>width||t.top<height*.55||t.bottom>height||t.right<=t.left||t.bottom<=t.top||t.right-t.left>width*.4||t.bottom-t.top>height*.18)continue;
   // Reject content links: toolbar must sit at/below the WebView's bottom edge.
   if(t.top<webBottom-height*.035)continue;
   matches.add(target);
  }
  return matches.size()==1?matches.iterator().next():-1;
 }
}
