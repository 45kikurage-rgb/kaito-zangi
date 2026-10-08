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
 private boolean resumeChecked=false;private String savedPending="",savedPackage="",transitionScreen="";private int transitionSteps=0;
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
  savedPending=Store.prefs(this).getString("pending","");savedPackage=Store.prefs(this).getString("pending_package","");resumeChecked=false;transitionSteps=0;transitionScreen="";
  registrationSteps=0;registrationSince=0;registrationSent=0;registrationPending=Store.prefs(this).getString("registration_pending","");registrationPriorQuestion=Store.prefs(this).getString("registration_prior_question","");guard=new RunGuard();stable="";stableCount=0;current="";answer="";scrolls=0;transition="";lastAction=0;lastTick=0;questionSince=SystemClock.elapsedRealtime();running=true;play.setText("■ 停止");status("認識中","現在のLINE画面から開始します");check();
 }
 public void stop(String state,String detail){running=false;if(play!=null)play.setText("▶ 再生");status(state,detail);Store.log(this,state,current,answer,detail);}
 private void status(String state,String detail){Store.prefs(this).edit().putString("state",state).putString("detail",detail).putString("question",current).putString("answer",answer).apply();if(info!=null)info.setText("回答ザンギ · "+state+"\n"+detail);}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){if(!running)return;int type=e.getEventType();String pkg=String.valueOf(e.getPackageName());
  if(type==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED&&!pkg.equals(getPackageName())&&!pkg.contains("inputmethod")&&!pkg.contains("keyboard")&&!pkg.equals("com.android.systemui")){previousPackage=pkg;if(!Store.allowed(this,pkg)){stop("要確認","LINE以外の画面へ移動しました");return;}}
  if(type==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED||type==AccessibilityEvent.TYPE_VIEW_SCROLLED||type==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)handler.post(this::check);
 }
 private void check(){if(!running)return;long now=SystemClock.elapsedRealtime();if(now-lastTick<1500)return;lastTick=now;
  if(((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()){stop("要確認","画面ロック・画面OFFを検知しました");return;}
  if(now-questionSince>240000){stop("要確認","問題の制限時間に近づいたため停止しました");return;}
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null){stop("要確認","画面要素を取得できません");return;}
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();stop("要確認","許可したLINE以外の画面です");return;}
  try(Screen s=new Screen(root)){
   if(!Store.allowed(this,s.pkg)||!s.title||s.overflow){stop("要確認","対象LINE画面を確認できません（権限・トーク・要素数を確認）");return;}
   previousPackage=s.pkg;
   if(now-lastAction<4500)return;
   boolean complete=QuizFlow.complete(s.model);
   if(complete){clearPending();guard.pending="";stop("完了","クイズの完了表示を確認しました");return;}
   String next=QuizFlow.transition(s.model);
   if(!resumeChecked){
    if(s.question!=null||!next.isEmpty()||s.registrationStage()!=Registration.Stage.UNKNOWN){
     if(!savedPending.isEmpty()&&savedPending.equals(s.question==null?"":s.question.key)&&(savedPackage.isEmpty()||savedPackage.equals(s.pkg))&&!next.equals("もう一度挑戦する")&&!next.equals("問題を見る")&&s.registrationStage()==Registration.Stage.UNKNOWN)guard.restore(savedPending,now);
     else if(!savedPending.isEmpty()){clearPending();Store.log(this,"画面から再開","","","現在の問題が前回の記録と異なるため現在画面を採用");}
     resumeChecked=true;
    }
   }
   if(!next.isEmpty()){
    String identity=s.pkg+":"+next+":"+(s.question==null?"":s.question.key)+":"+s.latest;
    if(!identity.equals(transitionScreen)){
     if(transitionSteps>=20){stop("要確認","画面遷移の操作上限に達しました");return;}
     AccessibilityNodeInfo b=s.quick(next);
     if(b==null||!click(s,b)){stop("要確認","クイズ専用の進行ボタンを押せません");return;}
     transitionScreen=identity;transitionSteps++;questionSince=now;scrolls=0;
     if(next.equals("もう一度挑戦する")||next.equals("問題を見る")){clearPending();guard=new RunGuard();current="";answer="";stable="";stableCount=0;transition="";}
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
     java.util.List<AccessibilityNodeInfo> inputs=s.numericInputs();AccessibilityNodeInfo submit=s.submit();
     if(inputs.size()!=1||submit==null){stop("要確認","計算結果 "+answer+"。クイズ専用の数字欄・回答ボタンを確認できません。通常LINE送信は行いません");return;}
     AccessibilityNodeInfo input=inputs.get(0);String full=s.allVisibleText();if(!full.contains("第5問")||!full.contains("回答")){stop("要確認","専用数字画面を確認できません");return;}
     Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,answer);
     if(!input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args)){stop("要確認","数字を入力できません");return;}
     if(!input.refresh()||!answer.equals(Screen.text(input))){stop("要確認","入力結果が一致しません");return;}
     mark(q,now);if(!click(s,submit)){stop("要確認","専用回答ボタンを押せません");return;}
     status("確定確認待ち","5/5 · 計算結果 "+answer+" を確定しました");
    }
   }catch(IllegalArgumentException e){stop("要確認",e.getMessage());}
  }catch(Exception e){stop("要確認","画面確認エラー: "+e.getClass().getSimpleName());}
 }
 private boolean handleRegistration(Screen s,long now){
  Registration.Stage stage=s.registrationStage();
  if(stage==Registration.Stage.UNKNOWN){
   if(!registrationPending.isEmpty()){
    if(s.question!=null&&s.qRow==s.flex.size()-1&&s.visibleQuestion()&&!s.question.key.equals(registrationPriorQuestion)){
     registrationPending="";Store.prefs(this).edit().remove("registration_pending").remove("registration_prior_question").commit();
     Store.log(this,"登録確認","","","問題画面への移行を確認。登録情報そのものは保存しません");registrationSince=0;questionSince=now;current="";
    }else {if(registrationSent==0||now-registrationSent>45000)stop("要確認","登録の次の画面を確認できません。未確認記録を保持");else status("登録確認待ち","次の専用選択肢の表示を待っています");return true;}
   }
   return false;
  }
  if(!guard.pending.isEmpty()){stop("要確認","回答の確定待ちに登録画面を検知しました");return true;}
  if(registrationSince==0){registrationSince=now;registrationPriorQuestion=s.question==null?"":s.question.key;Store.prefs(this).edit().putString("registration_prior_question",registrationPriorQuestion).commit();}
  if(now-registrationSince>300000||registrationSteps>=30){stop("要確認","登録の時間・操作上限に達しました");return true;}
  java.util.Map<String,AccessibilityNodeInfo> controls=s.registrationControls(stage);
  java.util.List<String> labels=new java.util.ArrayList<>(controls.keySet());java.util.Collections.sort(labels);
  String key=stage.name()+":"+labels.toString();
  if(registrationPending.equals(key)){
   if(registrationSent==0||now-registrationSent>45000)stop("要確認","前回の登録選択の反映が未確認です。画面を確認してから未確認記録を解除してください");
   else status("登録確認待ち","同じ登録画面を再操作しません");return true;
  }
  String choice=Registration.choose(stage,labels,registrationRandom);if(choice==null){stop("要確認","登録用の専用選択肢を確認できません");return true;}
  current="初回参加登録 · "+stage.name();answer="";registrationPending=key;registrationSent=now;registrationSteps++;
  Store.prefs(this).edit().putString("registration_pending",key).commit();
  if(!clickRegistration(s,stage,choice)){stop("要確認","登録選択を操作できません。未確認記録を保持");return true;}
  questionSince=now;status("登録中","既存情報の変更を避けて進めています · "+registrationSteps+"/30");return true;
 }
 private boolean clickRegistration(Screen original,Registration.Stage stage,String label){
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null)return false;
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();return false;}
  try(Screen fresh=new Screen(root)){
   if(!fresh.title||fresh.overflow||fresh.registrationStage()!=stage)return false;
   AccessibilityNodeInfo target=fresh.registrationControls(stage).get(label);return target!=null&&performChecked(target);
  }
 }
 private boolean performChecked(AccessibilityNodeInfo target){
  if(((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()||!target.refresh()||!target.isVisibleToUser()||!target.isEnabled()||!target.isClickable())return false;
  Rect bounds=new Rect();target.getBoundsInScreen(bounds);if(bounds.width()<=0||bounds.height()<=0)return false;
  if(overlay!=null){int[] xy=new int[2];overlay.getLocationOnScreen(xy);Rect cover=new Rect(xy[0],xy[1],xy[0]+overlay.getWidth(),xy[1]+overlay.getHeight());if(Rect.intersects(bounds,cover)){status("要確認","フロートがボタンに重なっています。ドラッグで移動してください");return false;}}
  lastAction=SystemClock.elapsedRealtime();return target.performAction(AccessibilityNodeInfo.ACTION_CLICK);
 }
 private void mark(Question q,long now){guard.mark(q,now);transitionScreen="";Store.prefs(this).edit().putString("pending",q.key).putString("pending_package",previousPackage).commit();Store.log(this,"回答操作",current,answer,"二重操作防止記録を保存");}
 private void clearPending(){Store.prefs(this).edit().remove("pending").remove("pending_package").commit();savedPending="";savedPackage="";}
 private boolean click(Screen original,AccessibilityNodeInfo old){
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null)return false;
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();return false;}
  try(Screen fresh=new Screen(root)){
   if(!fresh.title||!fresh.pkg.equals(original.pkg)||fresh.overflow)return false;
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
 @Override public void onInterrupt(){stop("要確認","アクセシビリティ処理が中断されました");}
 @Override public void onDestroy(){running=false;handler.removeCallbacksAndMessages(null);hideFloat();instance=null;Store.prefs(this).edit().putString("state","権限未接続").apply();super.onDestroy();}
}
