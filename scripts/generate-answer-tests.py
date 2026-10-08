#!/usr/bin/env python3
"""Load exactly the bundled JSON into Java for host-side runtime tests (no Android SDK needed)."""
import json,pathlib
root=pathlib.Path(__file__).resolve().parents[1]
d=json.loads((root/'app/src/main/assets/answers.json').read_text())
def s(x):return json.dumps(x,ensure_ascii=False)
def strings(x):return 'Arrays.asList('+','.join(s(v) for v in x)+')'
def groups(x):return 'Arrays.asList('+','.join(strings(v) for v in x)+')' if x else 'new ArrayList<List<String>>()'
out=['import jp.kossacktouch.core.*; import java.util.*; public class AnswerFixture {public static List<AnswerBank.Entry> entries(){List<AnswerBank.Entry> all=new ArrayList<>();']
for q in d['questions']:
 out.append('{AnswerBank.Entry e=new AnswerBank.Entry('+','.join([s(q['id']),s(q['stem']),s(q['answer']),strings(q['answer_tokens']),str(q['enabled']).lower(),s(q['question'] if q['question_completeness']=='full' else '')])+');')
 for field,key in [('keywords','question_keywords'),('context','context_keywords'),('answerGroups','answer_keyword_groups'),('visualText','visual_text_alternatives')]:out.append('e.'+field+'='+groups(q.get(key,[]))+';')
 out.append('e.visualRequired='+str(q['requires_visual_context']).lower()+';e.aliases='+strings(q['answer_aliases'])+';all.add(e);}')
out.append('return all;} public static AnswerBank bank(){return new AnswerBank(entries());}}')
(root/'build/AnswerFixture.java').write_text('\n'.join(out)+'\n')
