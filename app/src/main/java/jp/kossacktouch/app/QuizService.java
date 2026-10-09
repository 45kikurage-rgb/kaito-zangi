package jp.kossacktouch.app;
import android.accessibilityservice.*;
import android.app.KeyguardManager;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.*;
import android.widget.*;
import jp.kossacktouch.core.*;

public final class QuizService extends AccessibilityService {
 public static QuizService instance;
 private final Handler handler=new Handler(Looper.getMainLooper());private WindowManager wm;private LinearLayout overlay;private TextView info;private Button play;private WindowManager.LayoutParams layout;
 private AnswerBank bank;private boolean running=false;private RunGuard guard=new RunGuard();private String stable="",current="",answer="",transition="";private int stableCount=0,scrolls=0;private long lastTick=0,lastAction=0,questionSince=0;private String previousPackage="";
 private final java.util.Random registrationRandom=new java.util.Random();private int registrationSteps=0;private long registrationSince=0,registrationSent=0;private String registrationPending="",registrationPriorQuestion="";
 private android.content.SharedPreferences session;private String runPackage="",finalPrepared="";private boolean composerOpened=false,inputFocused=false;
 private boolean resumeChecked=false;private String savedPending="",savedPackage="",transitionScreen="";private int transitionSteps=0,finalRevealSteps=0;private long finalRevealAt=0;
 private final Runnable poll=new Runnable(){public void run(){check();handler.postDelayed(this,5000);}};
 @Override protected void onServiceConnected(){instance=this;try{bank=Store.bank(this);}catch(Exception e){status("要確認","正答データを読み込めません");}wm=(WindowManager)getSystemService(WINDOW_SERVICE);handler.post(poll);}
 public void showFloat(){if(overlay!=null)return;overlay=new LinearLayout(this);overlay.setOrientation(LinearLayout.VERTICAL);overlay.setPadding(dp(12),dp(8),dp(12),dp(8));overlay.setBackgroundColor(Color.rgb(17,40,51));
  info=new TextView(this);info.setTextColor(Color.WHITE);info.setTextSize(12);info.setText("回答ザンギ\n待機");overlay.addView(info);
  LinearLayout row=new LinearLayout(this);play=new Button(this);play.setText("▶ 再生");play.setOnClickListener(v->{if(running)stop("停止","利用者が停止しました");else startRun();});row.addView(play);
  Button close=new Button(this);close.setText("×");close.setOnClickListener(v->{stop("停止","フロートを閉じました");hideFloat();});row.addView(close);overlay.addView(row);
  layout=new WindowManager.LayoutParams(dp(200),WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);layout.gravity=Gravity.TOP|Gravity.LEFT;layout.x=dp(12);layout.y=dp(95);
  info.setOnTouchListener(new View.OnTouchListener(){float x,y;int ox,oy;public boolean onTouch(View v,MotionEvent e){if(e.getAction()==0){x=e.getRawX();y=e.getRawY();ox=layout.x;oy=layout.y;return true;}if(e.getAction()==2){layout.x=Math.max(0,ox+(int)(e.getRawX()-x));layout.y=Math.max(0,oy+(int)(e.getRawY()-y));wm.updateViewLayout(overlay,layout);return true;}return true;}});
  try{wm.addView(overlay,layout);}catch(Exception e){overlay=null;stop("要確認","フロートを表示できません");}
 }
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private void hideFloat(){if(overlay!=null){wm.removeView(overlay);overlay=null;info=null;play=null;}}
 private void startRun(){if(bank==null){stop("要確認","正答データ不備");return;}
  session=null;runPackage="";finalPrepared="";composerOpened=false;inputFocused=false;savedPending="";savedPackage="";resumeChecked=false;transitionSteps=0;transitionScreen="";finalRevealSteps=0;finalRevealAt=0;
  registrationSteps=0;registrationSince=0;registrationSent=0;registrationPending="";registrationPriorQuestion="";guard=new RunGuard();stable="";stableCount=0;current="";answer="";scrolls=0;transition="";lastAction=0;lastTick=0;questionSince=SystemClock.elapsedRealtime();running=true;play.setText("■ 停止");status("認識中","現在のLINE画面から開始します");check();
 }
 public void stop(String state,String detail){running=false;if(play!=null)play.setText("▶ 再生");status(state,detail);Store.log(this,state,current,answer,detail);}
 private void status(String state,String detail){Store.prefs(this).edit().putString("state",state).putString("detail",detail).putString("question",current).putString("answer",answer).apply();if(info!=null)info.setText("回答ザンギ · "+state+"\n"+detail);}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){if(!running)return;int type=e.getEventType();String pkg=String.valueOf(e.getPackageName());
  if(type==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED&&!pkg.equals(getPackageName())&&!pkg.contains("inputmethod")&&!pkg.contains("keyboard")&&!pkg.equals("com.android.systemui")){if(!runPackage.isEmpty()&&Store.allowed(this,pkg)&&!runPackage.equals(pkg)){stop("停止","LINEを切り替えました。対象トークで▶再生してください");return;}previousPackage=pkg;if(!Store.allowed(this,pkg)){stop("要確認","LINE以外の画面へ移動しました");return;}}
  if(type==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED||type==AccessibilityEvent.TYPE_VIEW_SCROLLED||type==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)handler.post(this::check);
 }
 private void check(){if(!running)return;long now=SystemClock.elapsedRealtime();if(now-lastTick<5000)return;lastTick=now;
  if(((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()){stop("要確認","画面ロック・画面OFFを検知しました");return;}
  if(now-questionSince>240000){stop("要確認","問題の制限時間に近づいたため停止しました");return;}
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null){stop("要確認","画面要素を取得できません");return;}
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();stop("要確認","対応するLINEの画面ではありません");return;}
  try(Screen s=new Screen(root)){
   if(!s.trusted()){stop("要確認","対象LINE画面を確認できません（権限・トーク・要素数を確認）");return;}
   if(runPackage.isEmpty()){
    runPackage=s.pkg;Store.migrateSession(this,s.pkg);session=Store.session(this,s.pkg);
    savedPending=session.getString("pending","");savedPackage=s.pkg;registrationPending=session.getString("registration_pending","");registrationPriorQuestion=session.getString("registration_prior_question","");
    finalPrepared=session.getString("final_prepared","");Store.prefs(this).edit().putString("active_line_package",s.pkg).apply();
   }else if(!runPackage.equals(s.pkg)){stop("停止","LINEを切り替えました。対象トークで▶再生してください");return;}
   previousPackage=s.pkg;
   boolean complete=QuizFlow.complete(s.model);
   if(complete){clearPending();guard.pending="";stop("完了","クイズの完了表示を確認しました");return;}
   String next=QuizFlow.transition(s.model);
   if(next.isEmpty()&&QuizFlow.finalGuide(s.model)){
    if(finalRevealSteps>=6){stop("要確認","最終問題の確認ボタンを上下に探しましたが表示されません");return;}
    if(now-finalRevealAt>=900){revealFinalButton(s,now);handler.postDelayed(this::check,1100);}
    return;
   }
   if(now-lastAction<4500)return;
   if(!resumeChecked){
    if(s.question!=null||!next.isEmpty()||s.registrationStage()!=Registration.Stage.UNKNOWN){
     if(!savedPending.isEmpty()&&savedPending.equals(s.question==null?"":s.question.key)&&(savedPackage.isEmpty()||savedPackage.equals(s.pkg))&&!next.equals("もう一度挑戦する")&&!next.equals("問題を見る")&&s.registrationStage()==Registration.Stage.UNKNOWN)guard.restore(savedPending,now);
     else if(!savedPending.isEmpty()){clearPending();Store.log(this,"画面から再開","","","現在の問題が前回の記録と異なるため現在画面を採用");}
     resumeChecked=true;
    }
   }
   if(!next.isEmpty()){
    session.edit().remove("rich_start_pending").commit();
    String identity=s.pkg+":"+next+":"+(s.question==null?"":s.question.key)+":"+s.latest;
    if(!identity.equals(transitionScreen)){
     if(transitionSteps>=20){stop("要確認","画面遷移の操作上限に達しました");return;}
     AccessibilityNodeInfo b=s.quick(next);
     if(b==null||!click(s,b)){stop("要確認","クイズ専用の進行ボタンを押せません");return;}
     transitionScreen=identity;transitionSteps++;questionSince=now;scrolls=0;finalRevealSteps=0;finalRevealAt=0;
     if(next.equals("もう一度挑戦する")||next.equals("問題を見る")){clearPending();registrationPending="";registrationPriorQuestion="";session.edit().remove("registration_pending").remove("registration_prior_question").commit();guard=new RunGuard();current="";answer="";stable="";stableCount=0;transition="";}
     else if(guard.pending.isEmpty()&&s.question!=null){guard.mark(s.question,now);guard.lastConfirmed=s.question.number-1;}
     status("自動進行",next);return;
    }
    if(now-questionSince>60000){stop("要確認","進行ボタンの反映を確認できません");return;}
    status("画面待機",next+" の反映を待っています");return;
   }
   if(handleRegistration(s,now))return;
   if(QuizFlow.failed(s.model)){stop("要確認","不正解表示。再挑戦ボタンを確認できません");return;}
   Question q=s.question;
   if(q!=null&&!q.key.equals(stable)){stable=q.key;stableCount=1;}else if(q!=null)stableCount++;
   if(!guard.pending.isEmpty()){
    if(q!=null&&!q.key.equals(guard.pending)&&guard.confirmNext(q)){clearPending();Store.log(this,"回答確認",current,answer,"次の問題の表示を確認");questionSince=now;transition="";scrolls=0;}
    else {
     if(now-guard.sentAt>90000){stop("要確認","回答の反映を確認できません。再回答せず停止");return;}
     status("確定確認待ち","同じ問題への再タップを防止しています");return;
    }
   }
   if(s.registrationChoicesVisible()){stop("要確認","登録の選択画面を特定できません。スクロールせず停止します");return;}
   if(q==null&&s.richStart()!=null){
    if(!session.getBoolean("rich_start_pending",false)){
     if(!session.edit().putBoolean("rich_start_pending",true).commit()){stop("要確認","開始記録を保存できません");return;}
     if(!click(s,s.richStart())){stop("要確認","開始ボタンを押せません");return;}lastAction=now;questionSince=now;status("登録開始","リッチメニューの開始を押しました");return;
    }
    stop("要確認","前回の開始操作が未確認です。LINEの画面を確認してください");return;
   }
   if(q==null&&s.uniqueId("oa_richmenu_imageview")!=null){stop("本人操作","開始が画像内だけのため要素を確認できません。LINEの開始を1回押してから▶再生してください");return;}
   if(q==null){if(now-questionSince>60000){stop("要確認","最新問題を取得できません");return;}scroll(s);return;}
   if(current.isEmpty())guard.lastConfirmed=q.number-1;
   current="第"+q.number+"問\n"+q.body;
   if(QuizFlow.correct(s.model)){if(now-questionSince>60000){stop("要確認","次の問題ボタンが表示されません");return;}status("次問待機","正解済みのため次の専用ボタンを待っています");scroll(s);return;}
   if(stableCount<2){status("照合中",q.number+"/5 · 問題文の安定を確認");handler.postDelayed(this::check,1700);return;}
   if(!guard.canAnswer(q)){stop("要確認","問題番号が想定した順序と異なります");return;}
   if(!s.visibleQuestion()){scroll(s);return;}
   try{
    if(q.number<5){answer=bank.match(q);status("回答待機",q.number+"/5 · 答え "+answer);AccessibilityNodeInfo b=s.quick(answer);
     if(b==null){if(now-questionSince>60000){stop("要確認","回答ボタンが表示されません");return;}scroll(s);return;}
     mark(q,now);if(!click(s,b)){stop("要確認","回答タップ失敗（未確認記録を保持）");return;}status("確定確認待ち",q.number+"/5 · "+answer+" を操作しました");
    }else{
     MathEngine.Result r=MathEngine.solve(q.body);answer=r.answer;status("数字入力待機","5/5 · "+answer);
     handleFinal(s,q,now);
    }
   }catch(IllegalArgumentException e){stop("要確認",e.getMessage());}
  }catch(Exception e){stop("要確認","画面確認エラー: "+e.getClass().getSimpleName());}
 }
 private boolean handleRegistration(Screen s,long now){
  Registration.Stage stage=s.registrationStage();
  if(stage!=Registration.Stage.UNKNOWN)session.edit().remove("rich_start_pending").commit();
  if(stage==Registration.Stage.UNKNOWN){
   if(!registrationPending.isEmpty()){
    if(s.question!=null&&s.qRow==s.flex.size()-1&&s.visibleQuestion()&&!s.question.key.equals(registrationPriorQuestion)){
     registrationPending="";session.edit().remove("registration_pending").remove("registration_prior_question").commit();
     Store.log(this,"登録確認","","","問題画面への移行を確認。登録情報そのものは保存しません");registrationSince=0;questionSince=now;current="";
    }else {if(registrationSent==0||now-registrationSent>45000)stop("要確認","登録の次の画面を確認できません。未確認記録を保持");else status("登録確認待ち","次の専用選択肢の表示を待っています");return true;}
   }
   return false;
  }
  if(!guard.pending.isEmpty()){stop("要確認","回答の確定待ちに登録画面を検知しました");return true;}
  if(Registration.personal(stage)){
   current="本人確認 · "+stage.name();answer="";
   stop("本人確認",stage==Registration.Stage.TERMS?"LINEで参加規約を読み、本人が同意してから▶再生してください":"生年・性別は本人がLINEで選択してください。選択後に▶再生で続行します");return true;
  }
  if(registrationSince==0){registrationSince=now;registrationPriorQuestion=s.question==null?"":s.question.key;session.edit().putString("registration_prior_question",registrationPriorQuestion).commit();}
  if(now-registrationSince>300000||registrationSteps>=30){stop("要確認","登録の時間・操作上限に達しました");return true;}
  java.util.Map<String,AccessibilityNodeInfo> controls=s.registrationControls(stage);
  java.util.List<String> labels=new java.util.ArrayList<>(controls.keySet());java.util.Collections.sort(labels);
  String key=stage.name()+":"+jp.kossacktouch.core.Text.norm(labels.toString()).hashCode();
  if(registrationPending.equals(key)){
   if(registrationSent==0||now-registrationSent>45000)stop("要確認","前回の登録選択の反映が未確認です。画面を確認してから未確認記録を解除してください");
   else status("登録確認待ち","同じ登録画面を再操作しません");return true;
  }
  String choice=Registration.choose(stage,labels,registrationRandom);if(choice==null){stop("要確認","登録用の専用選択肢を確認できません");return true;}
  current="初回参加登録 · "+stage.name();answer="";registrationPending=key;registrationSent=now;registrationSteps++;
  if(!session.edit().putString("registration_pending",key).commit()){stop("要確認","登録記録を保存できません");return true;}
  if(!clickRegistration(s,stage,choice)){stop("要確認","登録選択を操作できません。未確認記録を保持");return true;}
  questionSince=now;status("登録中","既存情報の変更を避けて進めています · "+registrationSteps+"/30");return true;
 }
 private boolean clickRegistration(Screen original,Registration.Stage stage,String label){
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null)return false;
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();return false;}
  try(Screen fresh=new Screen(root)){
   if(!running||!fresh.trusted()||!fresh.pkg.equals(original.pkg)||fresh.registrationStage()!=stage)return false;
   AccessibilityNodeInfo target=fresh.registrationControls(stage).get(label);return target!=null&&performChecked(target);
  }
 }
 private boolean performChecked(AccessibilityNodeInfo target){
  if(!running||((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()||!target.refresh()||!target.isVisibleToUser()||!target.isEnabled()||!target.isClickable())return false;
  Rect bounds=new Rect();target.getBoundsInScreen(bounds);if(bounds.width()<=0||bounds.height()<=0)return false;
  if(overlay!=null){int[] xy=new int[2];overlay.getLocationOnScreen(xy);Rect cover=new Rect(xy[0],xy[1],xy[0]+overlay.getWidth(),xy[1]+overlay.getHeight());if(Rect.intersects(bounds,cover)){status("要確認","フロートがボタンに重なっています。ドラッグで移動してください");return false;}}
  lastAction=SystemClock.elapsedRealtime();return target.performAction(AccessibilityNodeInfo.ACTION_CLICK);
 }
 private void mark(Question q,long now){if(!session.edit().putString("pending",q.key).commit())throw new IllegalArgumentException("二重操作防止記録を保存できません");guard.mark(q,now);transitionScreen="";Store.log(this,"回答操作",current,answer,"二重操作防止記録を保存");}
 private void clearPending(){if(session!=null)session.edit().remove("pending").remove("final_prepared").commit();savedPending="";savedPackage="";finalPrepared="";}
 private void handleFinal(Screen original,Question q,long now){
  if(!FinalSubmission.numeric(answer)){stop("要確認","計算結果が数値だけではありません");return;}
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null){stop("要確認","数字入力前の画面を取得できません");return;}
  try(Screen s=new Screen(root)){
   if(!running||!s.trusted()||!s.pkg.equals(runPackage)||!s.currentFinal()||!s.question.key.equals(q.key)){stop("要確認","第5問の画面が変わりました");return;}
   java.util.List<AccessibilityNodeInfo> forms=s.numericInputs(),composers=s.composers();boolean form=s.formEvidence();
   FinalSubmission.Route route=FinalSubmission.route(form,forms.size(),s.submit()!=null,FinalSubmission.messageFormat(s.model),composers.size(),s.composerSend()!=null);
   if(route==FinalSubmission.Route.STOP){stop("要確認","計算結果 "+answer+"。第5問の回答形式を確認できません");return;}
   if(route==FinalSubmission.Route.WAIT){
    if(!form&&composers.isEmpty()&&!composerOpened&&s.keyboardButton()!=null){
     composerOpened=true;if(!click(s,s.keyboardButton())){stop("要確認","LINEの入力欄を表示できません");return;}status("数字入力待機","第5問のLINE入力欄を表示しています");return;
    }
    if(!form&&composerOpened){if(now-questionSince>60000)stop("要確認","LINEの入力欄を要素で確認できません。入力画面のUI全走査が必要です");else status("数字入力待機","LINE入力欄の表示を待っています");return;}
    if(scrolls>=8){stop("要確認","第5問の入力欄・回答ボタンを表示できません");return;}
    if(form){if(finalRevealSteps>=6){stop("要確認","専用入力欄・回答ボタンを上下に探しましたが確認できません");return;}revealFinalButton(s,now);}else scroll(s);return;
   }
   boolean message=route==FinalSubmission.Route.MESSAGE;if(!message&&!s.numericForm()){stop("要確認","専用回答フォームの第5問を確認できません");return;}
   AccessibilityNodeInfo input=message?composers.get(0):forms.get(0);String prepared=q.key+":"+answer;
   if(!FinalSubmission.draftSafe(Screen.text(input),answer,finalPrepared.equals(prepared))){stop("要確認","入力欄に既存の文字があります。書き換えず停止します");return;}
   if(!inputFocused){inputFocused=true;if(input.isClickable()&&!performChecked(input)){stop("要確認","数値欄をタップできません");return;}input.performAction(AccessibilityNodeInfo.ACTION_FOCUS);lastAction=now;status("数字入力待機","数値欄を選択しました");return;}
   if(!finalPrepared.equals(prepared)){
    if(!session.edit().putString("final_prepared",prepared).commit()){stop("要確認","数値入力の記録を保存できません");return;}finalPrepared=prepared;
    Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,answer);
    if(!input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args)){stop("要確認","数字を入力できません");return;}
    lastAction=now;status("数字入力確認","入力結果を確認してから1回だけ回答します");return;
   }
   if(!input.refresh()||!answer.equals(Screen.text(input))){stop("要確認","入力した数値が計算結果と一致しません");return;}
   AccessibilityNodeInfo submit=message?s.composerSend():s.submit();if(submit==null){if(now-questionSince>60000)stop("要確認","送信・回答ボタンを確認できません。入力した数字は残します");else status("数字入力確認","送信・回答ボタンの表示を待っています");return;}
   mark(q,now);if(!click(s,submit)){stop("要確認","回答操作を確認できません。再送せず停止します");return;}
   status("確定確認待ち","5/5 · "+answer+" を"+(message?"LINEで送信":"専用ボタンで回答")+"しました");
  }
 }
 private boolean click(Screen original,AccessibilityNodeInfo old){
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null)return false;
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();return false;}
  try(Screen fresh=new Screen(root)){
   if(!running||!fresh.trusted()||!fresh.pkg.equals(original.pkg))return false;
   if(original.question!=null&&(fresh.question==null||!fresh.question.key.equals(original.question.key)))return false;
   AccessibilityNodeInfo target=null;String resource=Screen.id(old),label=original.content(old).trim();
   if(resource.endsWith(":id/chat_ui_quick_reply_item_root"))target=fresh.quick(label);
   else {for(AccessibilityNodeInfo n:fresh.nodes)if(n.isClickable()&&n.isVisibleToUser()&&n.isEnabled()&&Screen.id(n).equals(resource)&&Screen.text(n).equals(Screen.text(old))){if(target!=null)return false;target=n;}}
   return target!=null&&performChecked(target);
  }
 }
 private void scroll(Screen s){if(scrolls>=8){stop("要確認","スクロール上限に達しました");return;}if(SystemClock.elapsedRealtime()-lastAction<5000)return;scrolls++;
  AccessibilityNodeInfo latest=s.exact("最新のメッセージに移動",":id/chat_ui_scroll_to_bottom_button");if(latest!=null){click(s,latest);return;}
  AccessibilityNodeInfo list=s.scrollable();if(list!=null&&list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)){lastAction=SystemClock.elapsedRealtime();status("スクロール",scrolls+"/8 · 最新問題へ移動");return;}
  // Fallback is a bounded swipe inside LINE's chat area; no coordinate answer taps.
  Rect r=new Rect();for(AccessibilityNodeInfo n:s.nodes)if(Screen.id(n).endsWith(":id/chat_ui_main_content_area")){n.getBoundsInScreen(r);break;}
  if(r.height()>dp(200)){Path p=new Path();p.moveTo(r.centerX(),r.top+r.height()*.75f);p.lineTo(r.centerX(),r.top+r.height()*.25f);lastAction=SystemClock.elapsedRealtime();dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,350)).build(),null,null);status("スクロール",scrolls+"/8 · ボタン表示を待機");}else status("待機","画面要素の更新を待っています");
 }
 private void revealFinalButton(Screen s,long now){
  boolean forward=QuizFlow.finalRevealForward(finalRevealSteps);finalRevealSteps++;finalRevealAt=now;
  AccessibilityNodeInfo list=s.scrollable();boolean moved=list!=null&&list.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
  if(!moved){Rect r=new Rect();for(AccessibilityNodeInfo n:s.nodes)if(Screen.id(n).endsWith(":id/chat_ui_main_content_area")){n.getBoundsInScreen(r);break;}
   if(r.height()>dp(200)){Path p=new Path();float from=forward?0.72f:0.35f,to=forward?0.35f:0.72f;p.moveTo(r.centerX(),r.top+r.height()*from);p.lineTo(r.centerX(),r.top+r.height()*to);dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,300)).build(),null,null);}
  }
  status("最終問題へ移動",finalRevealSteps+"/6 · "+(forward?"上へ移動":"少し下へ戻す")+" · 確認ボタンを探索");
 }
 @Override public void onInterrupt(){stop("要確認","アクセシビリティ処理が中断されました");}
 @Override public void onDestroy(){running=false;handler.removeCallbacksAndMessages(null);hideFloat();instance=null;Store.prefs(this).edit().putString("state","権限未接続").apply();super.onDestroy();}
}
