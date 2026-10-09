package jp.kossacktouch.core;
import java.math.*;
import java.util.*;
import java.util.regex.*;

/** Pure on-device arithmetic. No network, model, or cached numeric answers. */
public final class MathEngine {
 public static final class Result {
  public final String type, answer, detail;
  Result(String t,String a,String d){type=t;answer=a;detail=d;}
 }
 public static final Map<String,Integer> SPORTS = new LinkedHashMap<>();
 public static final Map<String,Integer> BOXING = new LinkedHashMap<>();
 static {
  SPORTS.put("カーリング",4); SPORTS.put("バスケットボール",5); SPORTS.put("ビーチサッカー",5); SPORTS.put("バレーボール",6); SPORTS.put("アイスホッケー",6); SPORTS.put("カバディ",7); SPORTS.put("ハンドボール",7); SPORTS.put("水球",7); SPORTS.put("サッカー",11); SPORTS.put("ホッケー",11);
  BOXING.put("ライトフライ級",48); BOXING.put("フライ級",51); BOXING.put("バンタム級",54); BOXING.put("フェザー級",57); BOXING.put("ライト級",60); BOXING.put("ウェルター級",65); BOXING.put("ライトミドル級",70); BOXING.put("ミドル級",75); BOXING.put("ライトヘビー級",80);
 }
 public static long gcd(long a,long b){while(b!=0){long t=a%b;a=b;b=t;}return Math.abs(a);}
 private static void positive(long... n){for(long x:n)if(x<=0||x>1000000)throw new IllegalArgumentException("数値の範囲外");}
 private static String percent(long count,long total,int digits){
  BigDecimal numerator=BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(100));
  if(digits==-1){try{return numerator.divide(BigDecimal.valueOf(total)).stripTrailingZeros().toPlainString();}catch(ArithmeticException e){throw new IllegalArgumentException("循環小数の丸め方法を確認してください");}}
  return numerator.divide(BigDecimal.valueOf(total),digits,RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
 }
 public static Result coprime(int a,int b,boolean inverse,int digits){positive(a,b);long c=0;for(int n=1;n<=a;n++)if(gcd(n,b)==1)c++;if(inverse)c=a-c;return new Result("互いに素",percent(c,a,digits),"a="+a+", b="+b+", 該当="+c+"/"+a);}
 public static Result divisorDifference(int a,int b,boolean inverse,int digits){positive(a,b);if(a<2)throw new IllegalArgumentException("2数が選べない");long c=0;for(int d=1;d<a;d++)if(b%d==0)c+=a-d;long total=(long)a*(a-1)/2;if(inverse)c=total-c;return new Result("差と約数",percent(c,total,digits),"a="+a+", b="+b+", 該当="+c+"/"+total);}
 public static Result triangle(int a,int b,int digits){positive(a,b);return new Result("三角形",BigDecimal.valueOf(Math.hypot(a,b)).setScale(digits,RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(),"直角時に面積最大: x=√("+a+"²+"+b+"²)");}
 public static Result ends(int a,int b,boolean same,int digits){positive(a,b);long t=(long)a+b;long c=same?(long)a*(a-1)+(long)b*(b-1):2L*a*b;return new Result("両端",percent(c,t*(t-1),digits),"a="+a+", b="+b+", 両端="+(same?"同じ":"別"));}
 public static Result multiples(int a,int b,int n,String mode,int digits){positive(a,b,n);long common=(long)a/gcd(a,b)*b;long ca=n/a,cb=n/b,cab=n/common,c;
  switch(mode){case "neither":c=n-ca-cb+cab;break;case "a-not-b":c=ca-cab;break;case "b-not-a":c=cb-cab;break;case "both":c=cab;break;case "either":c=ca+cb-cab;break;default:throw new IllegalArgumentException("倍数の条件不明");}
  return new Result("倍数",percent(c,n,digits),"a="+a+", b="+b+", N="+n+", 条件="+mode+", 該当="+c);
 }
 public static long term(long a,long d,long n){return Math.addExact(a,Math.multiplyExact(n-1,d));}
 public static long sum(long a,long d,long n){return Math.multiplyExact(n,Math.addExact(2*a,Math.multiplyExact(n-1,d)))/2;}
 public static Result arithmeticRange(int a,int d,int from,int to){positive(a,d,from,to);if(from>to)throw new IllegalArgumentException("項の順序が逆");long n=to-from+1;long v=Math.multiplyExact(n,Math.addExact(term(a,d,from),term(a,d,to)))/2;return new Result("等差数列の和",Long.toString(v),"初項="+a+", 公差="+d+", 第"+from+"〜"+to+"項");}
 public static Result arithmeticThreshold(int a,int d,int target){positive(a,d,target);int lo=1,hi=1;while(sum(a,d,hi)<=target){hi*=2;if(hi>1000000)throw new IllegalArgumentException("項数超過");}while(lo<hi){int m=(lo+hi)/2;if(sum(a,d,m)>target)hi=m;else lo=m+1;}if(sum(a,d,lo)<=target||(lo>1&&sum(a,d,lo-1)>target))throw new IllegalArgumentException("検算失敗");return new Result("等差数列の閾値",Integer.toString(lo),"S"+(lo-1)+"="+sum(a,d,lo-1)+" ≤ "+target+" < S"+lo+"="+sum(a,d,lo));}
 private static int number(String q,String... patterns){Integer found=null;for(String p:patterns){Matcher m=Pattern.compile(p).matcher(q);while(m.find()){int v=Integer.parseInt(m.group(1));if(found!=null&&found!=v)throw new IllegalArgumentException("条件が複数あり確定できない");found=v;}}if(found==null)throw new IllegalArgumentException("必要な数値を取得できない");return found;}
 private static int rounding(String q){
  if(q.contains("小数点以下を四捨五入")||q.contains("小数以下を四捨五入")||q.contains("整数に四捨五入"))return 0;
  Matcher m=Pattern.compile("小数(?:点)?第([1-4])位を四捨五入").matcher(q);if(m.find())return Integer.parseInt(m.group(1))-1;
  m=Pattern.compile("小数(?:点)?第([1-3])位まで(?:求め|答え|表示)").matcher(q);if(m.find()&&(q.contains("四捨五入")))return Integer.parseInt(m.group(1));
  throw new IllegalArgumentException("丸め方法を確認してください");
 }
 private static List<Integer> named(String q,Map<String,Integer> map){
  List<Integer> values=new ArrayList<>(); List<String> names=new ArrayList<>(map.keySet());names.sort((a,b)->Integer.compare(b.length(),a.length()));
  String regex=String.join("|",names.stream().map(Pattern::quote).toArray(String[]::new));Matcher m=Pattern.compile(regex).matcher(q);while(m.find())values.add(map.get(m.group()));return values;
 }
 private static int[] parameters(String q,boolean boxing){
  Integer a=null,b=null;
  if(Pattern.compile("(?:a|初項)(?:=|は)[0-9]+").matcher(q).find())a=number(q,"(?:a|初項)(?:=|は)([0-9]+)");
  if(Pattern.compile("(?:b|公差)(?:=|は)[0-9]+").matcher(q).find())b=number(q,"(?:b|公差)(?:=|は)([0-9]+)");
  if(a!=null&&b!=null)return new int[]{a,b};
  if(boxing&&!(q.contains("女子")&&q.contains("エリート")&&q.contains("WB")))throw new IllegalArgumentException("ボクシング階級の体系が未対応");
  List<Integer> v=named(q,boxing?BOXING:SPORTS);
  if(v.size()!=2)throw new IllegalArgumentException("競技名・階級のa,bが一意でない");
  // The observed ends-of-a-row problem names each team without introducing a or b.
  // Only this symmetric family can omit roles; no other family infers variable definitions.
  if(!boxing&&!q.contains("a")&&!q.contains("b")&&q.contains("各1チーム")&&q.contains("スターティングメンバー")&&q.contains("両端")&&(q.contains("1列")||q.contains("一列")))return new int[]{v.get(0),v.get(1)};
  // Preserve semantic role; don't silently swap initial term and difference.
  if(!boxing&&q.contains("初項")&&q.contains("公差")&&q.indexOf("公差")<q.indexOf("初項"))throw new IllegalArgumentException("a,bの順序を要確認");
  if(!q.contains("a")||!q.contains("b")||q.indexOf("b")<q.indexOf("a"))throw new IllegalArgumentException("a,bの定義を要確認");
  return new int[]{v.get(0),v.get(1)};
 }
 /** Strict template classifier. Unsupported phrasing returns no numeric guess. */
 public static Result solve(String raw){
  String q=Text.clean(raw);if(q.length()>6000)throw new IllegalArgumentException("問題文が長すぎる");
  if(q.contains("切り捨て")||q.contains("切り上げ")||q.contains("以上の確率"))throw new IllegalArgumentException("未対応の丸め・条件");
  boolean boxing=q.contains("ボクシング")||q.contains("互いに素")||q.contains("正の約数");
  int[] p=parameters(q,boxing);int a=p[0],b=p[1];
  if(boxing){int digits=rounding(q);if(!q.contains("1からaまで")&&!q.contains("1~a")&&!q.contains("1〜a"))throw new IllegalArgumentException("抽出範囲が未対応");
   if(q.contains("互いに素")){if(!q.contains("1つ")&&!q.contains("1個"))throw new IllegalArgumentException("選択数が未対応");boolean inverse=q.contains("互いに素ではない")||q.contains("互いに素でない");if(!inverse&&!q.contains("互いに素になる確率")&&!q.contains("互いに素である確率"))throw new IllegalArgumentException("互いに素の条件が未対応");return coprime(a,b,inverse,digits);}
   if(q.contains("差")&&q.contains("正の約数")&&(q.contains("異なる2")||q.contains("異なる二"))){boolean inverse=q.contains("約数ではない")||q.contains("約数でない");if(!inverse&&!q.contains("約数となる確率")&&!q.contains("約数になる確率"))throw new IllegalArgumentException("約数の条件が未対応");return divisorDifference(a,b,inverse,digits);}
  }
  if(q.contains("三角形")&&q.contains("面積")&&q.contains("最大")&&q.contains("x")&&(q.contains("3辺")||q.contains("三辺")))return triangle(a,b,rounding(q));
  if(q.contains("等差数列")||q.contains("初項")&&q.contains("公差")){
   if(q.contains("初めて")&&q.contains("超え")){int t=number(q,"(?:和が|和は)([0-9]+)を初めて超え","初めて([0-9]+)を超え");return arithmeticThreshold(a,b,t);}
   Matcher m=Pattern.compile("第([0-9]+)項(?:から|〜|~)第([0-9]+)項までの和").matcher(q);if(m.find())return arithmeticRange(a,b,Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)));
  }
  if(q.contains("両端")&&(q.contains("並")||q.contains("一列"))){if(Pattern.compile("同じ(?:スポーツ|競技)?(?:ではない|でない)").matcher(q).find())return ends(a,b,false,rounding(q));if(q.contains("同じ")&&(q.contains("異なる")||q.contains("別の")))throw new IllegalArgumentException("両端の条件が曖昧です");if(q.contains("同じ"))return ends(a,b,true,rounding(q));if(q.contains("異なる")||q.contains("別の"))return ends(a,b,false,rounding(q));}
  if(q.contains("倍数")&&(q.contains("1つ")||q.contains("1個"))){int n=number(q,"1から([0-9]+)まで");String mode=null;
   if(q.contains("aの倍数でもbの倍数でもない"))mode="neither";
   else if(Pattern.compile("aの倍数で(?:あり[、,]?(?:(?:なお)?かつ)?|[、,](?:(?:なお)?かつ)?|かつ)bの倍数で(?:は)?ない").matcher(q).find())mode="a-not-b";
   else if(Pattern.compile("bの倍数で(?:あり[、,]?(?:(?:なお)?かつ)?|[、,](?:(?:なお)?かつ)?|かつ)aの倍数で(?:は)?ない").matcher(q).find())mode="b-not-a";
   else if(q.contains("両方の倍数")||q.contains("aの倍数かつbの倍数"))mode="both";
   else if(q.contains("少なくとも一方")||q.contains("aまたはbの倍数"))mode="either";
   // With no rounding instruction, only a mathematically exact terminating decimal is allowed.
   if(mode!=null)return multiples(a,b,n,mode,q.contains("四捨五入")||q.contains("小数第")||q.contains("小数点第")?rounding(q):-1);
  }
  throw new IllegalArgumentException("計算形式・条件が未対応です");
 }
}
