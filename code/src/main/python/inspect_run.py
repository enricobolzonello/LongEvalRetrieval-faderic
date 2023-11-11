# This script can be used to further analyze a run
# Input: a file containing the output of "trec_eval -q <qrels> <run>"

import numpy as np
import sys
import re

class Result:
    id: str
    num_rel: int
    num_rel_ret: int

    def __init__(self, id, num_rel, num_rel_ret):
        self.id = id
        self.num_rel = num_rel
        self.num_rel_ret = num_rel_ret

    def get_rel_diff(self) -> int:
        return self.num_rel - self.num_rel_ret
    
    def __str__(self) -> str:
        return self.id
        
def get_diffs(results):
    return [res.get_rel_diff() for res in results]

assert len(sys.argv) == 2, "you should pass a run file as arg"

with open(sys.argv[1], 'r', encoding='utf-8') as file:
    data = file.read()

query_ids = re.findall(r'num_rel\s+\t(.+)\t\d+\n', data)
query_ids.pop()
num_rels = [int(num) for num in re.findall(r'num_rel\s.+\s(\d+)\n', data)]
num_rels_ret = [int(num) for num in re.findall(r'num_rel_ret\s.+\s(\d+)\n', data)]

results = [Result(query_ids[i], num_rels[i], num_rels_ret[i]) for i in range(len(query_ids))]

print("num_rel: avg = ", np.average([res.num_rel for res in results]))
print("num_rel_ret: avg = ", np.average([res.num_rel_ret for res in results]))
print(f"num_rel - num_rel_ret: max = {np.max(get_diffs(results))}, avg = {np.average(get_diffs(results))}, middle = {np.median(list(set(get_diffs(results))))}")

results.sort(key=lambda x: x.get_rel_diff(), reverse=True)

print(f"queries with difference greater than middle value ({np.median(list(set(get_diffs(results))))}):")
c = 0
for result in results:
    if result.get_rel_diff() > np.median(list(set(get_diffs(results)))):
        print("\t", result, result.get_rel_diff())
        c+=1
print("total number = ", c)
