import jp.kossacktouch.core.*;
public class RichStart27Tests {
 static int count;static void check(boolean ok){count++;if(!ok)throw new AssertionError("richStart27 "+count);}
 public static void main(String[] args){
  // Supplied 02-16420.png: image [0,950,684,1410], upper-centre Start cell y=1122..1231.
  float[] point=RichStartFlow.target(0,950,684,1410,684,1520);
  check(point!=null);check(point[0]==342);check(point[1]>1122&&point[1]<1231);
  for(float scale:new float[]{.5f,1,1.5f,2,3}){
   point=RichStartFlow.target(0,950*scale,684*scale,1410*scale,(int)(684*scale),(int)(1520*scale));
   check(point!=null);check(Math.abs(point[0]-342*scale)<.01f);check(point[1]>1122*scale&&point[1]<1231*scale);
  }
  point=RichStartFlow.target(42,800,642,1200,684,1520);check(point!=null&&point[0]==342);
  check(RichStartFlow.target(0,950,684,1410,0,1520)==null);
  check(RichStartFlow.target(-1,950,683,1410,684,1520)==null);
  check(RichStartFlow.target(0,-1,684,459,684,1520)==null);
  check(RichStartFlow.target(1,950,685,1410,684,1520)==null);
  check(RichStartFlow.target(0,1100,684,1560,684,1520)==null);
  check(RichStartFlow.target(0,950,300,1150,684,1520)==null);
  check(RichStartFlow.target(0,950,684,1400,684,0)==null);
  check(RichStartFlow.target(0,950,684,950,684,1520)==null);
  check(RichStartFlow.target(0,950,0,1410,684,1520)==null);
  check(RichStartFlow.target(0,950,684,1200,684,1520)==null);
  check(RichStartFlow.target(0,500,684,1400,684,1520)==null);
  check(RichStartFlow.target(Float.NaN,950,684,1410,684,1520)==null);
  check(RichStartFlow.target(0,950,Float.POSITIVE_INFINITY,1410,684,1520)==null);
  for(int mask=0;mask<32;mask++){
   boolean trusted=(mask&1)!=0,menu=(mask&2)!=0,quiz=(mask&4)!=0,registration=(mask&8)!=0,pending=(mask&16)!=0;
   check(RichStartFlow.canStart(trusted,menu,quiz,registration,pending)==(mask==3));
  }
  System.out.println("PASS "+count+" code27 rich-start checks: observed upper-centre geometry, density/offset, invalid bounds and all trusted/menu/quiz/registration/pending combinations.");
 }
}
