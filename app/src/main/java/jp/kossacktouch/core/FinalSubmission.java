package jp.kossacktouch.core;
/** Dedicated forms win. LINE messages require the observed numeric-answer guide before Q5. */
public final class FinalSubmission {
 public enum Route { FORM, MESSAGE, WAIT, STOP }
 public static boolean messageFormat(UiModel m){
  if(m.question==null||m.question.number!=5||QuizFlow.correct(m)||QuizFlow.failed(m)||QuizFlow.complete(m)||QuizFlow.answerControls(m))return false;
  for(int i=Math.max(0,m.qRow-3);i<m.qRow;i++){
   String t=Text.norm(m.flex.get(i));
   boolean numericGuide=t.contains("答えは数字")||t.contains("答えは整数または小数で入力してください");
   if(t.contains("回答方法")&&numericGuide&&(t.contains("小数点")||t.contains("スペース")))return true;
  }return false;
 }
 public static Route route(boolean formEvidence,int formInputs,boolean formSubmit,boolean messageFormat,int composers,boolean send){
  if(formEvidence)return formInputs==1&&formSubmit?Route.FORM:Route.WAIT;
  if(!messageFormat)return Route.STOP;
  if(composers>1)return Route.STOP;
  // The send control may only become enabled after a non-empty draft is prepared.
  return composers==1?Route.MESSAGE:Route.WAIT;
 }
 public static boolean numeric(String answer){return answer!=null&&answer.matches("-?[0-9]+(?:\\.[0-9]+)?");}
 public static boolean draftSafe(String existing,String answer,boolean prepared){return existing.isEmpty()||(prepared&&existing.equals(answer));}
}
