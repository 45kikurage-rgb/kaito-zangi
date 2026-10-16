#!/usr/bin/env python3
"""Reuse actual supplied accessibility dumps for the menu-close regression."""
import json,pathlib
root=pathlib.Path(__file__).resolve().parents[1]
out=['import java.util.*;import jp.kossacktouch.core.*;public final class RichMenu29Fixtures {public static List<UiModel.Node> nodes(int fixture){List<UiModel.Node> ns=new ArrayList<>();switch(fixture){']
for index,name in enumerate(['ui-tree-1791429612812-all.json','ui-tree-1791429701292-all.json','ui-tree-1791261411601-all.json']):
    nodes=json.loads((root/'tests/fixtures'/name).read_text())['nodes'];indices={n['id']:i for i,n in enumerate(nodes)}
    out.append(f'case {index}:')
    for n in nodes:
        fields=[str(indices.get(n['parent_id'],-1)),json.dumps(n['resource'],ensure_ascii=False),json.dumps(n['text'],ensure_ascii=False)]+[str(n[k]).lower() for k in ['visible_to_user','enabled','clickable']]
        out.append('ns.add(new UiModel.Node('+','.join(fields)+'));')
    out.append('break;')
out.append('default:throw new IllegalArgumentException();}return ns;}}')
(root/'build/RichMenu29Fixtures.java').write_text('\n'.join(out)+'\n')
