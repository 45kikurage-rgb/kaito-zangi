package jp.kossacktouch.app;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.*;
import jp.kossacktouch.core.*;

public final class MainActivity extends Activity {
 private LinearLayout body;private TextView state,question,answer;private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable refresh=new Runnable(){public void run(){update();handler.postDelayed(this,1000);}};
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(16,43,53));getWindow().setNavigationBarColor(Color.rgb(16,43,53));getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);build();}
 @Override public void onResume(){super.onResume();handler.post(refresh);}
 @Override public void onPause(){handler.removeCallbacks(refresh);super.onPause();}
 private void build(){ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(20),dp(28),dp(20),dp(36));body.setBackgroundColor(Color.rgb(247,248,244));scroll.addView(body);setContentView(scroll);scroll.setOnApplyWindowInsetsListener((v,insets)->{body.setPadding(dp(20),dp(28)+insets.getSystemWindowInsetTop(),dp(20),dp(36)+insets.getSystemWindowInsetBottom());return insets;});
  ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.quiz_knockout);logo.setContentDescription("怪盗ザンギのロゴ");body.addView(logo,new LinearLayout.LayoutParams(dp(92),dp(92)));
  title("怪盗ザンギ",28);label("超良問ドリル · テスト版 "+Store.VERSION+" / code 16（自動化改修候補）",13);label("この端末だけで問題を確認・計算します。\n第1〜4問は自動回答。第5問は回答形式と数字を確認しLINEの送信まで進め、クーポン表示ボタンを探索します。不明な画面では停止します。",15);
  state=label("待機",20);question=label("認識した問題がここに表示されます",16);answer=label("答え: —",22);
  button("スタート · フロートを表示",()->{if(QuizService.instance==null){guide();return;}if(!Store.prefs(this).getBoolean("disclosure_registration_v2",false)){new AlertDialog.Builder(this).setTitle("操作の許可について").setMessage("怪盗ザンギはアクセシビリティで、自動認識したLINEの超良問ドリル画面の問題文と回答ボタンを読み取り、利用者が再生した時に操作します。初回参加登録では既存情報を変更せず、都道府県・高校は表示中の候補からランダムに選びます。生年・性別・規約同意は初期状態で本人操作です。事前承認をこの端末で有効化すると、生年・性別を含む登録候補を無作為に選び、既知の「参加規約に同意して進む」を自動操作します。実際の本人情報と異なる選択が発生し、応募資格に影響する可能性があります。未知の同意画面は操作しません。第1〜4問は専用ボタンを押し、第5問は計算数字を検証してLINEの数字入力・送信ボタンを自動操作します。正解後にクーポンを取得・表示するボタンを操作します。意図しない下書きや不明な画面を確認したら停止します。問題・回答・エラー履歴はこの端末に保存します。認証情報や会話を外部へ送信しません。停止ボタンでいつでも止められます。").setNegativeButton("取消",null).setPositiveButton("確認して表示",(d,w)->{Store.prefs(this).edit().putBoolean("disclosure_registration_v2",true).apply();QuizService.instance.showFloat();}).show();}else QuizService.instance.showFloat();});
  button("初回アンケート・登録の事前承認設定",this::registrationApproval);
  button("停止",()->{if(QuizService.instance!=null)QuizService.instance.stop("停止","アプリから停止しました");});
  title("はじめての設定",19);label("① 下の権限設定を開き、怪盗ザンギをON。\n② スタートを押してフロートを表示。\n③ LINEの超良問ドリルを開く。\n④ 現在の画面でフロートの▶再生を押す。\n⑤ 第1〜4問は自動で回答・進行します。第5問は数字を自動入力し、入力内容を確認して送信します。正解後はクーポン関連ボタンを操作します。未確認の画面では停止します。\n\nフロートの「怪盗ザンギ」をドラッグすると移動でき、位置を端末内へ保存します。再生前は小型、再生中は全幅・高さ約3倍に拡大します。初回登録画面も自動で進みます。既存情報の「変更する」は選びません。都道府県・高校は見えている選択肢からランダムに選び、スクロールしません。生年・性別・規約同意は初期設定では停止します。事前承認を端末ごとに有効化した場合だけ、表示中の生年・性別候補を無作為に選び、既知の参加規約同意ボタンを操作します。規約や本人情報が変更された場合は設定を見直してください。実機では未検証です。",15);
  button("アクセシビリティ権限の設定",this::guide);
  label("標準LINE・複製LINEは自動認識します。アカウント切替後は対象トークで▶再生。package名の入力は不要です。",14);
  button("回答・エラー履歴",this::history);
  button("計算エンジンで問題文を確認",this::calculator);
  button("前回の未確認記録を解除",()->new AlertDialog.Builder(this).setTitle("LINEの回答結果を確認しましたか？").setMessage("未確認のまま解除すると同じ問題を再操作できる場合があります。現在のLINE画面で結果を確認し、次の未回答問題を表示した後に解除してください。解除しても自動で再生しません。").setNegativeButton("取消",null).setPositiveButton("確認したので解除",(d,w)->{if(QuizService.instance!=null)QuizService.instance.stop("停止","未確認記録解除のため停止");String pkg=Store.prefs(this).getString("active_line_package","");if(!pkg.isEmpty())Store.session(this,pkg).edit().clear().commit();Store.prefs(this).edit().remove("pending").remove("registration_pending").remove("registration_prior_question").apply();Store.log(this,"手動確認","","","利用者が未確認記録を解除");}).show());
  title("安全停止",19);label("知らない問題、条件不明、権限解除、画面OFF、LINE以外の画面では停止します。第5問は計算結果をLINEの入力欄へ直接設定し、入力文字列が完全一致したときのみ送信します。別の下書き、送信先不明、送信操作結果不明は二重送信せず停止します。クーポンページが検証できない場合も完了扱いにせず停止します。要確認時は問題と計算結果を確認してください。画面要素が取れない場合のOCRは、このテスト版では未搭載です。",14);
 }
 private TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(24,47,55));t.setPadding(0,dp(8),0,dp(10));t.setTextIsSelectable(true);body.addView(t);return t;}
 private void title(String s,int size){TextView t=label(s,size);t.setTypeface(null,Typeface.BOLD);}
 private void button(String s,Runnable action){Button b=new Button(this);b.setText(s);b.setTextSize(15);b.setAllCaps(false);b.setMinHeight(dp(54));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));body.addView(b,p);b.setOnClickListener(v->action.run());}
 private void update(){if(state==null)return;android.content.SharedPreferences p=Store.prefs(this);state.setText((QuizService.instance==null?"権限未接続":p.getString("state","待機"))+"\n"+p.getString("detail",""));String q=p.getString("question","");question.setText(q.isEmpty()?"認識した問題がここに表示されます":q);answer.setText("答え: "+p.getString("answer","—"));}
 private void guide(){new AlertDialog.Builder(this).setTitle("権限設定").setMessage("Androidの設定 → ユーザー補助 → ダウンロードしたアプリ → 怪盗ザンギ → 使用をON。\n\n『制限付き設定』が表示された場合は、設定 → アプリ → 怪盗ザンギ → 右上の︙ → 制限付き設定を許可。その後ユーザー補助設定を開き直してください。端末により名称が異なります。").setNegativeButton("閉じる",null).setPositiveButton("設定を開く",(d,w)->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).show();}

 private void registrationApproval(){
  boolean approved=Store.prefs(this).getBoolean("registration_preapproved_v1",false);
  new AlertDialog.Builder(this).setTitle("初回登録の自動進行 · "+(approved?"ON":"OFF"))
   .setMessage("この端末の超良問ドリル初回登録について、表示された選択肢を無作為に選びます。生年・性別・都道府県・高校は事実と異なる内容になる場合があり、応募条件・景品受取に影響する可能性があります。\n\n同意操作は認識済みの「参加規約に同意して進む」だけです。新しい条件や不明な画面での同意を保証しません。条件を確認し、了承できる場合にONにしてください。\n\nこの設定は端末内で保存され、いつでも解除できます。他の端末や配布先には引き継がれません。")
   .setNegativeButton("閉じる",null)
   .setNeutralButton("承認を解除", (d,w)->{if(QuizService.instance!=null)QuizService.instance.stop("停止","事前承認の設定を変更しました");Store.prefs(this).edit().putBoolean("registration_preapproved_v1",false).apply();})
   .setPositiveButton("了承してON", (d,w)->{if(QuizService.instance!=null)QuizService.instance.stop("停止","事前承認の設定を変更しました");Store.prefs(this).edit().putBoolean("registration_preapproved_v1",true).apply();})
   .show();
 }
 private void history(){StringBuilder s=new StringBuilder();try{JSONArray a=new JSONArray(Store.prefs(this).getString("history","[]"));for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);s.append(x.getString("time")).append(" · ").append(x.getString("state")).append('\n').append(x.getString("question")).append('\n').append("答え: ").append(x.getString("answer")).append('\n').append(x.getString("detail")).append("\n\n");}}catch(Exception ignored){}TextView text=new TextView(this);text.setPadding(dp(18),dp(12),dp(18),dp(12));text.setText(s.length()==0?"履歴はありません":s.toString());text.setTextIsSelectable(true);ScrollView v=new ScrollView(this);v.addView(text);new AlertDialog.Builder(this).setTitle("回答・エラー履歴（最大200件）").setView(v).setPositiveButton("閉じる",null).setNeutralButton("履歴削除",(d,w)->Store.prefs(this).edit().remove("history").apply()).show();}
 private void calculator(){EditText input=new EditText(this);input.setMinLines(5);input.setHint("第5問の問題文を貼り付け");new AlertDialog.Builder(this).setTitle("端末内の計算を確認").setView(input).setNegativeButton("取消",null).setPositiveButton("計算",(d,w)->{String result;try{MathEngine.Result r=MathEngine.solve(input.getText().toString());result="答え: "+r.answer+"\n形式: "+r.type+"\n"+r.detail;}catch(Exception ex){result="要確認: "+ex.getMessage();}new AlertDialog.Builder(this).setTitle("計算結果").setMessage(result+"\n\nここでの計算はLINEを操作しません。").setPositiveButton("閉じる",null).show();}).show();}
}
