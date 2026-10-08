package jp.kossacktouch.core;
import java.util.*;
/** AND between keyword groups, OR within each group. No fuzzy matching or letter lookup. */
public final class AnswerBank {
 public static final class Entry {
  public String id,stem,answer,full,sourceLetter="";public List<String> tokens;public boolean enabled;
  public List<List<String>> keywords=new ArrayList<>(),context=new ArrayList<>(),answerGroups=new ArrayList<>();
  public List<String> aliases=new ArrayList<>();
  public Entry(String id,String s,String a,List<String> t,boolean e){this(id,s,a,t,e,"");}
  public Entry(String id,String s,String a,List<String> t,boolean e,String f){this.id=id;stem=s;answer=a;tokens=t;enabled=e;full=f;}
 }
 private final List<Entry> entries;
 public AnswerBank(List<Entry> e){entries=e;Set<String> ids=new HashSet<>(),stems=new HashSet<>();for(Entry x:e)if(!ids.add(x.id)||!stems.add(Text.norm(x.stem)))throw new IllegalArgumentException("正答データ重複");}
 public static boolean containsGroups(String body,List<List<String>> groups){
  String normalized=Text.norm(body);
  for(List<String> group:groups){boolean found=false;for(String word:group)if(containsWord(normalized,Text.norm(word)))found=true;if(!found)return false;}
  return true;
 }
 private static boolean containsWord(String body,String word){
  if(word.isEmpty())return false;String pattern=java.util.regex.Pattern.quote(word);
  if(Character.isDigit(word.charAt(0)))pattern="(?<![0-9])"+pattern;
  if(Character.isDigit(word.charAt(word.length()-1)))pattern+="(?![0-9])";
  return java.util.regex.Pattern.compile(pattern).matcher(body).find();
 }
 public String match(Question q){
  List<Entry> candidates=new ArrayList<>();String body=Text.norm(q.body);
  for(Entry e:entries){
   boolean matches=e.keywords.isEmpty()?(e.full.isEmpty()?body.startsWith(Text.norm(e.stem)):body.equals(Text.norm(e.full))):containsGroups(body,e.keywords);
   if(e.enabled&&matches)candidates.add(e);
  }
  if(candidates.size()!=1)throw new IllegalArgumentException("正答集で一意に照合できません");
  Entry e=candidates.get(0);
  if(!containsGroups(body,e.context))throw new IllegalArgumentException("登録済み問題の条件と一致しません");
  if(!e.sourceLetter.matches("[A-D]")||!q.choices.containsKey(e.sourceLetter))throw new IllegalArgumentException("登録済みの正答ボタンを確認できません");
  return e.sourceLetter;
 }
 public int size(){return entries.size();}
}
