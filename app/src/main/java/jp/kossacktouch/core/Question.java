package jp.kossacktouch.core;
import java.util.*;
import java.util.regex.*;
public final class Question {
 public final int number;public final String body;public final Map<String,String> choices;public final String key;
 public Question(int n,String body,Map<String,String> c){number=n;this.body=body.trim();choices=c;key=Text.norm(n+":"+body+":"+c.toString());}
 public static Question parse(String text){
  Matcher h=Pattern.compile("第\\s*([1-5１-５])\\s*問").matcher(text);if(!h.find())return null;
  int n=Integer.parseInt(Text.clean(h.group(1)));String remainder=text.substring(h.end());remainder=remainder.replaceFirst("^\\s*[(（][^\\n)）]*[)）]","").trim();
  Matcher m=Pattern.compile("[【\\[]([A-DＡ-Ｄ])[】\\]]\\s*([^\\n]+)").matcher(remainder);Map<String,String> choices=new LinkedHashMap<>();int first=remainder.length();while(m.find()){if(choices.isEmpty())first=m.start();String letter=Text.clean(m.group(1));if(choices.containsKey(letter))return null;choices.put(letter,m.group(2).trim());}
  if(n<5&&choices.size()!=4)return null;
  return new Question(n,remainder.substring(0,first),choices);
 }
}
