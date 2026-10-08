package jp.kossacktouch.app;
import android.content.*;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;
import jp.kossacktouch.core.*;
public final class Store {
 public static final String VERSION="0.1.0-test01";
 public static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("local",Context.MODE_PRIVATE);}
 public static void log(Context c,String state,String question,String answer,String detail){
  try{JSONArray old=new JSONArray(prefs(c).getString("history","[]")),arr=new JSONArray();JSONObject item=new JSONObject();item.put("time",new SimpleDateFormat("MM/dd HH:mm:ss",Locale.JAPAN).format(new Date()));item.put("state",state);item.put("question",question);item.put("answer",answer);item.put("detail",detail);arr.put(item);for(int i=0;i<Math.min(old.length(),199);i++)arr.put(old.get(i));prefs(c).edit().putString("history",arr.toString()).apply();}catch(JSONException ignored){}
 }
 public static AnswerBank bank(Context c)throws Exception{
  java.io.InputStream in=c.getAssets().open("answers.json");java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);in.close();JSONArray a=new JSONObject(out.toString("UTF-8")).getJSONArray("questions");List<AnswerBank.Entry> e=new ArrayList<>();for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);List<String> t=new ArrayList<>();JSONArray tokens=x.getJSONArray("answer_tokens");for(int k=0;k<tokens.length();k++)t.add(tokens.getString(k));String full=x.optString("question_completeness").equals("full")?x.getString("question"):"";e.add(new AnswerBank.Entry(x.getString("id"),x.getString("stem"),x.getString("answer"),t,x.getBoolean("enabled"),full));}return new AnswerBank(e);
 }
 public static boolean allowed(Context c,String pkg){String csv=prefs(c).getString("packages","jp.naver.line.android,jp.naver.line.androie");for(String p:csv.split(","))if(p.trim().equals(pkg))return true;return false;}
}
