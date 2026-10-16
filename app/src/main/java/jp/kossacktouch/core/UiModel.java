package jp.kossacktouch.core;
import java.util.*;
/** Platform-free parsing of actual accessibility snapshots; selectors tested with supplied dumps. */
public final class UiModel {
 public static final class Node {
  public final int parent;public final String resource,text;public final boolean visible,enabled,clickable;
  public Node(int p,String r,String t,boolean v,boolean e,boolean c){parent=p;resource=r;text=t;visible=v;enabled=e;clickable=c;}
 }
 private int[] order;private int nextOrder=0,ordinaryPrompt=-1;
 private final List<Node> nodes;private final List<List<Integer>> children=new ArrayList<>();
 public final List<String> flex=new ArrayList<>();public final List<Integer> flexRoots=new ArrayList<>();public Question question;public int qRow=-1;public String latest="";public boolean title=false;
 public UiModel(List<Node> n){nodes=n;for(int i=0;i<n.size();i++)children.add(new ArrayList<>());for(int i=0;i<n.size();i++){int p=n.get(i).parent;if(p>=0&&p<i)children.get(p).add(i);}
  order=new int[n.size()];for(int i=0;i<n.size();i++)if(n.get(i).parent<0)rank(i);
  for(int i=0;i<n.size();i++){Node x=n.get(i);if(x.resource.endsWith(":id/header_title")&&x.text.equals("超良問ドリル")&&x.visible)title=true;
   if(x.resource.endsWith(":id/chat_ui_row_flex_message_frame")){String value=content(i);flex.add(value);flexRoots.add(i);Question q=Question.parse(value);if(q!=null||value.matches("(?s).*第\\s*[1-5１-５]\\s*問.*")){question=q;qRow=flex.size()-1;}}
  }if(!flex.isEmpty())latest=flex.get(flex.size()-1);
  for(int i=0;i<n.size();i++){Node x=n.get(i);
   if(x.visible&&x.enabled&&x.resource.endsWith(":id/chat_ui_message_text")&&Registration.promptStage(x.text)!=Registration.Stage.UNKNOWN
      &&(qRow<0||order[i]>order[flexRoots.get(qRow)])&&(ordinaryPrompt<0||order[i]>order[ordinaryPrompt]))ordinaryPrompt=i;
  }
 }
 private void rank(int i){order[i]=nextOrder++;for(int child:children.get(i))rank(child);}
 /** Ordinary bot prompts can be newer than the last flex card. Keep quiz/coupon parsing unchanged. */
 public String registrationPrompt(){
  if(ordinaryPrompt>=0){
   if(registrationPromptAfterCard())return nodes.get(ordinaryPrompt).text;
   if(Registration.stage(latest)==Registration.Stage.UNKNOWN&&!newerCardChangesStage(Registration.promptStage(nodes.get(ordinaryPrompt).text)))return nodes.get(ordinaryPrompt).text;
  }
  return latest;
 }
 /** A later visible choice card can supersede an older prompt whose successor is off-screen. */
 private boolean newerCardChangesStage(Registration.Stage oldStage){
  for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);int row=rowOf(i);
   if(!n.visible||!n.enabled||!leaf(i)||row<0||row!=flex.size()-1||row<=qRow)continue;
   String label=n.text.trim();boolean different=false;
   for(Registration.Stage st:Registration.Stage.values())if(st!=oldStage&&Registration.eligible(st,label)){different=true;break;}
   if(!different)continue;
   for(int d=0,index=i;index>=0&&d<6;d++,index=parentOf(index)){
    if(rowOf(index)!=row)break;Node p=nodes.get(index);
    if(p.visible&&p.enabled&&p.clickable&&content(index).trim().equals(label))return true;
   }
  }return false;
 }
 public boolean registrationPromptAfterCard(){return ordinaryPrompt>=0&&(flexRoots.isEmpty()||order[ordinaryPrompt]>order[flexRoots.get(flexRoots.size()-1)]);}
 private String content(int index){StringBuilder b=new StringBuilder();Set<String> seen=new HashSet<>();collect(index,b,seen,0);return b.toString();}
 private void collect(int i,StringBuilder b,Set<String> seen,int d){if(d>45)return;Node x=nodes.get(i);if(children.get(i).isEmpty()&&!x.text.isEmpty()&&seen.add(x.text))b.append(x.text).append('\n');for(int k:children.get(i))collect(k,b,seen,d+1);}
 public int quick(String label){int found=-1;for(int i=0;i<nodes.size();i++){Node x=nodes.get(i);if(x.resource.endsWith(":id/chat_ui_quick_reply_item_root")&&x.visible&&x.enabled&&x.clickable&&content(i).trim().equals(label)){if(found>=0)return -1;found=i;}}return found;}
 public boolean newerContains(String text){for(int i=qRow+1;i<flex.size();i++)if(flex.get(i).contains(text))return true;return false;}
 public String registrationContext(){StringBuilder s=new StringBuilder();for(int i=Math.max(qRow+1,flex.size()-2);i<flex.size();i++)s.append(flex.get(i)).append('\n');if(ordinaryPrompt>=0)s.append(nodes.get(ordinaryPrompt).text).append('\n');return s.toString();}
 public int rowOf(int index){int i=index;while(i>=0){int row=flexRoots.indexOf(i);if(row>=0)return row;i=nodes.get(i).parent;}return -1;}
 public int parentOf(int index){return nodes.get(index).parent;}
 // The selected last question row is authoritative; an older duplicate elsewhere cannot make it visible.
 public boolean visibleQuestion(){if(question==null)return false;for(int i=0;i<nodes.size();i++)if(rowOf(i)==qRow&&nodes.get(i).visible&&nodes.get(i).text.contains(question.body))return true;return false;}
 public boolean leaf(int index){return children.get(index).isEmpty();}
 /** Latest coupon card: exact link text, including labels whose click handler is on a parent. */
 public int couponLink(String label){
  if(!label.equals("クーポンを表示する"))return -1;
  if(!latest.contains("クーポン")||!latest.contains("バーコード")||!(latest.contains("セブン")||latest.contains("難問をクリア")))return -1;
  int found=-1;
  for(int i=0;i<nodes.size();i++){
   Node n=nodes.get(i);int row=rowOf(i);
   if(!leaf(i)||!n.visible||!n.enabled||!n.text.trim().equals(label)||row<0||row!=flex.size()-1||row<=qRow)continue;
   int target=i,index=i;
   for(int d=0;index>=0&&d<6;d++,index=parentOf(index)){if(rowOf(index)!=row)break;Node p=nodes.get(index);if(p.visible&&p.enabled&&p.clickable&&content(index).trim().equals(label)){target=index;break;}}
   if(found>=0)return -1;found=target;
  }return found;
 }
 /** Prove progression to the school list without interpreting the school name. */
 private int schoolBoundary(){
  // Registration prompts may scroll out while their candidate card remains visible.
  // Only this contextual selector uses hidden prompt evidence; controls still must be visible.
  int promptIndex=-1;
  for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);if(n.enabled&&n.resource.endsWith(":id/chat_ui_message_text")&&Registration.promptStage(n.text)!=Registration.Stage.UNKNOWN&&(qRow<0||order[i]>order[flexRoots.get(qRow)])&&(promptIndex<0||order[i]>order[promptIndex]))promptIndex=i;}
  if(promptIndex<0)return -1;
  Registration.Stage prompt=Registration.promptStage(nodes.get(promptIndex).text);
  if(prompt==Registration.Stage.SCHOOL)return order[promptIndex];
  if(prompt!=Registration.Stage.SCHOOL_INITIAL)return -1;
  int reply=-1;
  for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);if(n.visible&&n.enabled&&n.resource.endsWith(":id/chat_ui_message_text")&&order[i]>order[promptIndex]&&Registration.eligible(Registration.Stage.SCHOOL_INITIAL,n.text.trim()))reply=Math.max(reply,order[i]);}
  if(reply<0)return -1;
  // The school list's dedicated back control distinguishes it from unrelated later cards.
  for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);if(!n.visible||!n.enabled||!n.text.trim().equals("一つ前に戻る"))continue;
   int row=rowOf(i);if(quick("一つ前に戻る")>=0||(row>=0&&row==flex.size()-1&&order[flexRoots.get(row)]>reply))return reply;
  }return -1;
 }
 public boolean schoolSelectionContext(){return schoolBoundary()>=0&&!schoolChoiceControls().isEmpty();}
 /** Latest dedicated candidates only; ordinary balloons and whole-card click containers are excluded. */
 private Map<String,Integer> schoolChoiceControls(){
  Map<String,Integer> result=new LinkedHashMap<>();Set<String> ambiguous=new HashSet<>();int boundary=schoolBoundary();if(boundary<0)return result;
  for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);String label=n.text.trim();if(!n.visible||!n.enabled||!leaf(i)||!Registration.schoolCandidate(label))continue;
   int target=quick(label);
   if(target>=0){if(order[target]<=boundary)continue;}
   else {
    int row=rowOf(i);if(row<0||row!=flex.size()-1||row<=qRow||order[flexRoots.get(row)]<=boundary)continue;
    target=-1;for(int d=0,index=i;index>=0&&d<6;d++,index=parentOf(index)){
     if(rowOf(index)!=row||index==flexRoots.get(row))break;Node p=nodes.get(index);
     if(p.visible&&p.enabled&&p.clickable&&content(index).trim().equals(label)){target=index;break;}
    }
   }
   if(target<0)continue;if(result.containsKey(label)&&result.get(label)!=target)ambiguous.add(label);else result.put(label,target);
  }for(String label:ambiguous)result.remove(label);return result;
 }
 public Map<String,Integer> registrationControls(Registration.Stage stage){
  if(stage==Registration.Stage.SCHOOL&&schoolBoundary()>=0)return schoolChoiceControls();
  if(stage!=Registration.Stage.SCHOOL&&schoolSelectionContext())return new LinkedHashMap<>();
  Map<String,Integer> result=new LinkedHashMap<>();Set<String> ambiguous=new HashSet<>();
  Registration.Stage prompted=Registration.promptStage(registrationPrompt());
  if(prompted!=Registration.Stage.UNKNOWN&&prompted!=stage&&!(stage==Registration.Stage.SCHOOL_GUIDE&&prompted==Registration.Stage.PREFECTURE))return result;
  for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);String label=n.text.trim();if(!n.visible||!n.enabled||!leaf(i)||!Registration.eligible(stage,label))continue;
   int quick=quick(label);if(quick>=0){result.put(label,quick);continue;}
   int row=rowOf(i);if(row<0||row!=flex.size()-1||row<=qRow)continue;
   if(ordinaryPrompt>=0&&order[flexRoots.get(row)]<order[ordinaryPrompt])continue;
   int target=-1,index=i;for(int d=0;index>=0&&d<6;d++,index=parentOf(index)){
    if(rowOf(index)!=row)break;Node p=nodes.get(index);if(p.visible&&p.enabled&&p.clickable&&content(index).trim().equals(label)){target=index;break;}
   }
   if(target<0)continue;if(result.containsKey(label)&&result.get(label)!=target)ambiguous.add(label);else result.put(label,target);
  }for(String label:ambiguous)result.remove(label);return result;
 }
}
