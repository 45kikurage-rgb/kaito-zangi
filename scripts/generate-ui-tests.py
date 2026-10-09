#!/usr/bin/env python3
import json,pathlib
root=pathlib.Path(__file__).resolve().parents[1]
out=['import jp.kossacktouch.core.*; import java.util.*; public class UiTests { static int c=0;static void check(boolean b){c++;if(!b)throw new AssertionError("UI check "+c);}public static void main(String[] args){']
for name in ['ui-tree-1791429612812-all.json','ui-tree-1791429701292-all.json','ui-tree-1791261411601-all.json']:
    d=json.loads((root/'tests/fixtures'/name).read_text());nodes=d['nodes'];indices={n['id']:i for i,n in enumerate(nodes)}
    out.append('{List<UiModel.Node> ns=new ArrayList<>();')
    for n in nodes:
        vals=[str(indices.get(n['parent_id'],-1)),json.dumps(n['resource'],ensure_ascii=False),json.dumps(n['text'],ensure_ascii=False)]+[str(n[k]).lower() for k in ['visible_to_user','enabled','clickable']]
        out.append('ns.add(new UiModel.Node('+','.join(vals)+'));')
    out.append('UiModel m=new UiModel(ns);check(m.title);check(LineIdentity.target("jp.naver.line.androif",ns));check(!LineIdentity.target("com.android.chrome",ns));check(m.question!=null&&m.question.number==1);')
    if '1791429612812' in name:
        out.append('check(m.quick("C")>=0);check(ns.get(m.quick("C")).resource.endsWith("chat_ui_quick_reply_item_root"));check(m.question.choices.get("C").equals("67分"));')
        out.append('ns.add(new UiModel.Node(-1,"p:id/chat_ui_quick_reply_item_root","C",true,true,true));check(new UiModel(ns).quick("C")==-1);')
        out.append('ns.add(new UiModel.Node(-1,"p:id/chat_ui_row_flex_message_frame","第2問",true,true,false));check(new UiModel(ns).question==null);')
    elif '1791429701292' in name:out.append('check(m.quick("C")==-1);check(m.question.body.startsWith("100m走"));')
    else:out.append('check(m.quick("D")==-1);check(m.newerContains("正解！"));')
    out.append('}')
out.append('System.out.println("PASS "+c+" actual UI-dump checks: correct button container, hidden-button wait, answered-message rejection, latest incomplete question guard.");}}')
(root/'build/UiTests.java').write_text('\n'.join(out)+'\n')
