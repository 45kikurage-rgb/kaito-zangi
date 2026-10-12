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
 private final Handler handler=new Handler(Looper.getMainLooper());private WindowManager wm;private LinearLayout overlay;private TextView info;private TextView titleView;private Button play;private WindowManager.LayoutParams layout;private int compactX,compactY;
 private AnswerBank bank;private boolean running=false;private RunGuard guard=new RunGuard();private String current="",answer="",transition="";private long lastTick=0,lastAction=0,questionSince=0;private String previousPackage="";
 private final java.util.Random registrationRandom=new java.util.Random();private int registrationSteps=0;private long registrationSince=0,registrationSent=0;private String registrationPending="",registrationPriorQuestion="";
 private android.content.SharedPreferences session;private String runPackage="",finalPrepared="",couponStage="";private long couponSince=0,finalSince=0;
 private boolean resumeChecked=false;private String savedPending="",savedPackage="",transitionScreen="";private int transitionSteps=0;
 private final ManualFinal manualFinal=new ManualFinal();private final FinalAnchor finalAnchor=new FinalAnchor();private final ElementMonitor elementMonitor=new ElementMonitor();private final RevealSearch revealSearch=new RevealSearch();private boolean elementsStable=false,gesturePending=false;
 private final Runnable poll=new Runnable(){public void run(){check();handler.postDelayed(this,ElementMonitor.INTERVAL_MS);}};
 @Override protected void onServiceConnected(){instance=this;try{bank=Store.bank(this);}catch(Exception e){status("要確認","正答データを読み込めません");}wm=(WindowManager)getSystemService(WINDOW_SERVICE);handler.post(poll);}
 public void showFloat(){if(overlay!=null)return;
  overlay=new LinearLayout(this);overlay.setOrientation(LinearLayout.VERTICAL);overlay.setBackgroundColor(Color.rgb(17,40,51));
  LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.HORIZONTAL);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(8),0,dp(4),0);overlay.addView(header,new LinearLayout.LayoutParams(-1,dp(56)));
  titleView=new TextView(this);titleView.setTextColor(Color.WHITE);titleView.setTextSize(15);titleView.setTypeface(null,android.graphics.Typeface.BOLD);titleView.setText("怪盗ザンギ");titleView.setGravity(Gravity.CENTER_VERTICAL);titleView.setPadding(dp(5),0,dp(5),0);
  header.addView(titleView,new LinearLayout.LayoutParams(0,dp(56),1));
  play=new Button(this);play.setText("▶");play.setAllCaps(false);play.setTextSize(20);play.setMinHeight(0);play.setMinimumHeight(0);play.setMinWidth(0);play.setMinimumWidth(0);play.setPadding(0,0,0,0);play.setContentDescription("再生・停止");play.setOnClickListener(v->{if(running)stop("停止","利用者が停止しました");else startRun();});header.addView(play,new LinearLayout.LayoutParams(dp(56),dp(52)));
  Button close=new Button(this);close.setText("×");close.setAllCaps(false);close.setTextSize(22);close.setMinHeight(0);close.setMinimumHeight(0);close.setMinWidth(0);close.setMinimumWidth(0);close.setPadding(0,0,0,0);close.setContentDescription("フロートを閉じる");close.setOnClickListener(v->{stop("停止","フロートを閉じました");hideFloat();});header.addView(close,new LinearLayout.LayoutParams(dp(56),dp(52)));
  info=new TextView(this);info.setTextColor(Color.WHITE);info.setTextSize(14);info.setPadding(dp(12),dp(6),dp(12),dp(12));info.setGravity(Gravity.TOP);info.setVisibility(View.GONE);overlay.addView(info,new LinearLayout.LayoutParams(-1,dp(112)));
  android.content.SharedPreferences prefs=Store.prefs(this);compactX=prefs.getInt("float_x",dp(12));compactY=prefs.getInt("float_y",0);
  layout=new WindowManager.LayoutParams(dp(258),dp(56),WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);
  layout.gravity=Gravity.TOP|Gravity.LEFT;
  if(android.os.Build.VERSION.SDK_INT>=28)layout.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
  layout.x=compactX;layout.y=compactY;clampFloat(false);layout.x=compactX;layout.y=compactY;
  titleView.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy;boolean dragging;
   public boolean onTouch(View v,MotionEvent e){switch(e.getActionMasked()){
    case MotionEvent.ACTION_DOWN:sx=e.getRawX();sy=e.getRawY();ox=layout.x;oy=layout.y;dragging=false;return true;
    case MotionEvent.ACTION_MOVE:
     if(Math.abs(e.getRawX()-sx)+Math.abs(e.getRawY()-sy)>dp(5))dragging=true;
     layout.x=ox+(int)(e.getRawX()-sx);layout.y=oy+(int)(e.getRawY()-sy);
     clampFloat(running);if(overlay!=null)wm.updateViewLayout(overlay,layout);return true;
    case MotionEvent.ACTION_UP:
     if(dragging){compactY=layout.y;compactX=running?Store.prefs(QuizService.this).getInt("float_x",dp(12)):layout.x;
      Store.prefs(QuizService.this).edit().putInt("float_x",compactX).putInt("float_y",compactY).apply();}
     return true;
    default:return true;
   }}
  });
  try{wm.addView(overlay,layout);}catch(Exception e){overlay=null;stop("要確認","フロートを表示できません");}
 }
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private void clampFloat(boolean expanded){
  android.util.DisplayMetrics dm=getResources().getDisplayMetrics();
  int w=expanded?dm.widthPixels:dp(258),h=expanded?dp(168):dp(56);
  int maxX=Math.max(0,dm.widthPixels-w),maxY=Math.max(0,dm.heightPixels-h);
  layout.x=Math.max(0,Math.min(layout.x,maxX));layout.y=Math.max(0,Math.min(layout.y,maxY));
  if(!expanded){compactX=Math.max(0,Math.min(compactX,maxX));compactY=Math.max(0,Math.min(compactY,maxY));}
 }
 private void floatSize(boolean expanded){
  if(overlay==null||layout==null)return;
  info.setVisibility(expanded?View.VISIBLE:View.GONE);
  layout.width=expanded?WindowManager.LayoutParams.MATCH_PARENT:dp(258);
  layout.height=expanded?dp(168):dp(56);
  if(expanded){compactX=layout.x;compactY=layout.y;layout.x=0;}else{layout.x=compactX;layout.y=compactY;}
  clampFloat(expanded);wm.updateViewLayout(overlay,layout);
 }
 private void hideFloat(){if(overlay!=null){wm.removeView(overlay);overlay=null;info=null;titleView=null;play=null;}}
 private void startRun(){if(bank==null){stop("要確認","正答データ不備");return;}
  manualFinal.reset();finalAnchor.reset();elementMonitor.reset();revealSearch.reset();gesturePending=false;elementsStable=false;
  session=null;runPackage="";finalPrepared="";couponStage="";couponSince=0;finalSince=0;savedPending="";savedPackage="";resumeChecked=false;transitionSteps=0;transitionScreen="";
  registrationSteps=0;registrationSince=0;registrationSent=0;registrationPending="";registrationPriorQuestion="";guard=new RunGuard();current="";answer="";transition="";lastAction=0;lastTick=0;questionSince=SystemClock.elapsedRealtime();running=true;play.setText("■");floatSize(true);status("認識中","現在のLINE画面から開始します");check();
 }
 public void stop(String state,String detail){running=false;elementMonitor.reset();revealSearch.reset();if(play!=null)play.setText("▶");floatSize(false);status(state,detail);Store.log(this,state,current,answer,detail);}
 private void status(String state,String detail){Store.prefs(this).edit().putString("state",state).putString("detail",detail).putString("question",current).putString("answer",answer).apply();if(info!=null)info.setText(state+"\n"+detail);}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){if(!running)return;int type=e.getEventType();
  // Event package identifies its source, not the foreground application (e.g. an IME).
  if(type==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED||type==AccessibilityEvent.TYPE_VIEW_SCROLLED||type==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED||type==AccessibilityEvent.TYPE_WINDOWS_CHANGED||type==AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED)handler.post(this::check);
 }
 private AccessibilityNodeInfo foregroundLineRoot(){
  java.util.List<AccessibilityWindowInfo> windows=new java.util.ArrayList<>();
  java.util.List<ForegroundWindow.Window> descriptions=new java.util.ArrayList<>();java.util.List<AccessibilityNodeInfo> roots=new java.util.ArrayList<>();
  int selected=-1;
  try{
   windows=getWindows();
   for(AccessibilityWindowInfo w:windows){AccessibilityNodeInfo root=w.getRoot();roots.add(root);descriptions.add(new ForegroundWindow.Window(w.getType(),w.getLayer(),w.isActive(),w.isFocused(),root==null?"":String.valueOf(root.getPackageName())));}
   selected=ForegroundWindow.select(descriptions);return selected<0?null:roots.get(selected);
  }catch(RuntimeException ex){selected=-1;return null;}finally{for(int i=0;i<roots.size();i++)if(i!=selected&&roots.get(i)!=null)roots.get(i).recycle();for(AccessibilityWindowInfo w:windows)w.recycle();}
 }
 private void check(){if(!running)return;long now=SystemClock.elapsedRealtime();if(now-lastTick<ElementMonitor.INTERVAL_MS)return;lastTick=now;
  if(((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()){stop("要確認","画面ロック・画面OFFを検知しました");return;}
  if(now-questionSince>240000){stop("要確認","問題の制限時間に近づいたため停止しました");return;}
  if(gesturePending)return;
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null){stop("要確認","前面のLINE画面を確認できません（別アプリ・ダイアログ・権限）");return;}
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();stop("要確認","対応するLINEの画面ではありません");return;}
  try(Screen s=new Screen(root)){
   if(!s.trusted()){
    if("display_clicked".equals(couponStage)&&s.verifiedCouponPage()){clearPending();session.edit().remove("coupon_stage").commit();stop("完了","クーポンページを確認しました");return;}
    stop("要確認","対象LINE画面を確認できません。クーポンを開いている場合は表示を目視確認してください");return;
   }
   if(runPackage.isEmpty()){
    runPackage=s.pkg;Store.migrateSession(this,s.pkg);session=Store.session(this,s.pkg);
    savedPending=session.getString("pending","");savedPackage=s.pkg;registrationPending=session.getString("registration_pending","");registrationPriorQuestion=session.getString("registration_prior_question","");
    finalPrepared=session.getString("final_prepared","");couponStage=session.getString("coupon_stage","");Store.prefs(this).edit().putString("active_line_package",s.pkg).apply();
   }else if(!runPackage.equals(s.pkg)){stop("停止","LINEを切り替えました。対象トークで▶再生してください");return;}
   previousPackage=s.pkg;elementsStable=elementMonitor.observe(s.confirmationKey(),now);
   if(finalAnchor.mismatch(s.question)){
    if(manualFinal.started()){stop("要確認","第5問 · 答え: "+answer+"\n現在の問題を確認できないため停止。入力・送信は行いません");return;}
    if(!elementsStable){status("照合中","探索で別の問題へ移ったため2回確認しています");return;}
    AccessibilityNodeInfo bottom=s.uniqueId("chat_ui_scroll_to_bottom_button");
    if(bottom==null||!finalAnchor.recoverOnce()){stop("要確認","探索で第5問の現在位置を失いました。過去の問題は操作しません。最新メッセージへ戻して▶再生してください");return;}
    if(!click(s,bottom,false)){stop("要確認","最新メッセージへ戻れません。過去の問題は操作しません");return;}
    status("現在位置復帰","過去の問題を操作せず、最新メッセージへ戻しています");return;
   }
   if(elementsStable&&s.currentFinal()&&s.visibleQuestion())finalAnchor.bind(s.question);
   if(!finalPrepared.isEmpty()&&!s.currentFinal()&&s.question!=null&&s.question.number==5&&!QuizFlow.complete(s.model)){stop("要確認","第5問の入力途中に画面が変わったため停止しました");return;}
   boolean complete=QuizFlow.complete(s.model);
   if(complete||!couponStage.isEmpty()||s.couponButton("クーポンをゲットする")!=null||s.couponButton("クーポンを表示する")!=null){
    if(handleCoupon(s,now,complete))return;
   }
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
     if(next.equals("もう一度挑戦する")||next.equals("問題を見る")){manualFinal.reset();finalAnchor.reset();clearPending();registrationPending="";registrationPriorQuestion="";session.edit().remove("registration_pending").remove("registration_prior_question").commit();guard=new RunGuard();current="";answer="";transition="";}
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
   if(s.registrationChoicesVisible()){stop("要確認","登録画面の選択肢が未確認です。画面を確認してください");return;}
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
   if(!s.visibleQuestion()&&!(q.number==5&&finalPrepared.equals(q.key))){scroll(s);return;}
   try{
    if(q.number<5){answer=bank.match(q);status("回答待機",q.number+"/5 · 答え "+answer);AccessibilityNodeInfo b=s.quick(answer);
     if(b==null){if(now-questionSince>60000){stop("要確認","回答ボタンが表示されません");return;}scroll(s);return;}
     if(!elementsStable){status("照合中",q.number+"/5 · 問題文と回答ボタンを2回確認しています");return;}
     mark(q,now);if(!click(s,b)){stop("要確認","回答タップ失敗（未確認記録を保持）");return;}status("確定確認待ち",q.number+"/5 · "+answer+" を操作しました");
    }else{
     MathEngine.Result r=MathEngine.solve(q.body);answer=r.answer;
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
  if(!Registration.canProceed(stage,Store.prefs(this).getBoolean("registration_preapproved_v1",false))){
   current="本人確認 · "+stage.name();answer="";
   stop("本人確認",stage==Registration.Stage.TERMS?"事前承認はOFFです。LINEで参加規約を確認して同意するか、アプリで事前承認を設定してください":"事前承認はOFFです。LINEで本人情報を選択するか、アプリで初回登録の事前承認を設定してください");return true;
  }
  if(registrationSince==0){registrationSince=now;registrationPriorQuestion=s.question==null?"":s.question.key;session.edit().putString("registration_prior_question",registrationPriorQuestion).commit();}
  if(now-registrationSince>300000||registrationSteps>=30){stop("要確認","登録の時間・操作上限に達しました");return true;}
  java.util.Map<String,AccessibilityNodeInfo> controls=s.registrationControls(stage);
  if(controls.isEmpty()){
   if(Registration.stage(s.latest)==Registration.Stage.UNKNOWN){stop("要確認","登録画面の選択肢を確認できません。誤った画面ではスライドしません");return true;}
   scroll(s,true);return true;
  }
  revealSearch.found();
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
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null)return false;
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();return false;}
  try(Screen fresh=new Screen(root)){
   if(!running||!fresh.trusted()||!fresh.pkg.equals(original.pkg)||fresh.registrationStage()!=stage||!fresh.confirmationKey().equals(original.confirmationKey()))return false;
   AccessibilityNodeInfo target=fresh.registrationControls(stage).get(label);return target!=null&&performChecked(target);
  }
 }
 private boolean performChecked(AccessibilityNodeInfo target){return performChecked(target,true);}
 private boolean performChecked(AccessibilityNodeInfo target,boolean resetSearch){
  if(!running||((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()||!target.refresh()||!target.isVisibleToUser()||!target.isEnabled()||!target.isClickable())return false;
  Rect bounds=new Rect();target.getBoundsInScreen(bounds);if(bounds.width()<=0||bounds.height()<=0)return false;
  if(overlay!=null){int[] xy=new int[2];overlay.getLocationOnScreen(xy);Rect cover=new Rect(xy[0],xy[1],xy[0]+overlay.getWidth(),xy[1]+overlay.getHeight());if(Rect.intersects(bounds,cover)){status("要確認","フロートがボタンに重なっています。ドラッグで移動してください");return false;}}
  lastAction=SystemClock.elapsedRealtime();boolean clicked=target.performAction(AccessibilityNodeInfo.ACTION_CLICK);if(clicked){elementMonitor.reset();if(resetSearch)revealSearch.reset();}return clicked;
 }
 private void mark(Question q,long now){if(!session.edit().putString("pending",q.key).commit())throw new IllegalArgumentException("二重操作防止記録を保存できません");guard.mark(q,now);transitionScreen="";Store.log(this,"回答操作",current,answer,"二重操作防止記録を保存");}
 private void clearPending(){if(session!=null)session.edit().remove("pending").remove("final_prepared").commit();savedPending="";savedPackage="";finalPrepared="";}
 private boolean keyboardVisible(){
  java.util.List<AccessibilityWindowInfo> windows=getWindows();
  try{for(AccessibilityWindowInfo w:windows)if(w.getType()==AccessibilityWindowInfo.TYPE_INPUT_METHOD){Rect bounds=new Rect();w.getBoundsInScreen(bounds);if(!bounds.isEmpty())return true;}return false;}
  finally{for(AccessibilityWindowInfo w:windows)w.recycle();}
 }
 private void handleFinal(Screen original,Question q,long now){
  if(!FinalSubmission.numeric(answer)){stop("要確認","計算結果が数値だけではありません");return;}
  if(!FinalSubmission.messageFormat(original.model)){
   stop("要確認","第5問の数字回答手順を確認できません。誤送信防止のため手動確認してください");return;
  }
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null){stop("要確認","第5問のLINE入力画面を確認できません");return;}
  try(Screen s=new Screen(root)){
   if(!running||!s.trusted()||!s.pkg.equals(runPackage)||!s.currentFinal()||s.question==null||!s.question.key.equals(q.key)||!s.confirmationKey().equals(original.confirmationKey())){
    elementMonitor.reset();status("照合中","第5問の画面を再確認します");return;
   }
   java.util.List<AccessibilityNodeInfo> inputs=s.composers();
   if(inputs.size()!=1){
    AccessibilityNodeInfo opener=s.keyboardButton();
    if(opener!=null&&finalSince==0&&elementsStable){finalSince=now;if(!click(s,opener))stop("要確認","数字入力欄を開けません");else status("入力欄待機","LINEの数字入力欄を開いています");return;}
    if(finalSince==0)finalSince=now;
    if(now-finalSince>12000){stop("要確認","LINEの入力欄を一意に確認できません");return;}
    status("入力欄待機","入力欄の表示を確認しています");return;
   }
   AccessibilityNodeInfo input=inputs.get(0);
   if(!input.isEditable()||input.isPassword()){stop("要確認","LINEの数字入力欄を安全に確認できません");return;}
   String draft=Screen.inputValue(input);
   boolean prepared=q.key.equals(finalPrepared);
   if(!FinalSubmission.draftSafe(draft,answer,prepared)){
    stop("要確認","入力欄に別の文字があります。上書き・送信しません");return;
   }
   if(!prepared){
    if(!elementsStable){status("照合中","入力欄と第5問を再確認しています");return;}
    android.os.Bundle args=new android.os.Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,answer);
    boolean entered=input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args);
    if(!entered){if(input.isClickable()&&finalSince==0&&click(s,input)){finalSince=now;status("入力準備","入力欄を選択しています");return;}
     stop("要確認","入力欄への数字設定に失敗しました");return;}
    if(!session.edit().putString("final_prepared",q.key).commit()){stop("要確認","入力操作の記録を保存できません");return;}
    finalPrepared=q.key;finalSince=now;lastAction=now;elementMonitor.reset();status("入力確認待ち","第5問 · 答え: "+answer+" を設定。送信前に照合中");return;
   }
   if(!answer.equals(draft)){if(now-finalSince>15000){stop("要確認","入力した数字を読み戻せないため送信しません");return;}status("入力確認待ち","数字の入力結果を確認しています");return;}
   if(!elementsStable){status("送信確認","第5問の数字と送信ボタンを再確認しています");return;}
   AccessibilityNodeInfo send=s.composerSend();
   if(send==null){if(now-finalSince>18000){stop("要確認","青い送信ボタンを特定できません");return;}status("送信待機","青い送信ボタンを確認しています");return;}
   if(!session.edit().putString("pending",q.key).putString("final_submit_key",q.key).commit()){
    stop("要確認","送信の二重操作防止記録を保存できません");return;
   }
   guard.mark(q,now);lastAction=now;
   if(!performChecked(send)){stop("要確認","送信ボタンの操作結果が不明です。二重送信防止のため再試行しません");return;}
   questionSince=now;status("送信済確認待ち","第5問の数字を送信しました。正解画面を待っています");
  }
 }
 private boolean handleCoupon(Screen s,long now,boolean complete){
  if("display_clicked".equals(couponStage)){
   if(s.verifiedCouponPage()){clearPending();session.edit().remove("coupon_stage").commit();stop("完了","クーポン表示を確認しました");return true;}
   if(couponSince==0)couponSince=now;
   if(now-couponSince>35000){stop("要確認","クーポン表示を確認できません。ページを目視確認してください");return true;}
   status("クーポン表示待ち","クーポンページへ移動するのを待っています");return true;
  }
  String target=s.couponButton("クーポンを表示する")!=null?"クーポンを表示する":(!"get_clicked".equals(couponStage)&&s.couponButton("クーポンをゲットする")!=null)?"クーポンをゲットする":"";
  if(target.isEmpty()){
   if(complete||!couponStage.isEmpty()){
    if(couponSince==0)couponSince=now;
    if(now-couponSince>45000){stop("要確認","クーポンの操作ボタンが現れません");return true;}
    status("クーポン待機","クーポン専用ボタンの表示を確認しています");return true;
   }
   return false;
  }
  if(!elementsStable){status("照合中",target+" を確認しています");return true;}
  if(now-lastAction<ElementMonitor.INTERVAL_MS)return true;
  String nextStage=target.equals("クーポンを表示する")?"display_clicked":"get_clicked";
  if(!session.edit().putString("coupon_stage",nextStage).commit()){stop("要確認","クーポンの二重操作防止記録を保存できません");return true;}
  couponStage=nextStage;couponSince=now;
  if(!click(s,s.couponButton(target))){stop("要確認",target+" の操作が未確認です。重複操作しません");return true;}
  questionSince=now;status("クーポン進行",target+" をタップしました");return true;
 }
 private boolean click(Screen original,AccessibilityNodeInfo old){return click(original,old,true);}
 private boolean click(Screen original,AccessibilityNodeInfo old,boolean resetSearch){
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null)return false;
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();return false;}
  try(Screen fresh=new Screen(root)){
   if(!running||!fresh.trusted()||!fresh.pkg.equals(original.pkg)||!fresh.confirmationKey().equals(original.confirmationKey()))return false;
   if(original.question!=null&&(fresh.question==null||!fresh.question.key.equals(original.question.key)))return false;
   AccessibilityNodeInfo target=null;String resource=Screen.id(old),label=original.content(old).trim();
   if(resource.endsWith(":id/chat_ui_quick_reply_item_root"))target=fresh.quick(label);
   else {for(AccessibilityNodeInfo n:fresh.nodes)if(n.isClickable()&&n.isVisibleToUser()&&n.isEnabled()&&Screen.id(n).equals(resource)&&Screen.text(n).equals(Screen.text(old))){if(target!=null)return false;target=n;}}
   return target!=null&&performChecked(target,resetSearch);
  }
 }
 private void scroll(Screen s){scroll(s,false);}
 private void scroll(Screen s,boolean registration){
  Registration.Stage expected=s.registrationStage();
  if(!running||!s.trusted()||(registration?(expected==Registration.Stage.UNKNOWN||Registration.stage(s.latest)==Registration.Stage.UNKNOWN):(expected!=Registration.Stage.UNKNOWN||s.registrationChoicesVisible()))){
   stop("要確認","安全に探索できない画面を検出しました");return;
  }
  if(gesturePending||SystemClock.elapsedRealtime()-lastAction<ElementMonitor.INTERVAL_MS)return;
  RevealSearch.Step step=revealSearch.missing(SystemClock.elapsedRealtime());
  if(step==RevealSearch.Step.WAIT){status("表示待機","必要な要素が見つからないため、1秒後に再確認します");return;}
  if(step==RevealSearch.Step.EXHAUSTED){stop("要確認","上約10%・下は従来どおりの探索を3回行いましたが要素を確認できません");return;}
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null){stop("要確認","スライド直前の画面を取得できません");return;}
  try(Screen fresh=new Screen(root)){
   if(!fresh.trusted()||!fresh.pkg.equals(runPackage)||!fresh.confirmationKey().equals(s.confirmationKey())||(registration?(fresh.registrationStage()!=expected||Registration.stage(fresh.latest)==Registration.Stage.UNKNOWN):(fresh.registrationStage()!=Registration.Stage.UNKNOWN||fresh.registrationChoicesVisible()))){
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
   float[] y=RevealSearch.path(r.top,r.bottom,metrics.heightPixels,step);Path path=new Path();path.moveTo(x,y[0]);path.lineTo(x,y[1]);
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
