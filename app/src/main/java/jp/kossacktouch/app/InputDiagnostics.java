package jp.kossacktouch.app;
import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.graphics.Rect;
import android.os.*;
import android.view.accessibility.*;
import java.util.*;
/** Local, bounded diagnostic log. Never records draft, hint, conversation or clipboard contents. */
public final class InputDiagnostics {
 private InputDiagnostics(){}
 public static void add(Context c,String code,String detail){
  String old=Store.prefs(c).getString("input_diagnostics","");
  String line=SystemClock.elapsedRealtime()+" "+code+" "+detail+"\n";
  String value=old+line;if(value.length()>60000){int cut=value.indexOf('\n',value.length()-60000);value=cut<0?line:value.substring(cut+1);}
  Store.prefs(c).edit().putString("input_diagnostics",value).apply();
 }
 public static String read(Context c){return Store.prefs(c).getString("input_diagnostics","診断はまだありません");}
 public static boolean action(AccessibilityNodeInfo n,int id){for(AccessibilityNodeInfo.AccessibilityAction a:n.getActionList())if(a.getId()==id)return true;return false;}
 private static int length(CharSequence s){return s==null?0:s.length();}
 private static String attrs(AccessibilityNodeInfo n){Rect b=new Rect();n.getBoundsInScreen(b);StringBuilder a=new StringBuilder();for(AccessibilityNodeInfo.AccessibilityAction x:n.getActionList())a.append(x.getId()).append(',');return "id="+Screen.id(n)+" class="+n.getClassName()+" editable="+n.isEditable()+" enabled="+n.isEnabled()+" visible="+n.isVisibleToUser()+" focused="+n.isFocused()+" password="+n.isPassword()+" bounds="+b.flattenToString()+" textLength="+(n.isPassword()?-1:length(n.getText()))+" hintLength="+(n.isPassword()?-1:length(n.getHintText()))+" actions="+a;}
 public static void coupon(AccessibilityService c,Screen s){
  AccessibilityNodeInfo selected=s.couponButton("クーポンを表示する");add(c,"COUPON_SELECTED",selected==null?"none":attrs(selected));
  int count=0;for(AccessibilityNodeInfo n:s.nodes){if(!Screen.text(n).contains("クーポンを表示する"))continue;if(++count>20)break;add(c,"COUPON_NODE",attrs(n));AccessibilityNodeInfo p=n.getParent();for(int d=0;p!=null&&d<3;d++){AccessibilityNodeInfo next=p.getParent();add(c,"COUPON_PARENT",d+" id="+Screen.id(p)+" class="+p.getClassName());p.recycle();p=next;}if(p!=null)p.recycle();}
 }
 public static void capture(AccessibilityService c,String phase,Screen s){
  add(c,phase,"app="+Store.VERSION+" Android="+Build.VERSION.RELEASE+" sdk="+Build.VERSION.SDK_INT+" model="+Build.MANUFACTURER+"/"+Build.MODEL+" serviceFlags="+(c.getServiceInfo()==null?0:c.getServiceInfo().flags));
  List<AccessibilityWindowInfo> windows=c.getWindows();try{for(AccessibilityWindowInfo w:windows){AccessibilityNodeInfo r=w.getRoot();try{Rect b=new Rect();w.getBoundsInScreen(b);add(c,"WINDOW","id="+w.getId()+" type="+w.getType()+" layer="+w.getLayer()+" active="+w.isActive()+" focused="+w.isFocused()+" bounds="+b.flattenToString()+" package="+(r==null?"":r.getPackageName()));}finally{if(r!=null)r.recycle();}}}finally{for(AccessibilityWindowInfo w:windows)w.recycle();}
  if(s==null)return;
  String ver="unavailable";try{ver=c.getPackageManager().getPackageInfo(s.pkg,0).versionName;}catch(Exception ignored){}
  add(c,"SCREEN","package="+s.pkg+" version="+ver+" trusted="+s.trusted()+" q5="+s.currentFinal()+" questionVisible="+s.visibleQuestion()+" strictCandidates="+s.composers().size()+" testCandidates="+s.testComposers().size());
  AccessibilityNodeInfo focus=s.nodes.isEmpty()?null:s.nodes.get(0).findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
  if(focus!=null){try{add(c,"FOCUS_INPUT",attrs(focus));}finally{focus.recycle();}}
  int count=0;for(AccessibilityNodeInfo n:s.nodes){if(!(n.isEditable()||action(n,AccessibilityNodeInfo.ACTION_SET_TEXT)||String.valueOf(n.getClassName()).contains("EditText")))continue;if(++count>30){add(c,"CANDIDATES","truncated");break;}add(c,"CANDIDATE",attrs(n));AccessibilityNodeInfo p=n.getParent();for(int d=0;p!=null&&d<3;d++){AccessibilityNodeInfo next=p.getParent();add(c,"PARENT",d+" id="+Screen.id(p)+" class="+p.getClassName());p.recycle();p=next;}if(p!=null)p.recycle();}
  AccessibilityNodeInfo send=s.composerSend();add(c,"SEND_SELECTED",send==null?"count=0_or_ambiguous":attrs(send));
  int sendCount=0;for(AccessibilityNodeInfo n:s.nodes){if(!n.isClickable()||!n.isVisibleToUser())continue;String label=Screen.text(n).trim();if(!Screen.id(n).contains("send")&&!label.equals("送信")&&!label.equals("Send"))continue;if(++sendCount>20)break;add(c,"SEND_CANDIDATE",attrs(n));AccessibilityNodeInfo parent=n.getParent();for(int d=0;parent!=null&&d<3;d++){AccessibilityNodeInfo next=parent.getParent();add(c,"SEND_PARENT",d+" id="+Screen.id(parent)+" class="+parent.getClassName());parent.recycle();parent=next;}if(parent!=null)parent.recycle();}

 }
}
