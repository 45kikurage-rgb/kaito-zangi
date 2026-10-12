package jp.kossacktouch.core;
/** Only the two observed coupon labels authorize navigation; never tap GET twice. */
public final class CouponFlow {
 private CouponFlow(){}
 public static String next(String stage,boolean canGet,boolean canDisplay){
  if("display_clicked".equals(stage))return "";
  if(canDisplay)return "クーポンを表示する";
  return !"get_clicked".equals(stage)&&canGet?"クーポンをゲットする":"";
 }
 public static boolean pending(String stage){return "get_clicked".equals(stage)||"display_clicked".equals(stage);}
}