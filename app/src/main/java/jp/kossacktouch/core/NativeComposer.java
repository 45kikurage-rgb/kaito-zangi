package jp.kossacktouch.core;
import java.util.*;
/** Native LINE composer only; unknown IDs require the input container observed in supplied dumps. */
public final class NativeComposer {
 public static final class Node {
  public final int parent;public final String resource,label;public final boolean visible,enabled,editable,password,clickable,web;
  public Node(int p,String r,String l,boolean v,boolean e,boolean edit,boolean pass,boolean click,boolean w){parent=p;resource=r;label=l;visible=v;enabled=e;editable=edit;password=pass;clickable=click;web=w;}
 }
 private final List<Node> nodes;private final String prefix;
 public NativeComposer(List<Node> n,String pkg){nodes=n;prefix=pkg+":id/";}
 private boolean id(Node n,String... names){for(String name:names)if(n.resource.equals(prefix+name))return true;return false;}
 private boolean nativeNode(int index){int i=index;for(int d=0;i>=0&&d<46;d++){Node n=nodes.get(i);if(n.web)return false;int p=n.parent;if(p>=i)return false;i=p;}return i<0;}
 private int container(int index){int i=index;for(int d=0;i>=0&&d<46;d++){Node n=nodes.get(i);if(id(n,"chat_ui_custom_expandable_input_container")&&n.visible&&n.enabled)return i;int p=n.parent;if(p>=i)return -1;i=p;}return -1;}
 public List<Integer> inputs(){List<Integer> found=new ArrayList<>();for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);if(n.visible&&n.enabled&&n.editable&&!n.password&&nativeNode(i)&&(container(i)>=0||id(n,"chat_ui_input_edit","chat_ui_input_edit_text","chat_ui_input_message_edit_text")))found.add(i);}return found;}
 public int send(){List<Integer> inputs=inputs();if(inputs.size()!=1)return -1;int boundary=container(inputs.get(0)),found=-1;for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);if(!n.visible||!n.enabled||!n.clickable||!nativeNode(i))continue;
  boolean known=id(n,"chat_ui_input_send","chat_ui_input_send_button","chat_ui_send_button");
  boolean labelled=boundary>=0&&container(i)==boundary&&(n.label.trim().equals("送信")||n.label.trim().equals("Send"));
  if(known||labelled){if(found>=0)return -1;found=i;}
 }return found;}
}
