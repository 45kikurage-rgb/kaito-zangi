import jp.kossacktouch.core.*;import java.util.*;
public class CouponShare20Tests {
 static int count;static void check(boolean x){count++;if(!x)throw new AssertionError("share20 "+count);}
 static CouponShare.Node n(int p,String label,boolean web,boolean visible,boolean clickable,int l,int t,int r,int b){return new CouponShare.Node(p,"",label,web,visible,true,clickable,l,t,r,b);}
 static List<CouponShare.Node> browser(){return new ArrayList<>(Arrays.asList(n(-1,"",false,true,false,0,0,1080,2400),n(0,"",true,true,false,0,150,1080,2100),n(0,"共有",false,true,true,650,2100,830,2260)));}
 public static void main(String[] args){
  List<CouponShare.Node> a=browser();check(CouponShare.select(a,1080,2400)==2);
  a.set(2,n(0,"共有 ボタン",false,true,true,650,2100,830,2260));check(CouponShare.select(a,1080,2400)==2);
  a.set(2,n(0,"Share",false,true,true,650,2100,830,2260));check(CouponShare.select(a,1080,2400)==2);
  a.set(2,n(0,"シェア",false,true,true,650,2100,830,2260));check(CouponShare.select(a,1080,2400)==2);
  a=browser();a.set(2,n(1,"共有",false,true,true,650,2100,830,2260));check(CouponShare.select(a,1080,2400)==-1);
  a=browser();a.set(2,n(0,"友だちに共有",false,true,true,650,2100,830,2260));check(CouponShare.select(a,1080,2400)==-1);
  a=browser();a.add(n(0,"共有",false,true,true,200,2100,400,2260));check(CouponShare.select(a,1080,2400)==-1);
  a=browser();a.set(2,n(0,"共有",false,false,true,650,2100,830,2260));check(CouponShare.select(a,1080,2400)==-1);
  a=browser();a.set(1,n(0,"",false,true,false,0,150,1080,2100));check(CouponShare.select(a,1080,2400)==-1);
  a=browser();a.set(2,n(0,"共有",false,true,true,650,1500,830,1700));check(CouponShare.select(a,1080,2400)==-1);
  a=browser();a.set(2,n(0,"",false,true,true,650,2100,830,2260));a.add(n(2,"共有",false,true,false,660,2120,800,2220));check(CouponShare.select(a,1080,2400)==2);
  a=browser();a.set(2,n(0,"共有",false,true,true,0,2100,1080,2260));check(CouponShare.select(a,1080,2400)==-1);
  a=browser();a.set(2,n(0,"共有",false,true,true,650,2350,830,2500));check(CouponShare.select(a,1080,2400)==-1);
  check(CouponShare.select(browser(),0,2400)==-1);
  System.out.println("PASS "+count+" code20 share checks: native toolbar, image-only WebView, nested label, ambiguous/hidden/content controls rejected.");
 }
}
