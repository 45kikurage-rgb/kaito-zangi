import jp.kossacktouch.core.*;import java.util.*;
public class FlowTests {
 static int c=0;static void check(boolean b){c++;if(!b)throw new AssertionError("flow "+c);}
 static UiModel screen(String question,String result,String...labels){List<UiModel.Node> ns=new ArrayList<>();if(!question.isEmpty())ns.add(new UiModel.Node(-1,"p:id/chat_ui_row_flex_message_frame",question,true,true,false));if(!result.isEmpty())ns.add(new UiModel.Node(-1,"p:id/chat_ui_row_flex_message_frame",result,true,true,false));for(String s:labels)ns.add(new UiModel.Node(-1,"p:id/chat_ui_quick_reply_item_root",s,true,true,true));return new UiModel(ns);}
 public static void main(String[] args){
  String q=VideoFixture.questions()[4];
  check(QuizFlow.transition(screen(q,"不正解！","もう一度挑戦する","ヒントを見る")).equals("もう一度挑戦する"));
  check(QuizFlow.transition(screen(q,"不正解！\n新しい挑戦です","問題を見る")).equals("問題を見る"));
  check(QuizFlow.transition(screen(q,"正解！","次の問題に進む")).equals("次の問題に進む"));
  check(QuizFlow.transition(screen(q,"正解！","最後の問題へ進む")).equals("最後の問題へ進む"));
  check(QuizFlow.transition(screen(q,"回答方法を確認","答え方確認しました！")).equals("答え方確認しました！"));
  check(QuizFlow.finalGuide(screen(q,"ついに最終問題！\n★回答方法\n答え方を確認したらボタンを押してください")));
  check(QuizFlow.finalGuide(screen("","ついに最終問題！\n★回答方法")));
  check(!QuizFlow.finalGuide(screen(q,"最終問題では回答方法が違います")));
  check(!QuizFlow.finalGuide(screen(q,"ついに最終問題！\n★回答方法","答え方確認しました！")));
  check(!QuizFlow.finalGuide(screen(q.replace("第4問","第5問"),"★回答方法")));
  check(QuizFlow.finalRevealForward(0));check(!QuizFlow.finalRevealForward(1));check(QuizFlow.finalRevealForward(2));
  check(QuizFlow.complete(screen(q,"5問連続正解！")));
  check(!QuizFlow.complete(screen(q,"5問連続正解！\n再挑戦","問題を見る")));
  check(QuizFlow.transition(screen(q,"不正解！","A","もう一度挑戦する")).isEmpty());
  check(QuizFlow.transition(screen(q,"不正解！","もう一度挑戦する","もう一度挑戦する")).isEmpty());
  check(QuizFlow.transition(screen(q,"正解！","次の問題に進む","次の問題に進む")).isEmpty());
  check(QuizFlow.transition(screen(q,"","次の問題に進む")).isEmpty());
  check(QuizFlow.transition(screen(q,"正解！","A","次の問題に進む")).isEmpty());
  Question question=Question.parse(q);RunGuard guard=new RunGuard();guard.restore(question.key,5000);check(!guard.canAnswer(question));check(guard.lastConfirmed==3);check(!guard.confirmNext(new Question(3,"old",question.choices)));check(guard.confirmNext(new Question(5,"final",new LinkedHashMap<>())));
  RunGuard finalGuard=new RunGuard();Question finalQ=new Question(5,"final",new LinkedHashMap<>());finalGuard.restore(finalQ.key,5000);check(finalGuard.finalPending&&!finalGuard.canAnswer(finalQ));
  RunGuard newRun=new RunGuard();newRun.lastConfirmed=question.number-1;check(newRun.canAnswer(question));
  System.out.println("PASS "+c+" flow checks: retry/open/correct/final/completion, duplicate and conflicting controls, resumed pending and current question numbering.");
 }
}
