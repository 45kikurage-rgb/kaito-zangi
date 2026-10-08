package jp.kossacktouch.core;
import java.util.*;
public final class AnswerBank {
 public static final class Entry {public String id,stem,answer,full;public List<String> tokens;public boolean enabled;public Entry(String id,String s,String a,List<String> t,boolean e){this(id,s,a,t,e,"");}public Entry(String id,String s,String a,List<String> t,boolean e,String f){this.id=id;stem=s;answer=a;tokens=t;enabled=e;full=f;}}
 private final List<Entry> entries;
 public AnswerBank(List<Entry> e){entries=e;Set<String> ids=new HashSet<>(),stems=new HashSet<>();for(Entry x:e)if(!ids.add(x.id)||!stems.add(Text.norm(x.stem)))throw new IllegalArgumentException("正答データ重複");}
 public String match(Question q){List<Entry> candidates=new ArrayList<>();String body=Text.norm(q.body);
  for(Entry e:entries)if(e.enabled&&(e.full.isEmpty()?body.startsWith(Text.norm(e.stem)):body.equals(Text.norm(e.full))))candidates.add(e);
  if(candidates.size()!=1)throw new IllegalArgumentException("正答集で一意に照合できません");Entry e=candidates.get(0);
  List<String> letters=new ArrayList<>();for(Map.Entry<String,String> c:q.choices.entrySet()){String value=Text.norm(c.getValue());boolean yes=true;for(String token:e.tokens)if(!value.contains(Text.norm(token)))yes=false;
   if(e.tokens.size()==1&&Text.norm(e.answer).length()<=10&&!value.equals(Text.norm(e.answer))&&!value.equals(Text.norm(e.answer)+"級"))yes=false;
   if(yes)letters.add(c.getKey());}
  if(letters.size()!=1)throw new IllegalArgumentException("正答と選択肢の照合が不一致です");return letters.get(0);
 }
 public int size(){return entries.size();}
}
