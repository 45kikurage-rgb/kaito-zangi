package jp.kossacktouch.core;
import java.util.*;
/** Select the foreground application, never a background LINE hidden by another app/dialog. */
public final class ForegroundWindow {
 public static final int APPLICATION=1,INPUT_METHOD=2,SYSTEM=3,OVERLAY=4;
 public static final class Window {
  public final int type,layer;public final boolean active,focused;public final String pkg;
  public Window(int t,int l,boolean a,boolean f,String p){type=t;layer=l;active=a;focused=f;pkg=p;}
 }
 public static int select(List<Window> windows){
  int selected=-1;
  for(int i=0;i<windows.size();i++){Window w=windows.get(i);if(w.type==APPLICATION){
   if(selected<0||w.layer>windows.get(selected).layer)selected=i;
   else if(w.layer==windows.get(selected).layer)return -1;
  }}
  if(selected<0)return -1;Window app=windows.get(selected);
  if(!LineIdentity.candidate(app.pkg)||(!app.active&&!app.focused))return -1;
  for(Window w:windows)if(w.layer>app.layer&&w.type!=INPUT_METHOD&&!(w.type==OVERLAY&&w.pkg.equals("jp.kossacktouch.app"))&&(w.active||w.focused))return -1;
  return selected;
 }
 private ForegroundWindow(){}
}
