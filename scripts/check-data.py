#!/usr/bin/env python3
import json, unicodedata, pathlib
p=pathlib.Path(__file__).resolve().parents[1]/'app/src/main/assets/answers.json'
d=json.loads(p.read_text());ids=set();stems=set()
for q in d['questions']:
    assert q['id'] not in ids, 'duplicate id'
    n=unicodedata.normalize('NFKC',q['stem']).strip()
    assert n not in stems, 'duplicate stem'
    assert q['source_letter'] in 'ABCD'
    assert q['answer_tokens'] and all(q['answer_tokens'])
    assert not(q['enabled'] and q['requires_visual_context'])
    ids.add(q['id']);stems.add(n)
print(f"Answer data: {len(ids)} unique entries; {sum(q['enabled'] for q in d['questions'])} text-enabled; {sum(q['requires_visual_context'] for q in d['questions'])} visual-only blocked")
