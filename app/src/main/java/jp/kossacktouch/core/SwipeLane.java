package jp.kossacktouch.core;
import java.util.*;
/** Find a continuous vertical lane outside the overlay and IME, in screen coordinates. */
public final class SwipeLane {
 private SwipeLane(){}
 public static float[] choose(float left,float top,float right,float bottom,List<float[]> blocked,float minHeight,float margin){
  if(right-left<margin*2||bottom-top<minHeight)return null;
  float[] best=null;float longest=0;
  for(float x:new float[]{(left+right)/2,left+margin,right-margin}){
   List<float[]> parts=new ArrayList<>();parts.add(new float[]{top,bottom});
   for(float[] b:blocked){if(x<b[0]-margin||x>b[2]+margin)continue;List<float[]> next=new ArrayList<>();
    for(float[] p:parts){float lo=b[1]-margin,hi=b[3]+margin;if(hi<=p[0]||lo>=p[1])next.add(p);else{if(lo>p[0])next.add(new float[]{p[0],lo});if(hi<p[1])next.add(new float[]{hi,p[1]});}}parts=next;
   }
   for(float[] p:parts)if(p[1]-p[0]>=minHeight&&p[1]-p[0]>longest){longest=p[1]-p[0];best=new float[]{x,p[0],p[1]};}
  }return best;
 }
}
