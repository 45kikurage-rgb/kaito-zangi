package jp.kossacktouch.core;
import java.text.Normalizer;
import java.util.regex.*;
/** OCR is only a text source; strict registered matching/local arithmetic remain authoritative. */
public final class CameraAnswer {
 public static final class Reading {
  public final String answer,detail;public final int number;
  Reading(String a,String d,int n){answer=a;detail=d;number=n;}
 }
 private final AnswerBank bank;private String previous="";private long observed=-1;
 public CameraAnswer(AnswerBank bank){this.bank=bank;}
 public void reset(){previous="";observed=-1;}
 public Reading observe(String raw,long now){
  if(observed>=0&&now-observed<1000)return new Reading("","次の確認を待っています",0);
  String text=Normalizer.normalize(raw==null?"":raw,Normalizer.Form.NFKC);
  Matcher headers=Pattern.compile("第\\s*([0-9]+)\\s*問").matcher(text);
  int start=-1,count=0;while(headers.find()){start=headers.start();count++;}
  if(count!=1){reset();return new Reading("",count>1?"1問だけを映してください":"第○問と問題文を映してください",0);}
  Question q=Question.parse(text.substring(start));
  if(q==null||q.body.isEmpty()||(q.number==5&&!q.choices.isEmpty())){reset();return new Reading("","問題全体とA〜Dを映してください",0);}
  // Preserve all punctuation and numbers; only whitespace/full-width variation is tolerated.
  String key=text.substring(start).replaceAll("[\\s\\p{Z}]","");
  boolean stable=key.equals(previous)&&observed>=0&&now-observed<=2500;
  previous=key;observed=now;
  if(!stable)return new Reading("","第"+q.number+"問を確認中（2回一致で表示）",q.number);
  try{
   if(q.number==5){
    if(!Text.clean(q.body).matches("(?s).*(?:求めよ|答えよ|か|[?？])[。.!！?？]*$"))return new Reading("","問題文の末尾まで映してください",5);
    MathEngine.Result r=MathEngine.solve(q.body);return new Reading(r.answer,"第5問 · "+r.type+"\n"+r.detail+"\n入力・送信は本人操作",5);
   }
   return new Reading(bank.match(q),"第"+q.number+"問 · 正答表で照合 · 回答は本人操作",q.number);
  }catch(IllegalArgumentException e){return new Reading("","要確認: "+e.getMessage(),q.number);}
 }
}
