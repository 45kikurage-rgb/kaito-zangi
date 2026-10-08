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
    assert q['question_keywords'] and all(g and all(g) for g in q['question_keywords'])
    assert not q['requires_visual_context'], 'image recognition is not used; identify every question by text'
    assert q['answer_aliases'] is not None
    ids.add(q['id']);stems.add(n)
assert len(ids)==50
print(f"Answer data: {len(ids)} unique entries; {sum(q['enabled'] and not q['requires_visual_context'] for q in d['questions'])} text-enabled; {sum(q['requires_visual_context'] for q in d['questions'])} image-dependent entries")
