import java.util.*;import jp.kossacktouch.core.*;
public class RichStart29Tests {
 static int count;static void check(boolean b){count++;if(!b)throw new AssertionError("rich29 "+count);}
 static UiModel.Node n(int p,String id,String text,boolean v,boolean e,boolean c){return new UiModel.Node(p,"jp.naver.line.androif:id/"+id,text,v,e,c);}
 static List<UiModel.Node> base(){return new ArrayList<>(Arrays.asList(n(-1,"chat_ui_oa_bottombar_promotion_menu_button","",true,true,true),n(0,"chat_ui_oa_bottombar_menu_text","メニュー開く/閉じる",true,true,false)));}
 public static void main(String[] args){
  // Both active quiz and active registration use close-only, including resumed sessions.
  for(String activeSource:Arrays.asList("quiz","registration")){
   check(RichStartFlow.shouldClose(false,"",true,true));check(!RichStartFlow.canStart(true,true,true,false,false));check(!RichStartFlow.canStart(true,true,false,true,false));
   check(!RichStartFlow.shouldClose(false,"",true,false));
  }
  check(!RichStartFlow.shouldClose(false,"",false,true));check(RichStartFlow.canStart(true,true,false,false,false));
  check(!RichStartFlow.shouldClose(true,"requested",false,true));check(RichStartFlow.shouldClose(true,"requested",true,true));
  check(!RichStartFlow.shouldClose(true,"",false,true));check(RichStartFlow.shouldClose(true,"",true,true));
  check(RichStartFlow.shouldClose(true,"tapped",false,true));check(RichStartFlow.shouldClose(true,"tapped",true,true));
  for(String phase:Arrays.asList("closing","closing_existing","unknown"))for(boolean active:new boolean[]{false,true}){
   check(!RichStartFlow.shouldClose(true,phase,active,true));check(!RichStartFlow.shouldClose(true,phase,active,false));
  }
  for(String phase:Arrays.asList("","requested","tapped","closing","closing_existing","unknown"))check(RichStartFlow.closeOnly(phase)==phase.equals("closing_existing"));
  for(int fixture=0;fixture<3;fixture++){
   List<UiModel.Node> a=RichMenu29Fixtures.nodes(fixture);int target=RichStartFlow.closeControl(a);check(target>=0);check(a.get(target).clickable);check(a.get(target).resource.endsWith(":id/chat_ui_oa_bottombar_promotion_menu_button"));
   check(a.stream().anyMatch(x->x.resource.endsWith(":id/chat_ui_oa_bottombar_menu_text")&&!x.clickable));
  }
  check(RichStartFlow.closeControl(base())==0);
  List<UiModel.Node> a=base();a.set(1,n(0,"chat_ui_oa_bottombar_menu_text","",true,true,false));check(RichStartFlow.closeControl(a)==0);
  a=base();a.set(0,n(-1,"chat_ui_oa_bottombar_promotion_menu_button","",false,true,true));check(RichStartFlow.closeControl(a)==-1);
  a=base();a.set(0,n(-1,"chat_ui_oa_bottombar_promotion_menu_button","",true,false,true));check(RichStartFlow.closeControl(a)==-1);
  a=base();a.set(0,n(-1,"chat_ui_oa_bottombar_promotion_menu_button","",true,true,false));check(RichStartFlow.closeControl(a)==-1);
  a=base();a.set(1,n(-1,"chat_ui_oa_bottombar_menu_text","メニュー開く/閉じる",true,true,false));check(RichStartFlow.closeControl(a)==-1);
  a=base();a.add(n(-1,"chat_ui_oa_bottombar_promotion_menu_button","",true,true,true));check(RichStartFlow.closeControl(a)==-1);
  a=base();a.add(n(0,"chat_ui_oa_bottombar_menu_text","",true,true,false));check(RichStartFlow.closeControl(a)==-1);
  a=base();a.set(1,n(0,"chat_ui_oa_bottombar_menu_text","別の操作",true,true,false));check(RichStartFlow.closeControl(a)==-1);
  a=base();a.set(0,n(-1,"chat_ui_oa_bottombar_keyboard_button","",true,true,true));check(RichStartFlow.closeControl(a)==-1);
  System.out.println("PASS "+count+" code29 rich-menu checks: active close-only, requested/closing recovery guards, no second toggle and actual fixture parent-button selection.");
 }
}
