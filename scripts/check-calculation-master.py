#!/usr/bin/env python3
"""Verify the bundled user-supplied Q5 calculation master against compiled lookup data."""
import csv
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/assets/final_question_calculation_master.csv"
ENGINE = ROOT / "app/src/main/java/jp/kossacktouch/core/MathEngine.java"
REQUIRED = {
    "triangle_max_x", "arithmetic_range_sum", "arithmetic_first_sum_over",
    "ends_same", "ends_different", "multiples_neither", "multiples_a_not_b",
    "multiples_b_not_a", "multiples_both", "multiples_either",
    "boxing_diff_divisor", "boxing_diff_not_divisor",
    "boxing_coprime", "boxing_not_coprime",
}

with SOURCE.open(encoding="utf-8-sig", newline="") as stream:
    rows = list(csv.DictReader(stream))
assert len(rows) == 33, f"Unexpected number of master rows: {len(rows)}"
codes = [row["code"] for row in rows if row["record_type"] == "problem_pattern"]
assert len(codes) == len(REQUIRED) and set(codes) == REQUIRED, "Missing, extra, or repeated formula family"
text = ENGINE.read_text(encoding="utf-8")
for category, symbol, expected_count in [
    ("sports_master", "SPORTS", 10),
    ("boxing_master", "BOXING", 9),
]:
    expected = {row["label"]: int(row["numeric_value"]) for row in rows if row["record_type"] == category}
    assert len(expected) == expected_count
    compiled = {
        name: int(value)
        for name, value in re.findall(r'\b' + symbol + r'\.put\("([^"]+)",(\d+)\)', text)
    }
    assert expected == compiled, f"{symbol} changed or differs from bundled CSV"
assert "if(inverse)c=total-c;" in text and 'percent(c,total,digits)' in text, "Boxing inverse probability conversion absent"
print(f"PASS master consistency: {len(codes)} formulas, 10 sports, 9 boxing categories; CSV bundled.")
