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
  }return headers==1&&title&&chat&&rows;
 }
}
