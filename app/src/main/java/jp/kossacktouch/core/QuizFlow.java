package jp.kossacktouch.core;
/** Only visible, unique quiz quick-replies authorize transitions. Old result rows never trigger taps. */
public final class QuizFlow {
 private QuizFlow(){}
 public static boolean complete(UiModel m){return m.quick("もう一度挑戦する")<0&&m.quick("問題を見る")<0&&!answerControls(m)&&completionEvidence(m);}
 private static boolean completionEvidence(UiModel m){
  // Campaign explanations also contain "5問連続正解". Only a result line or dedicated action is evidence.
  if(m.quick("クーポンをゲットする")>=0||m.quick("クーポンを表示する")>=0)return true;
  for(int i=m.qRow+1;i<m.flex.size();i++)for(String line:m.flex.get(i).split("\\n")){String t=line.trim();if(t.matches("5問連続正解[！!]+")||t.matches("見事難問をクリア[！!]+"))return true;}
  return false;
 }
 public static boolean correct(UiModel m){return m.newerContains("正解！")||m.newerContains("正解!");}
 public static boolean failed(UiModel m){return m.newerContains("残念！")||m.newerContains("不正解")||m.newerContains("チャレンジ失敗");}
 public static boolean finalGuide(UiModel m){
  String text=m.latest+"\n"+m.registrationContext();
  return (m.question==null||m.question.number==4)&&text.contains("ついに最終問題")&&text.contains("回答方法")&&!answerControls(m)&&m.quick("答え方確認しました！")<0;
 }
 public static boolean finalRevealForward(int step){return step%2==0;}
 public static boolean answerControls(UiModel m){for(String l:new String[]{"A","B","C","D"})if(m.quick(l)>=0)return true;return false;}
 public static String transition(UiModel m){
  if(complete(m))return "";
  if(!answerControls(m)){
   if(m.quick("もう一度挑戦する")>=0)return "もう一度挑戦する";
   if(m.quick("問題を見る")>=0)return "問題を見る";
   if(m.quick("答え方確認しました！")>=0&&m.latest.contains("回答方法"))return "答え方確認しました！";
   if(correct(m))for(String label:new String[]{"次の問題に進む","最後の問題へ進む","クーポンをゲットする"})if(m.quick(label)>=0)return label;
  }
  return "";
 }
}
