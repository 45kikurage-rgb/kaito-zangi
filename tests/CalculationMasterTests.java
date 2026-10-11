import jp.kossacktouch.core.*;
public class CalculationMasterTests {
 static int checks;
 static void eq(String wanted, String actual){checks++;if(!wanted.equals(actual))throw new AssertionError("calc "+checks+": "+actual+" expected "+wanted);}
 public static void main(String[] args){
  String sports="バレーボールの人数をa、カバディの人数をbとする。";
  eq("9.2",MathEngine.solve(sports+"3辺がa,b,xの三角形の面積が最大になるときの第3辺xは？小数第2位を四捨五入して答えよ。").answer);
  eq("81",MathEngine.solve(sports+"初項a、公差bの等差数列の第3項から第5項までの和を求めよ。").answer);
  eq("3",MathEngine.solve(sports+"初項a、公差bの等差数列の第1項からの和が25を初めて超える項を求めよ。").answer);
  eq("46",MathEngine.solve(sports+"a人とb人を一列に並べるとき両端が同じスポーツになる確率。小数点以下を四捨五入。").answer);
  eq("54",MathEngine.solve(sports+"a人とb人を一列に並べるとき両端が別のスポーツになる確率。小数点以下を四捨五入。").answer);
  String multiples=sports+"1から100までの数字をランダムに1つ選んだ時、";
  eq("72",MathEngine.solve(multiples+"aの倍数でもbの倍数でもない確率は何%か。小数点以下を四捨五入。").answer);
  eq("14",MathEngine.solve(multiples+"aの倍数であり、なおかつbの倍数でない確率は何%か。小数点以下を四捨五入。").answer);
  eq("12",MathEngine.solve(multiples+"bの倍数であり、なおかつaの倍数でない確率は何%か。小数点以下を四捨五入。").answer);
  eq("2",MathEngine.solve(multiples+"aの倍数かつbの倍数となる確率は何%か。小数点以下を四捨五入。").answer);
  eq("28",MathEngine.solve(multiples+"aまたはbの倍数となる確率は何%か。小数点以下を四捨五入。").answer);
  String boxing="アマチュアボクシング女子エリートWB新階級のライトフライ級の最大体重をa、フライ級の最大体重をbとする。1からaまでの自然数から";
  eq("11",MathEngine.solve(boxing+"異なる2つを選び、2数の差がbの正の約数となる確率。小数点以下を四捨五入。").answer);
  eq("89",MathEngine.solve(boxing+"異なる2つを選び、2数の差がbの正の約数ではない確率。小数点以下を四捨五入。").answer);
  eq("63",MathEngine.solve(boxing+"1つ選び、bと互いに素になる確率。小数点以下を四捨五入。").answer);
  eq("38",MathEngine.solve(boxing+"1つ選び、bと互いに素ではない確率。小数点以下を四捨五入。").answer);
  System.out.println("PASS "+checks+" CSV calculation patterns as on-device end-to-end cases.");
 }
}
