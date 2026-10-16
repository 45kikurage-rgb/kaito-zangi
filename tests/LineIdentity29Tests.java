import java.util.*;
import jp.kossacktouch.core.*;
public final class LineIdentity29Tests {
 static int checks;
 static final String PKG="jp.naver.line.android";
 static void check(boolean value){checks++;if(!value)throw new AssertionError("LineIdentity29 check "+checks);}
 static UiModel.Node n(int parent,String id,String text,boolean visible){return new UiModel.Node(parent,PKG+":id/"+id,text,visible,true,false);}
 static List<UiModel.Node> greeting(){return new ArrayList<>(Arrays.asList(
  n(-1,"header_title","超良問ドリル",true),n(-1,"chat_ui_main_content_area","",true),
  n(1,"chat_ui_message_list_refresh_layout","",true),n(2,"chathistory_message_list","",true),
  n(3,"chat_ui_row_message_layout","",true),n(4,"chat_ui_row_text_message","",true),
  n(5,"chat_ui_text_message_balloon_root","",true),n(6,"chat_ui_message_text","下のメニューの「開始」から挑戦じゃ",true)));
 }
 static UiModel.Node copy(UiModel.Node n,int parent,String resource,boolean visible){return new UiModel.Node(parent,resource,n.text,visible,n.enabled,n.clickable);}
 public static void main(String[] args){
  List<UiModel.Node> ns=greeting();check(LineIdentity.target(PKG,ns));
  check(LineIdentity.target("jp.naver.line.clone001",ns));
  check(!LineIdentity.target("com.android.chrome",ns));
  // Native text chat remains identifiable before and after the rich menu is closed.
  ns.add(n(-1,"chat_ui_oa_richmenu","",true));ns.add(n(8,"oa_richmenu_imageview","",true));check(LineIdentity.target(PKG,ns));
  ns=greeting();ns.set(0,n(-1,"header_title","別のトーク",true));check(!LineIdentity.target(PKG,ns));
  ns=greeting();ns.add(n(-1,"header_title","超良問ドリル",true));check(!LineIdentity.target(PKG,ns));
  for(int i=0;i<8;i++){
   ns=greeting();UiModel.Node node=ns.get(i);ns.set(i,copy(node,node.parent,node.resource,false));check(!LineIdentity.target(PKG,ns));
  }
  // A title or arbitrary list alone is insufficient; the actual nested text-message structure is required.
  for(int i:new int[]{1,3,5,7}){
   ns=greeting();UiModel.Node node=ns.get(i);ns.set(i,copy(node,node.parent,PKG+":id/unrelated",true));check(!LineIdentity.target(PKG,ns));
  }
  for(int i:new int[]{3,5,7}){
   ns=greeting();UiModel.Node node=ns.get(i);ns.set(i,copy(node,-1,node.resource,true));check(!LineIdentity.target(PKG,ns));
   ns=greeting();node=ns.get(i);ns.set(i,copy(node,node.parent,node.resource.replace(PKG,"jp.naver.line.other"),true));check(!LineIdentity.target(PKG,ns));
  }
  ns=greeting();UiModel.Node cycle=ns.get(6);ns.set(6,copy(cycle,6,cycle.resource,true));check(!LineIdentity.target(PKG,ns));
  ns=greeting();cycle=ns.get(6);ns.set(6,copy(cycle,999,cycle.resource,true));check(!LineIdentity.target(PKG,ns));
  // Supplied real trees use chathistory_message_list. Remove only flex evidence to reproduce pre-quiz greeting.
  for(int fixture=0;fixture<3;fixture++){
   ns=RichMenu29Fixtures.nodes(fixture);check(LineIdentity.target(PKG,ns));boolean observedList=false,observedText=false;
   for(int i=0;i<ns.size();i++){
    UiModel.Node node=ns.get(i);observedList|=node.resource.endsWith(":id/chathistory_message_list");observedText|=node.resource.endsWith(":id/chat_ui_message_text");
    if(node.resource.endsWith(":id/chat_ui_row_flex_message_frame"))ns.set(i,copy(node,node.parent,node.resource.replace("chat_ui_row_flex_message_frame","removed_flex_evidence"),node.visible));
   }
   check(observedList&&observedText);check(LineIdentity.target(PKG,ns));
   for(int i=0;i<ns.size();i++){UiModel.Node node=ns.get(i);if(node.resource.endsWith(":id/chat_ui_message_text"))ns.set(i,copy(node,node.parent,node.resource,false));}
   check(!LineIdentity.target(PKG,ns));
  }
  System.out.println("PASS "+checks+" line identity code29 checks: ordinary greeting, observed native lists, visible ancestry, duplicate headers and foreign-app rejection.");
 }
}
