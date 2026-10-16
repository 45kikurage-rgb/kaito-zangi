package jp.kossacktouch.core;
import java.util.*;
/** Package family is only a candidate. Target title and native LINE structure are mandatory. */
public final class LineIdentity {
 private LineIdentity(){}
 public static boolean candidate(String pkg){return pkg!=null&&pkg.matches("jp\\.naver\\.line\\.[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)*");}
 public static boolean target(String pkg,List<UiModel.Node> nodes){
  if(!candidate(pkg))return false;boolean title=false,chat=false,rows=false;int headers=0;
  for(UiModel.Node n:nodes){String id=n.resource;int split=id.indexOf(":id/");if(split<0||!candidate(id.substring(0,split)))continue;
   if(n.visible&&id.endsWith(":id/header_title")){headers++;if(n.text.equals("超良問ドリル"))title=true;}
   if(n.visible&&id.endsWith(":id/chat_ui_main_content_area"))chat=true;
   if(id.endsWith(":id/chat_ui_row_flex_message_frame")||id.endsWith(":id/chat_ui_list"))rows=true;
  }return headers==1&&title&&chat&&(rows||ordinaryChat(nodes));
 }
 /** Current LINE uses chathistory_message_list, including before any flex card arrives. */
 private static boolean ordinaryChat(List<UiModel.Node> nodes){
  for(int i=0;i<nodes.size();i++){
   UiModel.Node n=nodes.get(i);String id=n.resource;int split=id.indexOf(":id/");
   if(!n.visible||split<0||!candidate(id.substring(0,split))||!id.endsWith(":id/chat_ui_message_text"))continue;
   String owner=id.substring(0,split);int row=ancestor(nodes,i,owner+":id/chat_ui_row_text_message");
   int list=ancestor(nodes,row,owner+":id/chathistory_message_list");
   if(ancestor(nodes,list,owner+":id/chat_ui_main_content_area")>=0)return true;
  }return false;
 }
 private static int ancestor(List<UiModel.Node> nodes,int child,String resource){
  if(child<0||child>=nodes.size())return -1;
  int index=nodes.get(child).parent;
  for(int depth=0;index>=0&&index<nodes.size()&&depth<46;depth++){
   UiModel.Node n=nodes.get(index);if(!n.visible)return -1;if(n.resource.equals(resource))return index;
   if(n.parent>=index)return -1;index=n.parent;
  }return -1;
 }
}
