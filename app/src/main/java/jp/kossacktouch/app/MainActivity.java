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
  title("コサックタッチ",28);label("超良問ドリル · テスト版 "+Store.VERSION+" / code 1",13);label("この端末だけで問題を確認・計算します。\n実機での5問連続動作は未検証です。",15);
  state=label("待機",20);question=label("認識した問題がここに表示されます",16);answer=label("答え: —",22);
  button("スタート · フロートを表示",()->{if(QuizService.instance==null){guide();return;}if(!Store.prefs(this).getBoolean("disclosure",false)){new AlertDialog.Builder(this).setTitle("操作の許可について").setMessage("コサックタッチはアクセシビリティで、許可したLINEの超良問ドリル画面の問題文と回答ボタンを読み取り、利用者が再生した時に操作します。初回参加登録では既存情報を変更せず、新規項目をランダムに選び、参加規約に同意して進みます。問題・回答・エラー履歴はこの端末に保存します。認証情報や会話を外部へ送信しません。停止ボタンでいつでも止められます。").setNegativeButton("取消",null).setPositiveButton("確認して表示",(d,w)->{Store.prefs(this).edit().putBoolean("disclosure",true).apply();QuizService.instance.showFloat();}).show();}else QuizService.instance.showFloat();});
  button("停止",()->{if(QuizService.instance!=null)QuizService.instance.stop("停止","アプリから停止しました");});
  title("はじめての設定",19);label("① 下の権限設定を開き、コサックタッチをON。\n② スタートを押してフロートを表示。\n③ LINEの超良問ドリルで問題を表示。\n④ フロートの▶再生を押す。\n⑤ 完了後は待機に戻ります。\n\nフロート上部をドラッグすると移動できます。初回登録画面も自動で進みます。既存情報の「変更する」は選びません。新しい登録項目は表示された選択肢からランダムに選び、参加規約に同意して登録します。実機では未検証です。",15);
  button("アクセシビリティ権限の設定",this::guide);
  button("対応するLINEの設定",this::packages);
  button("回答・エラー履歴",this::history);
  button("計算エンジンで問題文を確認",this::calculator);
  button("前回の未確認記録を解除",()->new AlertDialog.Builder(this).setTitle("LINEの回答結果を確認しましたか？").setMessage("未確認のまま解除すると同じ問題を再操作できる場合があります。現在のLINE画面で結果を確認し、次の未回答問題を表示した後に解除してください。解除しても自動で再生しません。").setNegativeButton("取消",null).setPositiveButton("確認したので解除",(d,w)->{if(QuizService.instance!=null)QuizService.instance.stop("停止","未確認記録解除のため停止");Store.prefs(this).edit().remove("pending").remove("registration_pending").remove("registration_prior_question").apply();Store.log(this,"手動確認","","","利用者が未確認記録を解除");}).show());
  title("安全停止",19);label("知らない問題、条件不明、権限解除、画面OFF、LINE以外の画面では停止します。通常のLINE送信ボタンは操作しません。要確認時は問題と計算結果を確認してください。画面要素が取れない場合のOCRは、このテスト版では未搭載です。",14);
 }
 private TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(24,47,55));t.setPadding(0,dp(8),0,dp(10));t.setTextIsSelectable(true);body.addView(t);return t;}
 private void title(String s,int size){TextView t=label(s,size);t.setTypeface(null,Typeface.BOLD);}
 private void button(String s,Runnable action){Button b=new Button(this);b.setText(s);b.setTextSize(15);b.setAllCaps(false);b.setMinHeight(dp(54));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));body.addView(b,p);b.setOnClickListener(v->action.run());}
 private void update(){if(state==null)return;android.content.SharedPreferences p=Store.prefs(this);state.setText((QuizService.instance==null?"権限未接続":p.getString("state","待機"))+"\n"+p.getString("detail",""));String q=p.getString("question","");question.setText(q.isEmpty()?"認識した問題がここに表示されます":q);answer.setText("答え: "+p.getString("answer","—"));}
 private void guide(){new AlertDialog.Builder(this).setTitle("権限設定").setMessage("Androidの設定 → ユーザー補助 → ダウンロードしたアプリ → コサックタッチ → 使用をON。\n\n『制限付き設定』が表示された場合は、設定 → アプリ → コサックタッチ → 右上の︙ → 制限付き設定を許可。その後ユーザー補助設定を開き直してください。端末により名称が異なります。").setNegativeButton("閉じる",null).setPositiveButton("設定を開く",(d,w)->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).show();}
 private void packages(){EditText e=new EditText(this);e.setText(Store.prefs(this).getString("packages","jp.naver.line.android,jp.naver.line.androie"));e.setSingleLine(false);new AlertDialog.Builder(this).setTitle("許可するLINEのpackage名").setMessage("標準LINEと資料で確認した複製版のみ初期許可しています。別の複製LINEを使う場合は、その正確なpackage名をカンマ区切りで登録してください。jp.naver.line. で始まるものに限定します。超良問ドリルのトーク名も照合します。").setView(e).setNegativeButton("取消",null).setPositiveButton("保存",(d,w)->{String value=e.getText().toString().trim();String[] parts=value.split(",");if(parts.length==0||parts.length>300){Toast.makeText(this,"登録数を確認してください",0).show();return;}for(String p:parts)if(!p.trim().matches("jp\\.naver\\.line\\.[A-Za-z0-9_.]+")){Toast.makeText(this,"LINEの正確なpackage名を入力してください",0).show();return;}if(QuizService.instance!=null)QuizService.instance.stop("停止","LINEの許可設定を変更しました");Store.prefs(this).edit().putString("packages",value).apply();}).show();}
 private void history(){StringBuilder s=new StringBuilder();try{JSONArray a=new JSONArray(Store.prefs(this).getString("history","[]"));for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);s.append(x.getString("time")).append(" · ").append(x.getString("state")).append('\n').append(x.getString("question")).append('\n').append("答え: ").append(x.getString("answer")).append('\n').append(x.getString("detail")).append("\n\n");}}catch(Exception ignored){}TextView text=new TextView(this);text.setPadding(dp(18),dp(12),dp(18),dp(12));text.setText(s.length()==0?"履歴はありません":s.toString());text.setTextIsSelectable(true);ScrollView v=new ScrollView(this);v.addView(text);new AlertDialog.Builder(this).setTitle("回答・エラー履歴（最大200件）").setView(v).setPositiveButton("閉じる",null).setNeutralButton("履歴削除",(d,w)->Store.prefs(this).edit().remove("history").apply()).show();}
 private void calculator(){EditText input=new EditText(this);input.setMinLines(5);input.setHint("第5問の問題文を貼り付け");new AlertDialog.Builder(this).setTitle("端末内の計算を確認").setView(input).setNegativeButton("取消",null).setPositiveButton("計算",(d,w)->{String result;try{MathEngine.Result r=MathEngine.solve(input.getText().toString());result="答え: "+r.answer+"\n形式: "+r.type+"\n"+r.detail;}catch(Exception ex){result="要確認: "+ex.getMessage();}new AlertDialog.Builder(this).setTitle("計算結果").setMessage(result+"\n\nここでの計算はLINEを操作しません。").setPositiveButton("閉じる",null).show();}).show();}
}
