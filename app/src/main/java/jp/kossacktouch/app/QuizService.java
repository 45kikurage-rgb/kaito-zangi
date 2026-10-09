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
 private AnswerBank bank;private boolean running=false;private RunGuard guard=new RunGuard();private String current="",answer="",transition="";private long lastTick=0,lastAction=0,questionSince=0;private String previousPackage="";
 private final java.util.Random registrationRandom=new java.util.Random();private int registrationSteps=0;private long registrationSince=0,registrationSent=0;private String registrationPending="",registrationPriorQuestion="";
 private android.content.SharedPreferences session;private String runPackage="",finalPrepared="";private boolean composerOpened=false;
 private boolean resumeChecked=false;private String savedPending="",savedPackage="",transitionScreen="";private int transitionSteps=0;
 private final ElementMonitor elementMonitor=new ElementMonitor();private final RevealSearch revealSearch=new RevealSearch();private boolean elementsStable=false,gesturePending=false,numericGuideConfirmed=false;
 private final Runnable poll=new Runnable(){public void run(){check();handler.postDelayed(this,ElementMonitor.INTERVAL_MS);}};
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
  elementMonitor.reset();revealSearch.reset();gesturePending=false;elementsStable=false;numericGuideConfirmed=false;
  session=null;runPackage="";finalPrepared="";composerOpened=false;savedPending="";savedPackage="";resumeChecked=false;transitionSteps=0;transitionScreen="";
  registrationSteps=0;registrationSince=0;registrationSent=0;registrationPending="";registrationPriorQuestion="";guard=new RunGuard();current="";answer="";transition="";lastAction=0;lastTick=0;questionSince=SystemClock.elapsedRealtime();running=true;play.setText("■ 停止");status("認識中","現在のLINE画面から開始します");check();
 }
 public void stop(String state,String detail){running=false;elementMonitor.reset();revealSearch.reset();if(play!=null)play.setText("▶ 再生");status(state,detail);Store.log(this,state,current,answer,detail);}
 private void status(String state,String detail){Store.prefs(this).edit().putString("state",state).putString("detail",detail).putString("question",current).putString("answer",answer).apply();if(info!=null)info.setText("回答ザンギ · "+state+"\n"+detail);}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){if(!running)return;int type=e.getEventType();String pkg=String.valueOf(e.getPackageName());
  if(type==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED&&!pkg.equals(getPackageName())&&!pkg.contains("inputmethod")&&!pkg.contains("keyboard")&&!pkg.equals("com.android.systemui")){if(!runPackage.isEmpty()&&Store.allowed(this,pkg)&&!runPackage.equals(pkg)){stop("停止","LINEを切り替えました。対象トークで▶再生してください");return;}previousPackage=pkg;if(!Store.allowed(this,pkg)){stop("要確認","LINE以外の画面へ移動しました");return;}}
  if(type==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED||type==AccessibilityEvent.TYPE_VIEW_SCROLLED||type==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)handler.post(this::check);
 }
 private void check(){if(!running)return;long now=SystemClock.elapsedRealtime();if(now-lastTick<ElementMonitor.INTERVAL_MS)return;lastTick=now;
  if(((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()){stop("要確認","画面ロック・画面OFFを検知しました");return;}
  if(now-questionSince>240000){stop("要確認","問題の制限時間に近づいたため停止しました");return;}
  if(gesturePending)return;
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null){stop("要確認","画面要素を取得できません");return;}
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();stop("要確認","対応するLINEの画面ではありません");return;}
  try(Screen s=new Screen(root)){
   if(!s.trusted()){stop("要確認","対象LINE画面を確認できません（権限・トーク・要素数を確認）");return;}
   if(runPackage.isEmpty()){
    runPackage=s.pkg;Store.migrateSession(this,s.pkg);session=Store.session(this,s.pkg);
    savedPending=session.getString("pending","");savedPackage=s.pkg;registrationPending=session.getString("registration_pending","");registrationPriorQuestion=session.getString("registration_prior_question","");
    finalPrepared=session.getString("final_prepared","");Store.prefs(this).edit().putString("active_line_package",s.pkg).apply();
   }else if(!runPackage.equals(s.pkg)){stop("停止","LINEを切り替えました。対象トークで▶再生してください");return;}
   previousPackage=s.pkg;elementsStable=elementMonitor.observe(s.confirmationKey(),now);
   if(elementsStable&&(QuizFlow.finalGuide(s.model)||s.currentFinal())&&FinalSubmission.numericGuide(s.allVisibleText()))numericGuideConfirmed=true;
   boolean complete=QuizFlow.complete(s.model);
   if(complete){if(!elementsStable){status("照合中","完了表示を2回確認しています");return;}clearPending();guard.pending="";stop("完了","クイズの完了表示を確認しました");return;}
   String next=QuizFlow.transition(s.model);
   if(next.isEmpty()&&QuizFlow.finalGuide(s.model)){
    scroll(s);
    return;
   }
   if(now-lastAction<ElementMonitor.INTERVAL_MS)return;
   if(!resumeChecked){
    if(s.question!=null||!next.isEmpty()||s.registrationStage()!=Registration.Stage.UNKNOWN){
     if(!savedPending.isEmpty()&&savedPending.equals(s.question==null?"":s.question.key)&&(savedPackage.isEmpty()||savedPackage.equals(s.pkg))&&!next.equals("もう一度挑戦する")&&!next.equals("問題を見る")&&s.registrationStage()==Registration.Stage.UNKNOWN)guard.restore(savedPending,now);
     else if(!savedPending.isEmpty()){clearPending();Store.log(this,"画面から再開","","","現在の問題が前回の記録と異なるため現在画面を採用");}
     resumeChecked=true;
    }
   }
   if(!next.isEmpty()){
    if(!elementsStable){status("照合中","進行ボタンを2回確認しています");return;}
    session.edit().remove("rich_start_pending").commit();
    String identity=s.pkg+":"+next+":"+(s.question==null?"":s.question.key)+":"+s.latest;
    if(!identity.equals(transitionScreen)){
     if(transitionSteps>=20){stop("要確認","画面遷移の操作上限に達しました");return;}
     AccessibilityNodeInfo b=s.quick(next);
     if(b==null||!click(s,b)){stop("要確認","クイズ専用の進行ボタンを押せません");return;}
     transitionScreen=identity;transitionSteps++;questionSince=now;
     if(next.equals("もう一度挑戦する")||next.equals("問題を見る")){numericGuideConfirmed=false;clearPending();registrationPending="";registrationPriorQuestion="";session.edit().remove("registration_pending").remove("registration_prior_question").commit();guard=new RunGuard();current="";answer="";transition="";}
     else if(guard.pending.isEmpty()&&s.question!=null){guard.mark(s.question,now);guard.lastConfirmed=s.question.number-1;}
     status("自動進行",next);return;
    }
    if(now-questionSince>60000){stop("要確認","進行ボタンの反映を確認できません");return;}
    status("画面待機",next+" の反映を待っています");return;
   }
   if(handleRegistration(s,now))return;
   if(QuizFlow.failed(s.model)){stop("要確認","不正解表示。再挑戦ボタンを確認できません");return;}
   Question q=s.question;
   if(!guard.pending.isEmpty()){
    if(q!=null&&!q.key.equals(guard.pending)&&guard.confirmNext(q)){clearPending();Store.log(this,"回答確認",current,answer,"次の問題の表示を確認");questionSince=now;transition="";}
    else {
     if(now-guard.sentAt>90000){stop("要確認","回答の反映を確認できません。再回答せず停止");return;}
     status("確定確認待ち","同じ問題への再タップを防止しています");return;
    }
   }
   if(s.registrationChoicesVisible()){stop("要確認","登録の選択画面を特定できません。スクロールせず停止します");return;}
   if(q==null&&s.richStart()!=null){
    if(!elementsStable){status("照合中","開始ボタンを2回確認しています");return;}
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
   if(!guard.canAnswer(q)){stop("要確認","問題番号が想定した順序と異なります");return;}
   if(!s.visibleQuestion()){scroll(s);return;}
   try{
    if(q.number<5){answer=bank.match(q);status("回答待機",q.number+"/5 · 答え "+answer);AccessibilityNodeInfo b=s.quick(answer);
     if(b==null){if(now-questionSince>60000){stop("要確認","回答ボタンが表示されません");return;}scroll(s);return;}
     if(!elementsStable){status("照合中",q.number+"/5 · 問題文と回答ボタンを2回確認しています");return;}
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
  if(!elementsStable){status("照合中","登録の表示候補を2回確認しています");return true;}
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
   if(!running||!fresh.trusted()||!fresh.pkg.equals(original.pkg)||fresh.registrationStage()!=stage||!fresh.confirmationKey().equals(original.confirmationKey()))return false;
   AccessibilityNodeInfo target=fresh.registrationControls(stage).get(label);return target!=null&&performChecked(target);
  }
 }
 private boolean performChecked(AccessibilityNodeInfo target){
  if(!running||((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()||!target.refresh()||!target.isVisibleToUser()||!target.isEnabled()||!target.isClickable())return false;
  Rect bounds=new Rect();target.getBoundsInScreen(bounds);if(bounds.width()<=0||bounds.height()<=0)return false;
  if(overlay!=null){int[] xy=new int[2];overlay.getLocationOnScreen(xy);Rect cover=new Rect(xy[0],xy[1],xy[0]+overlay.getWidth(),xy[1]+overlay.getHeight());if(Rect.intersects(bounds,cover)){status("要確認","フロートがボタンに重なっています。ドラッグで移動してください");return false;}}
  lastAction=SystemClock.elapsedRealtime();boolean clicked=target.performAction(AccessibilityNodeInfo.ACTION_CLICK);if(clicked){elementMonitor.reset();revealSearch.reset();}return clicked;
 }
 private void mark(Question q,long now){if(!session.edit().putString("pending",q.key).commit())throw new IllegalArgumentException("二重操作防止記録を保存できません");guard.mark(q,now);transitionScreen="";Store.log(this,"回答操作",current,answer,"二重操作防止記録を保存");}
 private void clearPending(){if(session!=null)session.edit().remove("pending").remove("final_prepared").commit();savedPending="";savedPackage="";finalPrepared="";}
 private void handleFinal(Screen original,Question q,long now){
  if(!FinalSubmission.numeric(answer)){stop("要確認","計算結果が数値だけではありません");return;}
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null){stop("要確認","数字入力前の画面を取得できません");return;}
  try(Screen s=new Screen(root)){
   if(!running||!s.trusted()||!s.pkg.equals(runPackage)||!s.currentFinal()||!s.question.key.equals(q.key)||!s.confirmationKey().equals(original.confirmationKey())){elementMonitor.reset();status("照合中","第5問の画面が変わったため再確認します");return;}
   java.util.List<AccessibilityNodeInfo> forms=s.numericInputs(),composers=s.composers();boolean form=s.formEvidence();
   boolean messageFormat=FinalSubmission.messageFormat(s.model)||numericGuideConfirmed;
   if(!form&&!messageFormat){status("回答方法探索","計算結果 "+answer+"。数字入力の案内を再確認します");scroll(s);return;}
   FinalSubmission.Route route=FinalSubmission.route(form,forms.size(),s.submit()!=null,messageFormat,composers.size(),s.composerSend()!=null);
   if(route==FinalSubmission.Route.STOP){stop("要確認","計算結果 "+answer+"。第5問の回答形式を確認できません");return;}
   if(route==FinalSubmission.Route.WAIT){
    if(!form&&composers.isEmpty()&&!composerOpened&&s.keyboardButton()!=null){
     if(!elementsStable){status("照合中","キーボードボタンを2回確認しています");return;}
     composerOpened=true;if(!click(s,s.keyboardButton())){stop("要確認","LINEの入力欄を表示できません");return;}status("数字入力待機","第5問のLINE入力欄を表示しています");return;
    }
    if(!form&&composerOpened){if(now-questionSince>60000)stop("要確認","LINEの入力欄を要素で確認できません。入力画面のUI全走査が必要です");else scroll(s);return;}
    scroll(s);return;
   }
   boolean message=route==FinalSubmission.Route.MESSAGE;if(!message&&!s.numericForm()){stop("要確認","専用回答フォームの第5問を確認できません");return;}
   if(!elementsStable){status("照合中","第5問の入力・送信要素を2回確認しています");return;}
   AccessibilityNodeInfo input=message?composers.get(0):forms.get(0);String prepared=q.key+":"+answer;
   if(!FinalSubmission.draftSafe(Screen.text(input),answer,finalPrepared.equals(prepared))){stop("要確認","入力欄に既存の文字があります。書き換えず停止します");return;}
   if(!finalPrepared.equals(prepared)){
    if(!input.refresh()||!input.isVisibleToUser()||!input.isEnabled()||!input.isEditable()||!FinalSubmission.draftSafe(Screen.text(input),answer,false)){stop("要確認","入力欄が変化したため直接入力せず停止します");return;}
    if(!session.edit().putString("final_prepared",prepared).commit()){stop("要確認","数値入力の記録を保存できません");return;}finalPrepared=prepared;
    Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,answer);
    if(!input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args)){stop("要確認","数字の直接入力に対応していません。入力欄のUI全走査が必要です");return;}
    elementMonitor.reset();revealSearch.reset();lastAction=now;status("数字入力確認","入力結果を確認してから1回だけ回答します");return;
   }
   if(!input.refresh()||!answer.equals(Screen.text(input))){stop("要確認","入力した数値が計算結果と一致しません");return;}
   AccessibilityNodeInfo submit=message?s.composerSend():s.submit();if(submit==null){if(now-questionSince>60000)stop("要確認","送信・回答ボタンを確認できません。入力した数字は残します");else scroll(s);return;}
   mark(q,now);if(!click(s,submit)){stop("要確認","回答操作を確認できません。再送せず停止します");return;}
   status("確定確認待ち","5/5 · "+answer+" を"+(message?"LINEで送信":"専用ボタンで回答")+"しました");
  }
 }
 private boolean click(Screen original,AccessibilityNodeInfo old){
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null)return false;
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();return false;}
  try(Screen fresh=new Screen(root)){
   if(!running||!fresh.trusted()||!fresh.pkg.equals(original.pkg)||!fresh.confirmationKey().equals(original.confirmationKey()))return false;
   if(original.question!=null&&(fresh.question==null||!fresh.question.key.equals(original.question.key)))return false;
   AccessibilityNodeInfo target=null;String resource=Screen.id(old),label=original.content(old).trim();
   if(resource.endsWith(":id/chat_ui_quick_reply_item_root"))target=fresh.quick(label);
   else {for(AccessibilityNodeInfo n:fresh.nodes)if(n.isClickable()&&n.isVisibleToUser()&&n.isEnabled()&&Screen.id(n).equals(resource)&&Screen.text(n).equals(Screen.text(old))){if(target!=null)return false;target=n;}}
   return target!=null&&performChecked(target);
  }
 }
 private void scroll(Screen s){
  // Registration and untrusted screens never authorize coordinate gestures.
  if(!running||!s.trusted()||s.registrationStage()!=Registration.Stage.UNKNOWN||s.registrationChoicesVisible()){stop("要確認","登録画面または対象外の画面では探索しません");return;}
  if(gesturePending||SystemClock.elapsedRealtime()-lastAction<ElementMonitor.INTERVAL_MS)return;
  RevealSearch.Step step=revealSearch.missing(SystemClock.elapsedRealtime());
  if(step==RevealSearch.Step.WAIT){status("表示待機","必要な要素が見つからないため、1秒後に再確認します");return;}
  if(step==RevealSearch.Step.EXHAUSTED){stop("要確認","上半画面・下2倍の探索を3回行いましたが要素を確認できません");return;}
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null){stop("要確認","スライド直前の画面を取得できません");return;}
  try(Screen fresh=new Screen(root)){
   if(!fresh.trusted()||!fresh.pkg.equals(runPackage)||!fresh.confirmationKey().equals(s.confirmationKey())||fresh.registrationStage()!=Registration.Stage.UNKNOWN||fresh.registrationChoicesVisible()){
    elementMonitor.reset();revealSearch.found();status("照合中","スライド前に画面が変わったため再確認します");return;
   }
   if(((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()){stop("要確認","画面ロック・画面OFFを検知しました");return;}
   Rect r=new Rect();AccessibilityNodeInfo area=fresh.uniqueId("chat_ui_main_content_area");
   if(area==null){stop("要確認","LINEのスライド領域を一意に確認できません");return;}area.getBoundsInScreen(r);
   android.util.DisplayMetrics metrics=getResources().getDisplayMetrics();if(!r.intersect(0,0,metrics.widthPixels,metrics.heightPixels)||r.height()<dp(200)||r.width()<dp(100)){stop("要確認","安全なスライド領域を確認できません");return;}
   float x=r.centerX();
   if(overlay!=null){int[] xy=new int[2];overlay.getLocationOnScreen(xy);Rect cover=new Rect(xy[0],xy[1],xy[0]+overlay.getWidth(),xy[1]+overlay.getHeight());
    if(cover.left<=x&&cover.right>=x){float right=r.right-dp(20),left=r.left+dp(20);if(right>cover.right)x=right;else if(left<cover.left)x=left;else {stop("要確認","フロートがスライド領域を覆っています。移動してください");return;}}
   }
   float[] y=RevealSearch.path(r.top,r.bottom,step);Path path=new Path();path.moveTo(x,y[0]);path.lineTo(x,y[1]);
   final int pair=revealSearch.pair();gesturePending=true;lastAction=SystemClock.elapsedRealtime();elementMonitor.reset();
   boolean accepted=dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,0,RevealSearch.duration(step))).build(),new GestureResultCallback(){
    @Override public void onCompleted(GestureDescription g){gesturePending=false;elementMonitor.reset();if(running)status("表示確認",pair+"/3 · スライド後の要素を確認しています");}
    @Override public void onCancelled(GestureDescription g){gesturePending=false;if(running)stop("要確認","スライドが中断されました");}
   },handler);
   if(!accepted){gesturePending=false;stop("要確認","スライドを実行できません");return;}
   revealSearch.moved();status("要素探索",pair+"/3 · "+(step==RevealSearch.Step.UP?"半画面を上へ":"2倍の距離を速く下へ"));
  }
 }
 @Override public void onInterrupt(){stop("要確認","アクセシビリティ処理が中断されました");}
 @Override public void onDestroy(){running=false;handler.removeCallbacksAndMessages(null);hideFloat();instance=null;Store.prefs(this).edit().putString("state","権限未接続").apply();super.onDestroy();}
}
