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
 private final Handler handler=new Handler(Looper.getMainLooper());private WindowManager wm;private LinearLayout overlay;private TextView info;private TextView titleView;private Button play,pause;private android.graphics.drawable.Drawable collapseIcon;private WindowManager.LayoutParams layout;private int compactX,compactY;
 private LinearLayout testButtons;private boolean expandedFloat=false,finalAnswerKnown=false,submitAttempted=false,inputVerified=false;
 private Runnable clipboardCleanup;
 private void cleanupClipboard(){if(clipboardCleanup!=null){Runnable task=clipboardCleanup;clipboardCleanup=null;handler.removeCallbacks(task);task.run();}}
 private int testGeneration=0;private boolean testBusy=false;private String testQuestion="",testPackage="";private int testWindow=-1;private long testCaptured=0;
 private String couponDiagKey="";private String autoDiagKey="";private boolean textChanged=false;
 private boolean testMode(){return Store.prefs(this).getBoolean("final_input_test_mode",false);}
 private AnswerBank bank;private boolean running=false;private RunGuard guard=new RunGuard();private String current="",answer="",transition="";private long lastTick=0,lastAction=0,questionSince=0;private String previousPackage="";
 private final java.util.Random registrationRandom=new java.util.Random();private int registrationSteps=0;private long registrationSince=0,registrationSent=0;private String registrationPending="",registrationPriorQuestion="";
 private android.content.SharedPreferences session;private String runPackage="",finalPrepared="",couponStage="";private long couponSince=0,finalSince=0;
 private long anchorMissingSince=0;private boolean keyboardRequested=false;private boolean resumeChecked=false;private String savedPending="",savedPackage="",transitionScreen="";private int transitionSteps=0;
 private final ManualFinal manualFinal=new ManualFinal();private final FinalAnchor finalAnchor=new FinalAnchor();private final ElementMonitor elementMonitor=new ElementMonitor();private final RevealSearch revealSearch=new RevealSearch();private boolean elementsStable=false,gesturePending=false;
 private final Runnable poll=new Runnable(){public void run(){check();handler.postDelayed(this,ElementMonitor.INTERVAL_MS);}};
 @Override protected void onServiceConnected(){instance=this;try{bank=Store.bank(this);}catch(Exception e){status("要確認","正答データを読み込めません");}wm=(WindowManager)getSystemService(WINDOW_SERVICE);handler.post(poll);}
 public void showFloat(){if(overlay!=null)return;expandedFloat=false;final boolean inputTest=testMode();final int headerHeight=inputTest?36:72;
  overlay=new LinearLayout(this);overlay.setOrientation(LinearLayout.VERTICAL);overlay.setBackgroundColor(Color.rgb(17,40,51));
  LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.HORIZONTAL);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(8),0,dp(4),0);overlay.addView(header,new LinearLayout.LayoutParams(-1,dp(headerHeight)));
  titleView=new TextView(this);titleView.setTextColor(Color.WHITE);titleView.setTextSize(22.5f);titleView.setTypeface(null,android.graphics.Typeface.BOLD);titleView.setText("待機中");titleView.setMaxLines(inputTest?1:2);titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);titleView.setAutoSizeTextTypeUniformWithConfiguration(9,22,1,android.util.TypedValue.COMPLEX_UNIT_SP);titleView.setGravity(Gravity.CENTER_VERTICAL);titleView.setPadding(dp(5),0,dp(5),0);
  header.addView(titleView,new LinearLayout.LayoutParams(0,dp(headerHeight),1));
  play=new Button(this);play.setText("▶");play.setAllCaps(false);play.setTextSize(30);play.setMinHeight(0);play.setMinimumHeight(0);play.setMinWidth(0);play.setMinimumWidth(0);play.setPadding(0,0,0,0);play.setContentDescription("再生");play.setOnClickListener(v->{if(running||testBusy)return;if(testMode())prepareInputTest();else startRun();});header.addView(play,new LinearLayout.LayoutParams(dp(60),dp(68)));
  collapseIcon=getDrawable(R.drawable.ic_float_collapse);pause=new Button(this);pause.setForegroundGravity(Gravity.CENTER);pause.setForegroundTintList(pause.getTextColors());pause.setAllCaps(false);pause.setTextSize(30);pause.setMinHeight(0);pause.setMinimumHeight(0);pause.setMinWidth(0);pause.setMinimumWidth(0);pause.setPadding(0,0,0,0);pause.setOnClickListener(v->{if(running||testBusy)stop("一時停止","利用者が停止しました。▶で現在画面から再開できます");else floatSize(false);});header.addView(pause,new LinearLayout.LayoutParams(dp(60),dp(68)));updateFloatControls();
  Button close=new Button(this);close.setText("×");close.setAllCaps(false);close.setTextSize(33);close.setMinHeight(0);close.setMinimumHeight(0);close.setMinWidth(0);close.setMinimumWidth(0);close.setPadding(0,0,0,0);close.setContentDescription("フロートを閉じる");close.setOnClickListener(v->{stop("停止","フロートを閉じました");hideFloat();});header.addView(close,new LinearLayout.LayoutParams(dp(60),dp(68)));
  info=new TextView(this);info.setTextColor(Color.WHITE);info.setTextSize(21);info.setPadding(dp(12),dp(3),dp(12),dp(3));info.setGravity(Gravity.TOP);info.setIncludeFontPadding(false);info.setHorizontallyScrolling(false);info.setMaxLines(4);info.setEllipsize(android.text.TextUtils.TruncateAt.END);info.setAutoSizeTextTypeUniformWithConfiguration(9,21,1,android.util.TypedValue.COMPLEX_UNIT_SP);info.setVisibility(View.GONE);overlay.addView(info,new LinearLayout.LayoutParams(-1,0));
  testButtons=new LinearLayout(this);testButtons.setOrientation(LinearLayout.HORIZONTAL);
  for(int i=1;i<=3;i++){final int mode=i;Button b=new Button(this);b.setText("再生"+i);b.setTextSize(14);b.setPadding(0,0,0,0);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);b.setOnClickListener(v->runInputTest(mode));testButtons.addView(b,new LinearLayout.LayoutParams(0,dp(36),1));}
  Button copy=new Button(this);copy.setText("診断コピー");copy.setTextSize(12);copy.setPadding(0,0,0,0);copy.setMinWidth(0);copy.setMinimumWidth(0);copy.setMinHeight(0);copy.setMinimumHeight(0);copy.setOnClickListener(v->copyDiagnostics());testButtons.addView(copy,new LinearLayout.LayoutParams(0,dp(36),1));testButtons.setVisibility(inputTest?View.VISIBLE:View.GONE);overlay.addView(testButtons,new LinearLayout.LayoutParams(-1,dp(36)));
  if(inputTest){for(Button b:new Button[]{play,pause,close}){b.getLayoutParams().height=dp(34);b.setTextSize(22);}}
  android.content.SharedPreferences prefs=Store.prefs(this);compactX=prefs.getInt("float_x",dp(12));compactY=prefs.getInt("float_y",0);
  layout=new WindowManager.LayoutParams(compactWidth(),dp(72),WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);
  layout.gravity=Gravity.TOP|Gravity.LEFT;
  if(android.os.Build.VERSION.SDK_INT>=28)layout.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
  layout.x=compactX;layout.y=compactY;clampFloat(false);layout.x=compactX;layout.y=compactY;
  titleView.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy;boolean dragging;
   public boolean onTouch(View v,MotionEvent e){switch(e.getActionMasked()){
    case MotionEvent.ACTION_DOWN:sx=e.getRawX();sy=e.getRawY();ox=layout.x;oy=layout.y;dragging=false;return true;
    case MotionEvent.ACTION_MOVE:
     if(Math.abs(e.getRawX()-sx)+Math.abs(e.getRawY()-sy)>dp(5))dragging=true;
     layout.x=ox+(int)(e.getRawX()-sx);layout.y=oy+(int)(e.getRawY()-sy);
     clampFloat(expandedFloat);if(overlay!=null)wm.updateViewLayout(overlay,layout);return true;
    case MotionEvent.ACTION_UP:
     if(dragging){compactY=layout.y;compactX=expandedFloat?Store.prefs(QuizService.this).getInt("float_x",dp(12)):layout.x;
      Store.prefs(QuizService.this).edit().putInt("float_x",compactX).putInt("float_y",compactY).apply();}
     return true;
    default:return true;
   }}
  });
  try{wm.addView(overlay,layout);floatSize(true);if(testMode())status("第5問・入力テスト","▶で問題を確認。再生1〜3は入力だけ・送信なし");else status("待機中","▶で現在の問題から開始します");}catch(Exception e){overlay=null;stop("要確認","フロートを表示できません");}
 }
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private int compactWidth(){return Math.min(dp(390),getResources().getDisplayMetrics().widthPixels);}
 private int floatHeight(boolean expanded){return dp(72);}
 private void clampFloat(boolean expanded){
  android.util.DisplayMetrics dm=getResources().getDisplayMetrics();
  int w=compactWidth(),h=floatHeight(expanded);
  int maxX=Math.max(0,dm.widthPixels-w),maxY=Math.max(0,dm.heightPixels-h);
  layout.x=Math.max(0,Math.min(layout.x,maxX));layout.y=Math.max(0,Math.min(layout.y,maxY));
  if(!expanded){compactX=Math.max(0,Math.min(compactX,maxX));compactY=Math.max(0,Math.min(compactY,maxY));}
 }
 private void floatSize(boolean expanded){
  if(overlay==null||layout==null)return;
  expandedFloat=false;
  info.setVisibility(View.GONE);
  layout.width=compactWidth();
  layout.height=floatHeight(expanded);
  clampFloat(false);wm.updateViewLayout(overlay,layout);updateFloatControls();
 }
 private void updateFloatControls(){
  boolean active=running||testBusy;
  if(play!=null)play.setEnabled(!active);
  if(pause!=null){pause.setText(active?"||":"");pause.setForeground(active?null:collapseIcon);pause.setContentDescription(active?"一時停止（フロートを残す）":"横長フロート（固定表示）");pause.setEnabled(true);}
 }
 private void hideFloat(){if(overlay!=null){wm.removeView(overlay);overlay=null;info=null;titleView=null;play=null;pause=null;}}
 private void startRun(){testGeneration++;testBusy=false;finalAnswerKnown=false;submitAttempted=false;inputVerified=false;autoDiagKey="";couponDiagKey="";if(bank==null){stop("要確認","正答データ不備");return;}
  manualFinal.reset();finalAnchor.reset();elementMonitor.reset();revealSearch.reset();gesturePending=false;elementsStable=false;
  anchorMissingSince=0;keyboardRequested=false;session=null;runPackage="";finalPrepared="";couponStage="";couponSince=0;finalSince=0;savedPending="";savedPackage="";resumeChecked=false;transitionSteps=0;transitionScreen="";
  registrationSteps=0;registrationSince=0;registrationSent=0;registrationPending="";registrationPriorQuestion="";guard=new RunGuard();current="";answer="";transition="";lastAction=0;lastTick=0;questionSince=SystemClock.elapsedRealtime();running=true;play.setContentDescription("再生（実行中）");status("認識中","現在のLINE画面から開始します");check();
 }
 public void stop(String state,String detail){cleanupClipboard();running=false;testBusy=false;testGeneration++;elementMonitor.reset();revealSearch.reset();if(play!=null)play.setContentDescription("再生");
  if(!couponStage.isEmpty()&&!state.equals("完了"))detail+="\n"+(couponStage.equals("get_clicked")?"LINEの「クーポンを表示する」を手動で押してください。":"開いたクーポンページを確認してください。自動で押し直しません。");
  else if(finalAnswerKnown&&!state.equals("完了")&&!testMode())detail+="\n"+(submitAttempted?"送信履歴を確認してください。再送しないでください。":inputVerified?"入力欄の答えを確認して、手動で送信してください。":"同じ問題か確認し、上の答えを手動で入力・送信してください。");
  if(testMode())floatSize(true);status(state,detail);Store.log(this,state,current,answer,detail);
 }
 private void status(String state,String detail){updateFloatControls();Store.prefs(this).edit().putString("state",state).putString("detail",detail).putString("question",current).putString("answer",answer).apply();if(titleView!=null)titleView.setText((finalAnswerKnown?"答え "+answer+" · ":"")+state);if(info!=null)info.setText((finalAnswerKnown?"第5問の答え："+answer+"\n":"")+detail);}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){if(e.getEventType()==AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED&&(running||testBusy)){textChanged=true;}if(!running)return;int type=e.getEventType();
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
   if(runPackage.isEmpty()){
    runPackage=s.pkg;Store.migrateSession(this,s.pkg);session=Store.session(this,s.pkg);
    savedPending=session.getString("pending","");savedPackage=s.pkg;registrationPending=session.getString("registration_pending","");registrationPriorQuestion=session.getString("registration_prior_question","");
    finalPrepared=session.getString("final_prepared","");couponStage=session.getString("coupon_stage","");Store.prefs(this).edit().putString("active_line_package",s.pkg).apply();
   }else if(!runPackage.equals(s.pkg)){stop("停止","LINEを切り替えました。対象トークで▶再生してください");return;}
   previousPackage=s.pkg;elementsStable=elementMonitor.observe(s.confirmationKey(),now);
   if("share_clicked".equals(couponStage)){
    if(s.trusted()&&s.question!=null&&s.question.number<5&&s.visibleQuestion()&&s.couponButton("クーポンを表示する")==null&&s.couponButton("クーポンをゲットする")==null){
     if(!session.edit().remove("coupon_stage").commit()){stop("要確認","新しいクイズの開始記録を保存できません");return;}couponStage="";couponSince=0;
    }else{stop("完了","共有ボタンは操作済みです。共有先の一覧を確認してください。自動で押し直しません");return;}
   }
   if(!s.trusted()){
    if("display_clicked".equals(couponStage)){handleCouponShare(s,now);return;}
    stop("要確認","対象LINE画面を確認できません。超良問ドリルのトークで再生してください");return;
   }
   boolean submittedFinal=!guard.pending.isEmpty()&&guard.pending.equals(session.getString("final_submit_key",""));
   boolean finalResult=finalAnchor.result(s.question,submittedFinal,QuizFlow.complete(s.model)||CouponFlow.pending(couponStage)||s.couponButton("クーポンをゲットする")!=null||s.couponButton("クーポンを表示する")!=null);
   if(finalAnchor.mismatch(s.question)&&!finalResult){
    if(submittedFinal){if(s.question!=null){stop("要確認","送信後に別の問題を検知しました。再送しません");return;}if(now-guard.sentAt>180000){stop("要確認","送信後の結果を確認できません。再送しません");return;}status("結果待機","送信済みの第5問の結果を待っています");return;}
    if(manualFinal.started()){stop("要確認","第5問 · 答え: "+answer+"\n現在の問題を確認できないため停止。入力・送信は行いません");return;}
    if(!elementsStable){status("照合中","探索で別の問題へ移ったため2回確認しています");return;}
    if(anchorMissingSince==0)anchorMissingSince=now;
    AccessibilityNodeInfo bottom=s.uniqueId("chat_ui_scroll_to_bottom_button");
    if(bottom==null||!finalAnchor.recoverOnce()){if(now-anchorMissingSince>24000){stop("要確認","第5問の現在位置を24秒間確認できませんでした。最新メッセージへ戻して▶再生してください");return;}status("現在位置待機","第5問の再表示を待っています。過去の問題は操作しません");return;}
    if(!click(s,bottom,false)){stop("要確認","最新メッセージへ戻れません。過去の問題は操作しません");return;}
    status("現在位置復帰","過去の問題を操作せず、最新メッセージへ戻しています");return;
   }
   anchorMissingSince=0;
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
    if(now-questionSince>120000){stop("要確認","進行ボタンの反映を確認できません");return;}
    status("画面待機",next+" の反映を待っています");return;
   }
   if(handleRegistration(s,now))return;
   if(QuizFlow.failed(s.model)){stop("要確認","不正解表示。再挑戦ボタンを確認できません");return;}
   Question q=s.question;
   if(!guard.pending.isEmpty()){
    if(q!=null&&!q.key.equals(guard.pending)&&guard.confirmNext(q)){clearPending();Store.log(this,"回答確認",current,answer,"次の問題の表示を確認");questionSince=now;transition="";}
    else {
     if(now-guard.sentAt>180000){stop("要確認","回答の反映を確認できません。再回答せず停止");return;}
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
   if(q==null){if(now-questionSince>120000){stop("要確認","最新問題を取得できません");return;}scroll(s);return;}
   if(current.isEmpty())guard.lastConfirmed=q.number-1;
   if(q.number<5)finalAnswerKnown=false;
   current="第"+q.number+"問\n"+q.body;
   if(QuizFlow.correct(s.model)){if(now-questionSince>120000){stop("要確認","次の問題ボタンが表示されません");return;}status("次問待機","正解済みのため次の専用ボタンを待っています");scroll(s);return;}
   if(!guard.canAnswer(q)){stop("要確認","問題番号が想定した順序と異なります");return;}
   if(!s.visibleQuestion()&&!(q.number==5&&finalPrepared.equals(q.key))){scroll(s);return;}
   try{
    if(q.number<5){answer=bank.match(q);status("回答待機",q.number+"/5 · 答え "+answer);AccessibilityNodeInfo b=s.quick(answer);
     if(b==null){if(now-questionSince>120000){stop("要確認","回答ボタンが表示されません");return;}scroll(s);return;}
     if(!elementsStable){status("照合中",q.number+"/5 · 問題文と回答ボタンを2回確認しています");return;}
     mark(q,now);if(!tapQuizAnswer(s,q,answer)){stop("要確認","回答タップを開始できません（未確認記録を保持）");return;}status("確定確認待ち",q.number+"/5 · "+answer+" の画面タップを要求。LINEの結果を待っています");
    }else{
     finalAnswerKnown=false;answer="";MathEngine.Result r=MathEngine.solve(q.body);answer=r.answer;finalAnswerKnown=true;status("第5問・自動回答","数字を入力する前に答えを表示しています");
     handleFinal(s,q,now);
    }
   }catch(IllegalArgumentException e){stop("要確認",e.getMessage());}
  }catch(Exception e){stop("要確認","画面確認エラー: "+e.getClass().getSimpleName());}
 }
 private boolean handleRegistration(Screen s,long now){
   if(s.quizQuestionActive())return false; // Visible quiz outranks registration keywords.
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
  if(!guard.pending.isEmpty()){if(now-guard.sentAt>30000)stop("要確認","回答操作後30秒間、LINEの反映を確認できません。二重回答せず停止します");else status("回答確認待ち","登録用の文字も見えますが回答確定を優先して待機中です");return true;}
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
  String choice=Registration.choose(stage,labels,registrationRandom);
  if(stage==Registration.Stage.INTRO){java.util.Map<String,Integer> centres=new java.util.LinkedHashMap<>();for(java.util.Map.Entry<String,AccessibilityNodeInfo> e:controls.entrySet()){Rect bounds=new Rect();e.getValue().getBoundsInScreen(bounds);centres.put(e.getKey(),bounds.centerX());}choice=Registration.rightIntro(centres);}if(choice==null){stop("要確認","登録用の専用選択肢を確認できません");return true;}
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
  if(overlay!=null){int[] xy=new int[2];overlay.getLocationOnScreen(xy);Rect cover=new Rect(xy[0],xy[1],xy[0]+overlay.getWidth(),xy[1]+overlay.getHeight());if(Rect.intersects(bounds,cover)&&!moveFloatAway(bounds)){status("要確認","フロートがボタンに重なっています。ドラッグで移動してください");return false;}}
  lastAction=SystemClock.elapsedRealtime();boolean clicked=target.performAction(AccessibilityNodeInfo.ACTION_CLICK);if(clicked){elementMonitor.reset();if(resetSearch)revealSearch.reset();}return clicked;
 }
 private void mark(Question q,long now){if(!session.edit().putString("pending",q.key).commit())throw new IllegalArgumentException("二重操作防止記録を保存できません");guard.mark(q,now);transitionScreen="";Store.log(this,"回答準備",current,answer,"二重操作防止記録を保存。LINEへのタップは未確認");}
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
   String diagnosticKey=q.key+":"+keyboardVisible()+":"+inputs.size();if(!diagnosticKey.equals(autoDiagKey)){autoDiagKey=diagnosticKey;InputDiagnostics.capture(this,"AUTO_S4",s);}
   if(inputs.size()>1){stop("手動操作へ","S4_AMBIGUOUS: 入力欄を一意に確認できません");return;}
   if(inputs.isEmpty()){
    AccessibilityNodeInfo opener=s.keyboardButton();
    if(opener!=null&&!keyboardRequested&&elementsStable){keyboardRequested=true;if(finalSince==0)finalSince=now;if(!click(s,opener))stop("要確認","数字入力欄を開けません");else status("入力欄待機","LINEの数字入力欄を開いています");return;}
    if(finalSince==0)finalSince=now;
    if(now-finalSince>24000){stop("手動操作へ","S4_INPUT_COUNT: 入力欄候補="+inputs.size()+"。診断を保存しました");return;}
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
    if(!InputDiagnostics.action(input,AccessibilityNodeInfo.ACTION_SET_TEXT)){InputDiagnostics.add(this,"S5_UNSUPPORTED","SET_TEXT absent");stop("手動操作へ","S5_UNSUPPORTED: この入力欄は直接入力に対応していません");return;}
    android.os.Bundle args=new android.os.Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,answer);
    boolean entered=input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args);InputDiagnostics.add(this,"AUTO_S5","setText="+entered);
    if(!entered){if(input.isClickable()&&finalSince==0&&click(s,input)){finalSince=now;status("入力準備","入力欄を選択しています");return;}
     stop("要確認","入力欄への数字設定に失敗しました");return;}
    if(!session.edit().putString("final_prepared",q.key).commit()){stop("要確認","入力操作の記録を保存できません");return;}
    finalPrepared=q.key;finalSince=now;lastAction=now;elementMonitor.reset();status("入力確認待ち","第5問 · 答え: "+answer+" を設定。送信前に照合中");return;
   }
   if(!answer.equals(draft)){if(now-finalSince>30000){stop("要確認","入力した数字を読み戻せないため送信しません");return;}status("入力確認待ち","数字の入力結果を確認しています");return;}
   if(!inputVerified){InputDiagnostics.capture(this,"AUTO_S6_MATCH",s);InputDiagnostics.add(this,"AUTO_S6","matches=true length="+draft.length());}
   inputVerified=true;
   if(!elementsStable){status("送信確認","第5問の数字と送信ボタンを再確認しています");return;}
   AccessibilityNodeInfo send=s.composerSend();
   if(send==null){if(now-finalSince>36000){stop("要確認","青い送信ボタンを特定できません");return;}status("送信待機","青い送信ボタンを確認しています");return;}
   if(!session.edit().putString("pending",q.key).putString("final_submit_key",q.key).commit()){
    stop("要確認","送信の二重操作防止記録を保存できません");return;
   }
   guard.mark(q,now);lastAction=now;submitAttempted=true;
   boolean sent=performChecked(send);InputDiagnostics.add(this,"AUTO_S7","click="+sent);
   if(!sent){stop("要確認","送信ボタンの操作結果が不明です。二重送信防止のため再試行しません");return;}
   questionSince=now;status("送信済確認待ち","第5問の数字を送信しました。正解画面を待っています");
  }
 }
 private boolean handleCoupon(Screen s,long now,boolean complete){
  AccessibilityNodeInfo display=s.couponButton("クーポンを表示する");String diag=couponStage+":"+(display==null?"missing":Screen.id(display)+":"+display.isClickable());if(!diag.equals(couponDiagKey)){couponDiagKey=diag;InputDiagnostics.coupon(this,s);}
  if("display_clicked".equals(couponStage)){
   if(s.verifiedCouponPage()){handleCouponShare(s,now);return true;}
   if(couponSince==0)couponSince=now;
   if(now-couponSince>70000){stop("要確認","クーポン表示を確認できません。ページを目視確認してください");return true;}
   status("クーポン表示待ち","クーポンページへ移動するのを待っています");return true;
  }
  String target=CouponFlow.next(couponStage,s.couponButton("クーポンをゲットする")!=null,s.couponButton("クーポンを表示する")!=null);
  if(target.isEmpty()){
   if(complete||!couponStage.isEmpty()){
    if(couponSince==0)couponSince=now;
    if(now-couponSince>90000){stop("要確認","クーポンの操作ボタンが現れません");return true;}
    if(couponStage.isEmpty()){scroll(s);return true;}
    status("クーポン待機","クーポン専用ボタンの表示を確認しています");return true;
   }
   return false;
  }
  if(!elementsStable){status("照合中",target+" を確認しています");return true;}
  if(now-lastAction<ElementMonitor.INTERVAL_MS)return true;
  AccessibilityNodeInfo selected=s.couponButton(target);if(selected==null)return true;Rect selectedBounds=new Rect();selected.getBoundsInScreen(selectedBounds);if(moveFloatAway(selectedBounds)){elementMonitor.reset();status("表示調整","フロートを移動し、クーポンのリンクを再確認します");return true;}
  String nextStage=target.equals("クーポンを表示する")?"display_clicked":"get_clicked";
  if(!session.edit().putString("coupon_stage",nextStage).putLong("coupon_action_at",System.currentTimeMillis()).commit()){stop("要確認","クーポンの二重操作防止記録を保存できません");return true;}
  couponStage=nextStage;couponSince=now;
  boolean clicked=clickCoupon(s,target);InputDiagnostics.add(this,"AUTO_COUPON","stage="+nextStage+" click="+clicked);
  if(!clicked){stop("要確認",target+" の操作が未確認です。重複操作しません");return true;}
  questionSince=now;status("クーポン進行",target+" をタップしました");return true;
 }
 private void handleCouponShare(Screen s,long now){
  long recorded=session.getLong("coupon_action_at",0),age=System.currentTimeMillis()-recorded;
  if(recorded<=0||age<0||age>120000){stop("要確認","クーポンを開いた直後の画面か確認できません。共有は手動で押してください");return;}
  if(couponSince==0)couponSince=now;
  int width=getResources().getDisplayMetrics().widthPixels,height=getResources().getDisplayMetrics().heightPixels;
  AccessibilityNodeInfo share=s.couponShare(width,height);
  String diag="share:"+(share==null?"missing":Screen.id(share));
  if(!diag.equals(couponDiagKey)){couponDiagKey=diag;InputDiagnostics.share(this,s,share);}
  if(share==null){if(now-couponSince>70000){stop("要確認","クーポン画面の共有ボタンを特定できません。画面下部の共有を手動で押してください");return;}status("共有待機","クーポン画面と下部の共有ボタンを確認しています");return;}
  if(!elementsStable){status("照合中","クーポン画面の共有ボタンを2回確認しています");return;}
  Rect bounds=new Rect();share.getBoundsInScreen(bounds);if(moveFloatAway(bounds)){elementMonitor.reset();return;}
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null){stop("要確認","共有直前のLINE画面を確認できません");return;}
  try(Screen fresh=new Screen(root)){
   AccessibilityNodeInfo target=fresh.couponShare(width,height);
   if(!running||!testUnlocked()||!fresh.pkg.equals(runPackage)||!Store.allowed(this,fresh.pkg)||!fresh.confirmationKey().equals(s.confirmationKey())||target==null)return;
   if(!target.refresh()||!target.isVisibleToUser()||!target.isEnabled()||!target.isClickable())return;
   if(!session.edit().putString("coupon_stage","share_clicked").commit()){stop("要確認","共有操作の記録を保存できません");return;}
   couponStage="share_clicked";
   boolean clicked=performChecked(target);InputDiagnostics.add(this,"AUTO_SHARE","click="+clicked+" recipientSelected=false");
   if(clicked){clearPending();stop("完了","共有ボタンを押しました。表示された共有先の一覧から手動で選んでください");}
   else stop("要確認","共有ボタンの操作結果が不明です。共有先の一覧を確認してください。自動で押し直しません");
  }
 }
 /** Move only our own overlay; the next snapshot confirms any gesture target again. */
 private boolean moveFloatAway(Rect target){
  if(overlay==null||layout==null)return false;int[] xy=new int[2];overlay.getLocationOnScreen(xy);Rect cover=new Rect(xy[0],xy[1],xy[0]+overlay.getWidth(),xy[1]+overlay.getHeight());
  if(!Rect.intersects(cover,target))return false;int height=getResources().getDisplayMetrics().heightPixels,h=overlay.getHeight(),next=-1;
  if(target.top>=h+dp(8))next=0;else if(target.bottom+dp(8)<=height-h)next=height-h;
  if(next<0||layout.y==next)return false;layout.y=next;wm.updateViewLayout(overlay,layout);return true;
 }
 private boolean clickCoupon(Screen original,String label){
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null)return false;
  try(Screen fresh=new Screen(root)){
   if(!running||!testUnlocked()||!fresh.trusted()||!fresh.pkg.equals(runPackage)||!fresh.confirmationKey().equals(original.confirmationKey()))return false;
   AccessibilityNodeInfo target=fresh.couponButton(label);if(target==null||!target.refresh()||!target.isEnabled()||!target.isVisibleToUser())return false;
   if(target.isClickable())return performChecked(target);
   // Exact native text node in the latest verified coupon card; no fixed screen coordinates.
   if(!label.equals("クーポンを表示する")||!Screen.text(target).trim().equals(label)||fresh.model.couponLink(label)<0)return false;
   Rect b=new Rect();target.getBoundsInScreen(b);android.util.DisplayMetrics dm=getResources().getDisplayMetrics();
   if(b.width()<=0||b.height()<=0||b.left<0||b.top<0||b.right>dm.widthPixels||b.bottom>dm.heightPixels)return false;
   if(overlay!=null){int[] xy=new int[2];overlay.getLocationOnScreen(xy);if(Rect.intersects(b,new Rect(xy[0],xy[1],xy[0]+overlay.getWidth(),xy[1]+overlay.getHeight())))return false;}
   java.util.List<AccessibilityWindowInfo> ws=getWindows();try{for(AccessibilityWindowInfo w:ws)if(w.getType()==AccessibilityWindowInfo.TYPE_INPUT_METHOD){Rect ime=new Rect();w.getBoundsInScreen(ime);if(Rect.intersects(b,ime))return false;}}finally{for(AccessibilityWindowInfo w:ws)w.recycle();}
   Path path=new Path();path.moveTo(b.centerX(),b.centerY());final int generation=testGeneration;gesturePending=true;lastAction=SystemClock.elapsedRealtime();
   boolean accepted=dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,0,80)).build(),new GestureResultCallback(){
    @Override public void onCompleted(GestureDescription g){if(generation!=testGeneration)return;gesturePending=false;elementMonitor.reset();InputDiagnostics.add(QuizService.this,"COUPON_GESTURE","completed=true");}
    @Override public void onCancelled(GestureDescription g){if(generation!=testGeneration)return;gesturePending=false;if(running)stop("要確認","クーポンリンクのタップが中断されました。押し直しません");}
   },handler);if(!accepted)gesturePending=false;else{elementMonitor.reset();revealSearch.reset();}InputDiagnostics.add(this,"COUPON_GESTURE","accepted="+accepted);return accepted;
  }
 }

  /** Gesture-based A-D reply: one physical tap per recorded question; never blindly retry. */
  private boolean tapQuizAnswer(Screen original,Question q,String letter){
   AccessibilityNodeInfo root=foregroundLineRoot();if(root==null)return false;
   if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();return false;}
   try(Screen fresh=new Screen(root)){
    if(!running||!fresh.trusted()||!fresh.pkg.equals(original.pkg)||fresh.question==null||!q.key.equals(fresh.question.key)||!fresh.confirmationKey().equals(original.confirmationKey()))return false;
    if(!fresh.quizQuestionActive())return false;
    AccessibilityNodeInfo target=fresh.quick(letter);
    if(target==null||!target.refresh()||!target.isVisibleToUser()||!target.isEnabled()||!target.isClickable())return false;
    Rect bounds=new Rect();target.getBoundsInScreen(bounds);android.util.DisplayMetrics metrics=getResources().getDisplayMetrics();
    if(bounds.width()<dp(12)||bounds.height()<dp(12)||bounds.centerX()<0||bounds.centerY()<0||bounds.centerX()>=metrics.widthPixels||bounds.centerY()>=metrics.heightPixels)return false;
    if(overlay!=null){int[] xy=new int[2];overlay.getLocationOnScreen(xy);Rect cover=new Rect(xy[0],xy[1],xy[0]+overlay.getWidth(),xy[1]+overlay.getHeight());if(Rect.intersects(bounds,cover)&&!moveFloatAway(bounds))return false;}
    Path path=new Path();path.moveTo(bounds.centerX(),bounds.centerY());
    final int generation=testGeneration;final String pending=q.key;
    gesturePending=true;lastAction=SystemClock.elapsedRealtime();
    boolean accepted=dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,0,80)).build(),new GestureResultCallback(){
     @Override public void onCompleted(GestureDescription gesture){
      if(generation!=testGeneration)return;gesturePending=false;elementMonitor.reset();
      if(running&&pending.equals(guard.pending)){Store.log(QuizService.this,"タップ完了",current,answer,"画面タップ完了。LINE側の回答反映は未確認");status("回答反映待ち","画面タップ完了。LINEの正解・次問表示を確認中");}
     }
     @Override public void onCancelled(GestureDescription gesture){
      if(generation!=testGeneration)return;gesturePending=false;
      if(running)stop("要確認","回答タップが中断されました。二重回答防止記録を保持します");
     }
    },handler);
    if(!accepted){gesturePending=false;return false;}
    elementMonitor.reset();Store.log(this,"タップ要求",current,answer,"専用"+letter+"ボタンに一度だけタップ要求。LINE側の反映は未確認");return true;
   }
  }
 private boolean click(Screen original,AccessibilityNodeInfo old){return click(original,old,true);}
 private boolean click(Screen original,AccessibilityNodeInfo old,boolean resetSearch){
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null)return false;
  if(!Store.allowed(this,String.valueOf(root.getPackageName()))){root.recycle();return false;}
  try(Screen fresh=new Screen(root)){
   if(!running||!fresh.trusted()||!fresh.pkg.equals(original.pkg)||!fresh.confirmationKey().equals(original.confirmationKey()))return false;
   if(original.question!=null&&(fresh.question==null||!fresh.question.key.equals(original.question.key)))return false;
   AccessibilityNodeInfo target=null;String resource=Screen.id(old),label=original.content(old).trim();
   if(label.equals("クーポンをゲットする")||label.equals("クーポンを表示する"))target=fresh.couponButton(label);
   else if(resource.endsWith(":id/chat_ui_quick_reply_item_root"))target=fresh.quick(label);
   else {for(AccessibilityNodeInfo n:fresh.nodes)if(n.isClickable()&&n.isVisibleToUser()&&n.isEnabled()&&Screen.id(n).equals(resource)&&Screen.text(n).equals(Screen.text(old))){if(target!=null)return false;target=n;}}
   return target!=null&&performChecked(target,resetSearch);
  }
 }
 private void scroll(Screen s){scroll(s,false);}
 private void scroll(Screen s,boolean registration){
  if(!running)return;
  Registration.Stage expected=s.registrationStage();
  if(!s.trusted()||(registration?(expected==Registration.Stage.UNKNOWN||Registration.stage(s.latest)==Registration.Stage.UNKNOWN):(expected!=Registration.Stage.UNKNOWN||s.registrationChoicesVisible()))){
   stop("要確認","安全に探索できない画面を検出しました");return;
  }
  if(gesturePending||SystemClock.elapsedRealtime()-lastAction<ElementMonitor.INTERVAL_MS)return;
  RevealSearch.Step step=revealSearch.missing(SystemClock.elapsedRealtime());
  if(step==RevealSearch.Step.WAIT){status("表示待機","必要な要素が見つからないため、0.5秒後に再確認します");return;}
  if(step==RevealSearch.Step.EXHAUSTED){stop("要確認","下へ→フロートの高さ分だけ上への探索を最大6往復・26秒行いましたが要素を確認できません");return;}
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null){stop("要確認","スライド直前の画面を取得できません");return;}
  try(Screen fresh=new Screen(root)){
   if(!fresh.trusted()||!fresh.pkg.equals(runPackage)||!fresh.confirmationKey().equals(s.confirmationKey())||(registration?(fresh.registrationStage()!=expected||Registration.stage(fresh.latest)==Registration.Stage.UNKNOWN):(fresh.registrationStage()!=Registration.Stage.UNKNOWN||fresh.registrationChoicesVisible()))){
    elementMonitor.reset();revealSearch.found();status("照合中","スライド前に画面が変わったため再確認します");return;
   }
   if(((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive()){stop("要確認","画面ロック・画面OFFを検知しました");return;}
   Rect r=new Rect();AccessibilityNodeInfo area=fresh.uniqueId("chat_ui_main_content_area");
   if(area==null){status("表示待機","スライド領域が未確認のため、画面を再取得しています");return;}area.getBoundsInScreen(r);
   android.util.DisplayMetrics metrics=getResources().getDisplayMetrics();if(!r.intersect(0,0,metrics.widthPixels,metrics.heightPixels)||r.height()<dp(200)||r.width()<dp(100)){stop("要確認","安全なスライド領域を確認できません");return;}
   java.util.List<float[]> blocked=new java.util.ArrayList<>();
   if(overlay!=null){int[] xy=new int[2];overlay.getLocationOnScreen(xy);blocked.add(new float[]{xy[0],xy[1],xy[0]+overlay.getWidth(),xy[1]+overlay.getHeight()});}
   java.util.List<AccessibilityWindowInfo> windows=getWindows();try{for(AccessibilityWindowInfo w:windows)if(w.getType()==AccessibilityWindowInfo.TYPE_INPUT_METHOD){Rect b=new Rect();w.getBoundsInScreen(b);if(!b.isEmpty())blocked.add(new float[]{b.left,b.top,b.right,b.bottom});}}finally{for(AccessibilityWindowInfo w:windows)w.recycle();}
   float[] lane=SwipeLane.choose(r.left,r.top,r.right,r.bottom,blocked,dp(160),dp(12));
   if(lane==null){status("表示待機","操作できる範囲が狭いため再確認中です。フロートはドラッグで移動できます");return;}
   float upwardDistancePx=overlay!=null&&overlay.getHeight()>0?overlay.getHeight():dp(72);
   float[] y=RevealSearch.path(lane[1],lane[2],upwardDistancePx,step);Path path=new Path();path.moveTo(lane[0],y[0]);path.lineTo(lane[0],y[1]);
   if(!running)return;
   final int generation=testGeneration;final int pair=revealSearch.pair();gesturePending=true;lastAction=SystemClock.elapsedRealtime();elementMonitor.reset();
   boolean accepted=dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,0,RevealSearch.duration(step))).build(),new GestureResultCallback(){
    @Override public void onCompleted(GestureDescription g){if(generation!=testGeneration)return;gesturePending=false;elementMonitor.reset();if(running)status("表示確認",pair+"/6 · スライド後の要素を確認しています");}
    @Override public void onCancelled(GestureDescription g){if(generation!=testGeneration)return;gesturePending=false;if(running)stop("要確認","スライドが中断されました");}
   },handler);
   if(!accepted){gesturePending=false;stop("要確認","スライドを実行できません");return;}
   revealSearch.moved();status("要素探索",pair+"/6 · "+(step==RevealSearch.Step.UP?"フロートの高さ分だけ上へ":"従来の距離で下へ"));
  }
 }
 public void refreshTestMode(){stop("停止","入力テスト設定を変更しました");testQuestion="";hideFloat();showFloat();}
 public void copyDiagnostics(){try{((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("怪盗ザンギ入力診断",InputDiagnostics.read(this)));Toast.makeText(this,"診断結果をコピーしました",Toast.LENGTH_SHORT).show();}catch(RuntimeException e){Toast.makeText(this,"コピーできません。アプリの診断画面を開いてください",Toast.LENGTH_LONG).show();}}
 private boolean testUnlocked(){return !((KeyguardManager)getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked()&&((PowerManager)getSystemService(POWER_SERVICE)).isInteractive();}
 private boolean sameTest(Screen s){return testMode()&&testUnlocked()&&s.trusted()&&Store.allowed(this,s.pkg)&&s.pkg.equals(testPackage)&&!s.nodes.isEmpty()&&s.nodes.get(0).getWindowId()==testWindow&&s.currentFinal()&&s.visibleQuestion()&&s.question.key.equals(testQuestion)&&SystemClock.elapsedRealtime()-testCaptured<=120000;}
 private void testEnd(String code,String detail){cleanupClipboard();testBusy=false;testGeneration++;InputDiagnostics.add(this,code,"textChanged="+textChanged);floatSize(true);status(code,detail+"\n送信していません。次を試す前に入力欄を空にしてください。");}
 private void prepareInputTest(){
  running=false;testGeneration++;testBusy=false;testQuestion="";finalAnswerKnown=false;answer="";submitAttempted=false;inputVerified=false;
  if(!testUnlocked()){testEnd("S1_LOCKED","画面ロックを解除してください");return;}
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null){testEnd("S1_NO_LINE","LINEの第5問を表示してください");return;}
  try(Screen s=new Screen(root)){
   InputDiagnostics.capture(this,"TEST_BEFORE",s);
   if(!Store.allowed(this,s.pkg)||!s.trusted()||!s.currentFinal()||!s.visibleQuestion()){testEnd("S1_QUESTION_MISSING","超良問ドリルの第5問を表示してください");return;}
   current="第5問\n"+s.question.body;answer=MathEngine.solve(s.question.body).answer;finalAnswerKnown=true;
   if(!FinalSubmission.messageFormat(s.model)){testEnd("S1_GUIDE_MISSING","回答方法の案内と第5問を表示して、▶を押してください");return;}
   android.content.SharedPreferences prefs=Store.session(this,s.pkg);
   if(s.question.key.equals(prefs.getString("pending",""))||s.question.key.equals(prefs.getString("final_submit_key",""))){testEnd("S1_ALREADY_PENDING","送信記録があります。先にLINEの結果を確認してください");return;}
   testQuestion=s.question.key;testPackage=s.pkg;testWindow=s.nodes.get(0).getWindowId();testCaptured=SystemClock.elapsedRealtime();
   floatSize(true);InputDiagnostics.add(this,"S1_READY","question=5");status("入力テスト準備完了","再生1:直接入力 / 2:選択後入力 / 3:貼付\n入力だけ・送信なし。空の入力欄で試してください。");
  }catch(Exception e){testEnd("S1_ERROR",e.getClass().getSimpleName());}
 }
 private void runInputTest(int method){
  if(!testMode()||testBusy)return;
  if(testQuestion.isEmpty()||SystemClock.elapsedRealtime()-testCaptured>120000){prepareInputTest();if(testQuestion.isEmpty())return;}
  running=false;testBusy=true;textChanged=false;int generation=++testGeneration;updateFloatControls();InputDiagnostics.add(this,"TEST_START","method="+method);testLocate(method,generation,false);
 }
 private void testLocate(int method,int generation,boolean opened){
  if(generation!=testGeneration||!testBusy)return;
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null){testEnd("S1_NO_LINE","LINE画面を確認できません");return;}
  try(Screen s=new Screen(root)){
   if(!sameTest(s)){testEnd("S1_CONTEXT_CHANGED","問題または画面が変わりました。第5問を表示して▶で再確認してください");return;}
   InputDiagnostics.capture(this,opened?"TEST_AFTER_OPEN":"TEST_BEFORE_INPUT",s);
   java.util.List<AccessibilityNodeInfo> inputs=s.testComposers();
   InputDiagnostics.add(this,"S3_IME","visible="+keyboardVisible());
   InputDiagnostics.add(this,"S4_CANDIDATES","count="+inputs.size());
   if(inputs.size()!=1){
    AccessibilityNodeInfo opener=s.keyboardButton();
    if(inputs.isEmpty()&&!opened&&opener!=null&&opener.refresh()&&opener.isVisibleToUser()&&opener.isEnabled()&&opener.isClickable()){
     boolean clicked=opener.performAction(AccessibilityNodeInfo.ACTION_CLICK);InputDiagnostics.add(this,"S2_OPEN","click="+clicked);
     if(clicked){status("S2_OPEN","入力欄を開いています。送信はしません。");handler.postDelayed(()->testLocate(method,generation,true),600);return;}
    }
    testEnd("S4_INPUT_COUNT","入力欄候補="+inputs.size()+"。入力欄を手動で開いて再試行してください");return;
   }
   AccessibilityNodeInfo input=inputs.get(0);
   if(!Screen.inputValue(input).isEmpty()){testEnd("S4_DRAFT_PRESENT","既存の文字を保護しました。ご自身で空にしてから試してください");return;}
   if(method==1){testWrite(method,generation);return;}
   boolean clicked=InputDiagnostics.action(input,AccessibilityNodeInfo.ACTION_CLICK)&&input.performAction(AccessibilityNodeInfo.ACTION_CLICK);
   boolean focused=input.isFocused()||(InputDiagnostics.action(input,AccessibilityNodeInfo.ACTION_FOCUS)&&input.performAction(AccessibilityNodeInfo.ACTION_FOCUS));
   InputDiagnostics.add(this,"S4_FOCUS","click="+clicked+" focus="+focused);
   handler.postDelayed(()->testWrite(method,generation),250);
  }catch(Exception e){testEnd("S4_ERROR",e.getClass().getSimpleName());}
 }
 private void testWrite(int method,int generation){
  if(generation!=testGeneration||!testBusy)return;
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null){testEnd("S1_NO_LINE","LINE画面を確認できません");return;}
  try(Screen s=new Screen(root)){
   if(!sameTest(s)){testEnd("S1_CONTEXT_CHANGED","入力直前の問題照合に失敗しました");return;}
   java.util.List<AccessibilityNodeInfo> inputs=s.testComposers();if(inputs.size()!=1){testEnd("S4_INPUT_COUNT","入力直前の候補数="+inputs.size());return;}
   AccessibilityNodeInfo input=inputs.get(0);if(!input.refresh()||!input.isVisibleToUser()||!input.isEnabled()||!input.isEditable()||input.isPassword()||!Screen.inputValue(input).isEmpty()){testEnd("S4_DRAFT_OR_CHANGED","入力欄が変化したため操作しません");return;}
   int action=method==3?AccessibilityNodeInfo.ACTION_PASTE:AccessibilityNodeInfo.ACTION_SET_TEXT;
   if(!InputDiagnostics.action(input,action)){testEnd("S5_UNSUPPORTED","選んだ入力方式に対応していません");return;}
   if(method==3&&!input.isFocused()){testEnd("S5_NOT_FOCUSED","貼付先のフォーカスを確認できません");return;}
   ClipboardManager clipboard=null;ClipData oldClip=null;String clipToken="zangi-test-"+SystemClock.elapsedRealtime();boolean clipWritten=false;
   try{
    if(method==3){clipboard=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);oldClip=clipboard.getPrimaryClip();if(oldClip==null){testEnd("S5_CLIP_UNREADABLE","元のクリップボードを退避できません");return;}clipboard.setPrimaryClip(ClipData.newPlainText(clipToken,answer));clipWritten=true;}
    Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,answer);
    boolean result=input.performAction(action,method==3?null:args);InputDiagnostics.add(this,"S5_ACTION","method="+method+" returned="+result);
    // Read even on false: API return value and actual visible change are separate evidence.
    final boolean returned=result;handler.postDelayed(()->testRead(generation,200,returned),200);handler.postDelayed(()->testRead(generation,600,returned),600);
   }finally{
    if(clipWritten){final ClipboardManager cm=clipboard;final ClipData saved=oldClip;clipboardCleanup=()->restoreTestClipboard(cm,saved,clipToken);handler.postDelayed(clipboardCleanup,700);}
   }
  }catch(Exception e){testEnd("S5_ERROR",e.getClass().getSimpleName());}
 }
 private void restoreTestClipboard(ClipboardManager cm,ClipData saved,String token){
  try{ClipData now=cm.getPrimaryClip();if(now!=null&&token.contentEquals(now.getDescription().getLabel())){if(saved!=null)cm.setPrimaryClip(saved);else if(Build.VERSION.SDK_INT>=28)cm.clearPrimaryClip();else cm.setPrimaryClip(ClipData.newPlainText("",""));InputDiagnostics.add(this,"CLIPBOARD_RESTORED","ok");}else InputDiagnostics.add(this,"CLIPBOARD_CHANGED","preserved newer clipboard");}catch(Exception e){InputDiagnostics.add(this,"CLIPBOARD_RESTORE_FAILED",e.getClass().getSimpleName());Toast.makeText(this,"クリップボードを戻せませんでした",Toast.LENGTH_LONG).show();}
 }
 private void testRead(int generation,int delay,boolean returned){
  if(generation!=testGeneration||!testBusy)return;
  AccessibilityNodeInfo root=foregroundLineRoot();if(root==null){testEnd("S6_NO_LINE","読み戻し時にLINE画面を確認できません");return;}
  try(Screen s=new Screen(root)){
   if(!sameTest(s)){testEnd("S6_CONTEXT_CHANGED","読み戻し時に問題または画面が変わりました");return;}
   java.util.List<AccessibilityNodeInfo> inputs=s.testComposers();boolean refreshed=inputs.size()==1&&inputs.get(0).refresh();String value=refreshed?Screen.inputValue(inputs.get(0)):"";boolean match=refreshed&&answer.equals(value);
   InputDiagnostics.add(this,"S6_READBACK","delayMs="+delay+" candidates="+inputs.size()+" refresh="+refreshed+" length="+value.length()+" matches="+match+" textChanged="+textChanged);
   if(delay==600){InputDiagnostics.capture(this,"TEST_AFTER_INPUT",s);testEnd(match?(returned?"S6_MATCH":"S6_MATCH_API_FALSE"):"S6_MISMATCH",match?"答えと入力内容が一致しました":"入力した数字を確認できませんでした");}
  }catch(Exception e){testEnd("S6_ERROR",e.getClass().getSimpleName());}
 }

 @Override public void onInterrupt(){stop("要確認","アクセシビリティ処理が中断されました");}
 @Override public void onDestroy(){cleanupClipboard();running=false;handler.removeCallbacksAndMessages(null);hideFloat();instance=null;Store.prefs(this).edit().putString("state","権限未接続").apply();super.onDestroy();}
}
