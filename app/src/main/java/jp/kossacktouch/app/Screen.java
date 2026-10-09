package jp.kossacktouch.app;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;
import jp.kossacktouch.core.*;
/** Ephemeral snapshot of the foreground LINE tree. Never serialized as conversation content. */
public final class Screen implements AutoCloseable {
 public final List<AccessibilityNodeInfo> nodes=new ArrayList<>();
 public final List<String> flex=new ArrayList<>();
 public Question question;public int qRow=-1;public String latest="",pkg="";public boolean title=false,overflow=false;public UiModel model;
 private final List<UiModel.Node> snapshot=new ArrayList<>();
 public Screen(AccessibilityNodeInfo root){if(root==null)return;pkg=String.valueOf(root.getPackageName());walk(root,0,-1);model=new UiModel(snapshot);question=model.question;qRow=model.qRow;latest=model.latest;title=model.title;flex.addAll(model.flex);}
 private void walk(AccessibilityNodeInfo n,int depth,int parent){if(depth>45||nodes.size()>2500){overflow=true;n.recycle();return;}int index=nodes.size();nodes.add(n);snapshot.add(new UiModel.Node(parent,id(n),text(n),n.isVisibleToUser(),n.isEnabled(),n.isClickable()));for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo child=n.getChild(i);if(child!=null)walk(child,depth+1,index);}}
 public static String id(AccessibilityNodeInfo n){String x=n.getViewIdResourceName();return x==null?"":x;}
 public static String text(AccessibilityNodeInfo n){CharSequence t=n.getText();if(t==null||t.length()==0)t=n.getContentDescription();return t==null?"":t.toString();}
 public String content(AccessibilityNodeInfo parent){StringBuilder b=new StringBuilder();Set<String> seen=new HashSet<>();collect(parent,b,seen,0);return b.toString();}
 private void collect(AccessibilityNodeInfo n,StringBuilder b,Set<String> seen,int depth){if(depth>45)return;String t=text(n);if(n.getChildCount()==0&&!t.isEmpty()&&seen.add(t))b.append(t).append('\n');for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo c=n.getChild(i);if(c!=null){collect(c,b,seen,depth+1);c.recycle();}}}
 public AccessibilityNodeInfo quick(String label){int index=model.quick(label);return index<0?null:nodes.get(index);}
 public boolean newerContains(String s){for(int i=qRow+1;i<flex.size();i++)if(flex.get(i).contains(s))return true;return false;}
 public AccessibilityNodeInfo exact(String label,String suffix){for(AccessibilityNodeInfo n:nodes)if(n.isVisibleToUser()&&n.isEnabled()&&text(n).equals(label)&&(suffix.isEmpty()||id(n).endsWith(suffix)))return n;return null;}
 public boolean visibleQuestion(){if(question==null)return false;for(AccessibilityNodeInfo n:nodes)if(text(n).contains(question.body)&&n.isVisibleToUser())return true;return false;}
 public AccessibilityNodeInfo scrollable(){for(AccessibilityNodeInfo n:nodes)if(n.isVisibleToUser()&&n.isScrollable()&&id(n).contains("chat")&&id(n).contains("list"))return n;return null;}
 public List<AccessibilityNodeInfo> numericInputs(){List<AccessibilityNodeInfo> a=new ArrayList<>();for(AccessibilityNodeInfo n:nodes)if(n.isVisibleToUser()&&n.isEditable()&&n.isEnabled()&&!n.isPassword()&&!id(n).contains("chat_ui_input")&&!id(n).contains("message_edit")){
   // Only input embedded in a WebView with an explicitly labelled numeric quiz form.
   AccessibilityNodeInfo p=n.getParent();boolean web=false;int d=0;while(p!=null&&d++<12){AccessibilityNodeInfo next=p.getParent();if(String.valueOf(p.getClassName()).equals("android.webkit.WebView"))web=true;p.recycle();p=next;}if(p!=null)p.recycle();if(web)a.add(n);
  }return a;}
 public AccessibilityNodeInfo submit(){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo n:nodes)if(n.isVisibleToUser()&&n.isEnabled()&&n.isClickable()&&(text(n).equals("回答")||text(n).equals("回答する")||text(n).equals("答えを送信"))){
   // A LINE composer send button and sent chat bubble are never a quiz submit control.
   if(id(n).contains("send")||id(n).contains("chat_ui_text_message"))continue;
   AccessibilityNodeInfo p=n.getParent();boolean web=false;int d=0;while(p!=null&&d++<12){AccessibilityNodeInfo next=p.getParent();if(String.valueOf(p.getClassName()).equals("android.webkit.WebView"))web=true;p.recycle();p=next;}if(p!=null)p.recycle();if(web){if(found!=null)return null;found=n;}
  }return found;}
 public String allVisibleText(){StringBuilder b=new StringBuilder();for(AccessibilityNodeInfo n:nodes)if(n.isVisibleToUser()&&n.getChildCount()==0)b.append(text(n)).append('\n');return b.toString();}
 public String registrationContext(){return model.registrationContext();}
 public boolean trusted(){return !overflow&&LineIdentity.target(pkg,snapshot);}
 public String confirmationKey(){
  StringBuilder key=new StringBuilder(pkg).append('|').append(question==null?"":question.key).append('|').append(visibleQuestion()).append('|').append(latest).append('|').append(registrationContext());
  key.append('|').append(QuizFlow.transition(model)).append('|').append(QuizFlow.complete(model)).append('|').append(QuizFlow.correct(model)).append('|').append(QuizFlow.failed(model)).append('|').append(formEvidence());
  for(AccessibilityNodeInfo n:nodes)if(n.isVisibleToUser()&&(n.isEditable()||n.isClickable()||id(n).endsWith(":id/chat_ui_oa_bottombar_keyboard_button"))&&!n.isScrollable()){
   Rect b=new Rect();n.getBoundsInScreen(b);key.append('|').append(id(n)).append(':').append(n.getChildCount()==0?text(n):content(n)).append(':').append(n.isEnabled()).append(':').append(n.isClickable()).append(':').append(b.flattenToString());
  }return key.toString();
 }
 public AccessibilityNodeInfo uniqueId(String... suffixes){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo n:nodes){if(!n.isVisibleToUser()||!n.isEnabled())continue;for(String suffix:suffixes)if(id(n).endsWith(":id/"+suffix)){if(found!=null&&found!=n)return null;found=n;break;}}return found;}
 public List<AccessibilityNodeInfo> composers(){List<AccessibilityNodeInfo> found=new ArrayList<>();for(AccessibilityNodeInfo n:nodes)if(n.isVisibleToUser()&&n.isEnabled()&&n.isEditable()&&!n.isPassword()&&(id(n).endsWith(":id/chat_ui_input_edit")||id(n).endsWith(":id/chat_ui_input_edit_text")||id(n).endsWith(":id/chat_ui_input_message_edit_text")))found.add(n);return found;}
 public AccessibilityNodeInfo composerSend(){AccessibilityNodeInfo n=uniqueId("chat_ui_input_send","chat_ui_input_send_button","chat_ui_send_button");return n!=null&&n.isClickable()?n:null;}
 public boolean formEvidence(){if(!numericInputs().isEmpty()||submit()!=null)return true;for(AccessibilityNodeInfo n:nodes)if(n.isVisibleToUser()&&String.valueOf(n.getClassName()).equals("android.webkit.WebView"))return true;return false;}
 public boolean numericForm(){String t=Text.clean(allVisibleText());return t.contains("第5問")&&t.contains("回答");}
 public AccessibilityNodeInfo keyboardButton(){return uniqueId("chat_ui_oa_bottombar_keyboard_button");}
 public boolean currentFinal(){return question!=null&&question.number==5&&!QuizFlow.correct(model)&&!QuizFlow.failed(model)&&!QuizFlow.complete(model)&&!QuizFlow.answerControls(model);}
 public boolean registrationChoicesVisible(){for(int i=0;i<nodes.size();i++){AccessibilityNodeInfo n=nodes.get(i);if(model.rowOf(i)>qRow&&model.rowOf(i)==flex.size()-1&&n.isVisibleToUser()&&(text(n).contains("一つ前に戻る")||text(n).contains("生まれた年を選")||text(n).contains("性別を選")||text(n).contains("高校の頭文字")))return true;}return false;}
 public AccessibilityNodeInfo richStart(){AccessibilityNodeInfo found=null;for(AccessibilityNodeInfo n:nodes)if(n.isVisibleToUser()&&n.isEnabled()&&n.isClickable()&&text(n).equals("開始")){
  AccessibilityNodeInfo p=n.getParent();boolean rich=false;for(int d=0;p!=null&&d<12;d++){if(id(p).endsWith(":id/chat_ui_oa_richmenu"))rich=true;AccessibilityNodeInfo next=p.getParent();p.recycle();p=next;}if(p!=null)p.recycle();if(rich){if(found!=null)return null;found=n;}
 }return found;}
 public Registration.Stage registrationStage(){
  // Live quick replies identify intro/confirmation even when the prompt is an ordinary chat message.
  for(Registration.Stage st:new Registration.Stage[]{Registration.Stage.TERMS,Registration.Stage.SUMMARY,Registration.Stage.INTRO}){
   for(AccessibilityNodeInfo n:nodes)if(id(n).endsWith(":id/chat_ui_quick_reply_item_root")&&n.isVisibleToUser()&&n.isEnabled()&&Registration.eligible(st,content(n).trim()))return st;
  }
  String last=model.latest;Registration.Stage hinted=Registration.stage(last);
  if(hinted!=Registration.Stage.UNKNOWN&&!registrationControls(hinted).isEmpty())return hinted;
  Registration.Stage found=Registration.Stage.UNKNOWN;
  for(Registration.Stage st:new Registration.Stage[]{Registration.Stage.TERMS,Registration.Stage.YEAR,Registration.Stage.SEX,Registration.Stage.PREFECTURE,Registration.Stage.SCHOOL_INITIAL,Registration.Stage.SCHOOL})if(!registrationControls(st).isEmpty()){if(found!=Registration.Stage.UNKNOWN)return Registration.Stage.UNKNOWN;found=st;}
  return found;
 }
 public Map<String,AccessibilityNodeInfo> registrationControls(Registration.Stage stage){Map<String,AccessibilityNodeInfo> result=new LinkedHashMap<>();for(Map.Entry<String,Integer> e:model.registrationControls(stage).entrySet()){
  AccessibilityNodeInfo target=nodes.get(e.getValue());Rect bounds=new Rect();target.getBoundsInScreen(bounds);android.util.DisplayMetrics metrics=android.content.res.Resources.getSystem().getDisplayMetrics();
  // Require the centre of the visible hit area on screen; never scroll to a hidden registration choice.
  if(bounds.width()>0&&bounds.height()>0&&bounds.centerX()>=0&&bounds.centerY()>=0&&bounds.centerX()<metrics.widthPixels&&bounds.centerY()<metrics.heightPixels)result.put(e.getKey(),target);
 }return result;}

 @Override public void close(){for(AccessibilityNodeInfo n:nodes)n.recycle();nodes.clear();}
}
