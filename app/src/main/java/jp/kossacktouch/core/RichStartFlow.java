package jp.kossacktouch.core;
/** Geometry for the user-confirmed Sports 6 rich menu; never screen-fixed coordinates. */
public final class RichStartFlow {
 private RichStartFlow(){}
 /** Closing an already-active drill never authorizes another Start request. */
 public static boolean shouldClose(boolean pending,String phase,boolean active,boolean open){
  if(!open)return false;
  if(!pending)return active;
  return "tapped".equals(phase)||(active&&("requested".equals(phase)||phase.isEmpty()));
 }
 public static boolean closeOnly(String phase){return "closing_existing".equals(phase);}
 /** Actual LINE fixtures put the click handler on this dedicated parent, not its text child. */
 public static int closeControl(java.util.List<UiModel.Node> nodes){
  int label=-1,button=-1;
  for(int i=0;i<nodes.size();i++){UiModel.Node n=nodes.get(i);if(n.visible&&n.enabled&&n.resource.endsWith(":id/chat_ui_oa_bottombar_menu_text")){
   String t=Text.clean(n.text);if(!t.isEmpty()&&!(t.contains("メニュー")&&t.contains("開く")&&t.contains("閉じる")))return -1;
   if(label>=0)return -1;label=i;
  }}
  if(label<0)return -1;
  for(int i=0;i<nodes.size();i++){UiModel.Node n=nodes.get(i);if(n.visible&&n.enabled&&n.clickable&&n.resource.endsWith(":id/chat_ui_oa_bottombar_promotion_menu_button")){if(button>=0)return -1;button=i;}}
  for(int depth=0,index=label;index>=0&&depth<5;depth++,index=nodes.get(index).parent)if(index==button)return button;
  return -1;
 }
 public static boolean canStart(boolean trusted,boolean richMenu,boolean activeQuiz,boolean activeRegistration,boolean unsafePending){return trusted&&richMenu&&!activeQuiz&&!activeRegistration&&!unsafePending;}
 public static float[] target(float left,float top,float right,float bottom,int screenWidth,int screenHeight){
  float width=right-left,height=bottom-top;
  if(screenWidth<=0||screenHeight<=0||Float.isNaN(width)||Float.isNaN(height)||left<0||top<0||right>screenWidth||bottom>screenHeight||width<=0||height<=0||width<screenWidth*.8f)return null;
  float ratio=width/height;if(ratio<1.42f||ratio>1.56f)return null;
  // Header/title occupy the top portion; the upper centre action is at image y=0.49.
  return new float[]{left+width*.5f,top+height*.49f};
 }
}
