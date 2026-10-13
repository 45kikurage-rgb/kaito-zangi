import jp.kossacktouch.core.*;import java.util.*;
public class OverlayCoupon19Tests {
 static int count;static void check(boolean ok){count++;if(!ok)throw new AssertionError("overlay/coupon19 "+count);}
 static UiModel.Node n(int p,String id,String text,boolean visible,boolean clickable){return new UiModel.Node(p,"jp.naver.line.andronj:id/"+id,text,visible,true,clickable);}
 static List<UiModel.Node> card(){return new ArrayList<>(Arrays.asList(n(-1,"chat_ui_row_flex_message_frame","",true,false),n(0,"text","見事難問をクリアした諸君へ。セブン-イレブンのクーポン。下記クーポンバーコードをお持ちください。",true,false),n(0,"different_label_id","クーポンを表示する",true,false)));}
 public static void main(String[] args){
  float[] lane=SwipeLane.choose(0,100,1080,2200,Arrays.asList(new float[]{0,0,1080,860}),480,36);check(lane!=null);check(lane[1]>=896);check(lane[2]==2200);
  lane=SwipeLane.choose(0,100,1080,2400,Arrays.asList(new float[]{0,0,1080,860},new float[]{0,1377,1080,2621}),300,24);check(lane!=null);check(lane[1]>=884);check(lane[2]<=1353);
  lane=SwipeLane.choose(0,100,1080,2200,Arrays.asList(new float[]{0,700,1080,1100}),400,24);check(lane!=null);check(lane[1]>=1124);
  lane=SwipeLane.choose(0,100,1080,2200,Arrays.asList(new float[]{0,0,700,700}),400,24);check(lane!=null);check(lane[0]>724);check(lane[1]==100);
  check(SwipeLane.choose(0,100,1080,1200,Arrays.asList(new float[]{0,0,1080,1100}),300,24)==null);
  lane=SwipeLane.choose(0,100,1080,2200,Collections.emptyList(),400,24);check(lane[1]==100&&lane[2]==2200);
  float[] down=RevealSearch.path(lane[1],lane[2],2400,RevealSearch.Step.DOWN);check(down[0]==101&&down[1]==2199);
  // Exact separate link under a coupon card; the photograph establishes labels, not this synthetic tree.
  List<UiModel.Node> ns=card();check(new UiModel(ns).couponLink("クーポンを表示する")==2);check(new UiModel(ns).couponLink("クーポンをゲットする")==-1);
  ns.set(2,n(0,"different_label_id","クーポンを表示する",true,true));check(new UiModel(ns).couponLink("クーポンを表示する")==2);
  ns=card();ns.set(2,n(0,"wrapper","",true,true));ns.add(n(2,"label","クーポンを表示する",true,false));check(new UiModel(ns).couponLink("クーポンを表示する")==2);
  ns=card();ns.add(n(0,"duplicate","クーポンを表示する",true,true));check(new UiModel(ns).couponLink("クーポンを表示する")==-1);
  ns=card();ns.set(2,n(0,"label","クーポンを表示する",false,true));check(new UiModel(ns).couponLink("クーポンを表示する")==-1);
  ns=card();ns.set(1,n(0,"text","説明中のクーポン",true,false));check(new UiModel(ns).couponLink("クーポンを表示する")==-1);
  ns=card();ns.add(n(-1,"chat_ui_row_flex_message_frame","新しいメッセージ",true,false));check(new UiModel(ns).couponLink("クーポンを表示する")==-1);
  ns=card();ns.set(2,n(0,"label","クーポンを表示する方法はこちら",true,true));check(new UiModel(ns).couponLink("クーポンを表示する")==-1);
  ns=card();ns.set(2,n(0,"label","クーポンを表示する\n別のリンク",true,true));check(new UiModel(ns).couponLink("クーポンを表示する")==-1);
  check(CouponFlow.next("get_clicked",true,true).equals("クーポンを表示する"));check(CouponFlow.next("display_clicked",false,true).isEmpty());
  System.out.println("PASS "+count+" code19 checks: overlay/IME-safe swipe lanes, unchanged unobstructed downward swipe, exact latest coupon link and duplicate guards.");
 }
}
