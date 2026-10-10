package jp.kossacktouch.app;
import android.content.*;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;
import jp.kossacktouch.core.*;
public final class Store {
 public static final String VERSION="0.1.13-name14";
 public static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("local",Context.MODE_PRIVATE);}
 public static void log(Context c,String state,String question,String answer,String detail){
  try{JSONArray old=new JSONArray(prefs(c).getString("history","[]")),arr=new JSONArray();JSONObject item=new JSONObject();item.put("time",new SimpleDateFormat("MM/dd HH:mm:ss",Locale.JAPAN).format(new Date()));item.put("state",state);item.put("question",question);item.put("answer",answer);item.put("detail",detail);arr.put(item);for(int i=0;i<Math.min(old.length(),199);i++)arr.put(old.get(i));prefs(c).edit().putString("history",arr.toString()).apply();}catch(JSONException ignored){}
 }
 public static AnswerBank bank(Context c)throws Exception{
  java.io.InputStream in=c.getAssets().open("answers.json");java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);in.close();JSONArray a=new JSONObject(out.toString("UTF-8")).getJSONArray("questions");List<AnswerBank.Entry> e=new ArrayList<>();for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);String full=x.optString("question_completeness").equals("full")?x.getString("question"):"";
   AnswerBank.Entry item=new AnswerBank.Entry(x.getString("id"),x.getString("stem"),x.getString("answer"),strings(x.getJSONArray("answer_tokens")),x.getBoolean("enabled"),full);
   item.keywords=groups(x.optJSONArray("question_keywords"));item.context=groups(x.optJSONArray("context_keywords"));item.answerGroups=groups(x.optJSONArray("answer_keyword_groups"));item.aliases=strings(x.optJSONArray("answer_aliases"));item.sourceLetter=x.getString("source_letter");e.add(item);
  }return new AnswerBank(e);
 }
 private static List<String> strings(JSONArray a)throws JSONException{List<String> result=new ArrayList<>();if(a!=null)for(int i=0;i<a.length();i++)result.add(a.getString(i));return result;}
 private static List<List<String>> groups(JSONArray a)throws JSONException{List<List<String>> result=new ArrayList<>();if(a!=null)for(int i=0;i<a.length();i++)result.add(strings(a.getJSONArray(i)));return result;}
 public static boolean allowed(Context c,String pkg){return LineIdentity.candidate(pkg);}
 public static android.content.SharedPreferences session(Context c,String pkg){return c.getSharedPreferences("line_"+pkg,Context.MODE_PRIVATE);}
 public static void migrateSession(Context c,String pkg){
  android.content.SharedPreferences old=prefs(c),scoped=session(c,pkg);
  if(old.getBoolean("session_migrated",false))return;
  String owner=old.getString("pending_package","");
  // Legacy unscoped records are retained until an explicit result review; never silently re-send.
  if(owner.isEmpty()||owner.equals(pkg)){
   android.content.SharedPreferences.Editor e=scoped.edit();for(String k:new String[]{"pending","registration_pending","registration_prior_question"})if(!scoped.contains(k))e.putString(k,old.getString(k,""));
   if(e.commit())old.edit().putBoolean("session_migrated",true).commit();
  }
 }
}
