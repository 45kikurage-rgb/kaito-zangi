package jp.kossacktouch.core;
/** Geometry for the user-confirmed Sports 6 rich menu; never screen-fixed coordinates. */
public final class RichStartFlow {
 private RichStartFlow(){}
 public static boolean canStart(boolean trusted,boolean richMenu,boolean activeQuiz,boolean activeRegistration,boolean unsafePending){return trusted&&richMenu&&!activeQuiz&&!activeRegistration&&!unsafePending;}
 public static float[] target(float left,float top,float right,float bottom,int screenWidth,int screenHeight){
  float width=right-left,height=bottom-top;
  if(screenWidth<=0||screenHeight<=0||Float.isNaN(width)||Float.isNaN(height)||left<0||top<0||right>screenWidth||bottom>screenHeight||width<=0||height<=0||width<screenWidth*.8f)return null;
  float ratio=width/height;if(ratio<1.42f||ratio>1.56f)return null;
  // Header/title occupy the top portion; the upper centre action is at image y=0.49.
  return new float[]{left+width*.5f,top+height*.49f};
 }
}
