package jp.kossacktouch.core;
import java.util.*;
/** Platform-free parsing of actual accessibility snapshots; selectors tested with supplied dumps. */
public final class UiModel {
 public static final class Node {
  public final int parent;public final String resource,text;public final boolean visible,enabled,clickable;
  public Node(int p,String r,String t,boolean v,boolean e,boolean c){parent=p;resource=r;text=t;visible=v;enabled=e;clickable=c;}
 }
 private final List<Node> nodes;private final List<List<Integer>> children=new ArrayList<>();
 public final List<String> flex=new ArrayList<>();public final List<Integer> flexRoots=new ArrayList<>();public Question question;public int qRow=-1;public String latest="";public boolean title=false;
 public UiModel(List<Node> n){nodes=n;for(int i=0;i<n.size();i++)children.add(new ArrayList<>());for(int i=0;i<n.size();i++){int p=n.get(i).parent;if(p>=0&&p<i)children.get(p).add(i);}
  for(int i=0;i<n.size();i++){Node x=n.get(i);if(x.resource.endsWith(":id/header_title")&&x.text.equals("超良問ドリル")&&x.visible)title=true;
   if(x.resource.endsWith(":id/chat_ui_row_flex_message_frame")){String value=content(i);flex.add(value);flexRoots.add(i);Question q=Question.parse(value);if(q!=null||value.matches("(?s).*第\\s*[1-5１-５]\\s*問.*")){question=q;qRow=flex.size()-1;}}
  }if(!flex.isEmpty())latest=flex.get(flex.size()-1);
 }
 private String content(int index){StringBuilder b=new StringBuilder();Set<String> seen=new HashSet<>();collect(index,b,seen,0);return b.toString();}
 private void collect(int i,StringBuilder b,Set<String> seen,int d){if(d>45)return;Node x=nodes.get(i);if(children.get(i).isEmpty()&&!x.text.isEmpty()&&seen.add(x.text))b.append(x.text).append('\n');for(int k:children.get(i))collect(k,b,seen,d+1);}
 public int quick(String label){int found=-1;for(int i=0;i<nodes.size();i++){Node x=nodes.get(i);if(x.resource.endsWith(":id/chat_ui_quick_reply_item_root")&&x.visible&&x.enabled&&x.clickable&&content(i).trim().equals(label)){if(found>=0)return -1;found=i;}}return found;}
 public boolean newerContains(String text){for(int i=qRow+1;i<flex.size();i++)if(flex.get(i).contains(text))return true;return false;}
 public String registrationContext(){StringBuilder s=new StringBuilder();for(int i=Math.max(qRow+1,flex.size()-2);i<flex.size();i++)s.append(flex.get(i)).append('\n');return s.toString();}
 public int rowOf(int index){int i=index;while(i>=0){int row=flexRoots.indexOf(i);if(row>=0)return row;i=nodes.get(i).parent;}return -1;}
 public int parentOf(int index){return nodes.get(index).parent;}
 // The selected last question row is authoritative; an older duplicate elsewhere cannot make it visible.
 public boolean visibleQuestion(){if(question==null)return false;for(int i=0;i<nodes.size();i++)if(rowOf(i)==qRow&&nodes.get(i).visible&&nodes.get(i).text.contains(question.body))return true;return false;}
 public boolean leaf(int index){return children.get(index).isEmpty();}
 public Map<String,Integer> registrationControls(Registration.Stage stage){
  Map<String,Integer> result=new LinkedHashMap<>();Set<String> ambiguous=new HashSet<>();
  for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);String label=n.text.trim();if(!n.visible||!n.enabled||!leaf(i)||!Registration.eligible(stage,label))continue;
   int quick=quick(label);if(quick>=0){result.put(label,quick);continue;}
   int row=rowOf(i);if(row<0||row!=flex.size()-1||row<=qRow)continue;
   int target=-1,index=i;for(int d=0;index>=0&&d<6;d++,index=parentOf(index)){
    if(rowOf(index)!=row)break;Node p=nodes.get(index);if(p.visible&&p.enabled&&p.clickable&&content(index).trim().equals(label)){target=index;break;}
   }
   if(target<0)continue;if(result.containsKey(label)&&result.get(label)!=target)ambiguous.add(label);else result.put(label,target);
  }for(String label:ambiguous)result.remove(label);return result;
 }
}
