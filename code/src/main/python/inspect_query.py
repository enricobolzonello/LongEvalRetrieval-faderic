# This script can be used to inspect a trec format file containing topics

import sys
import re

assert len(sys.argv) == 2, "you should pass a query trec file as arg"

with open(sys.argv[1], 'r', encoding='utf-8') as file:
    data = file.read()

titles = re.findall(r'title>(.+?)<', data)
titles_str = ''.join(titles)

print('unique chars:', ''.join(sorted((set(titles_str)))))

print('\nqueries containing numbers:')
c=0
for title in titles:
    if re.search(r'\d', title):
        print(title)
        c+=1
print('total ', c)
        
c=0
print('\nqueries containing one word:')
for title in titles:
    if not re.search(r' ', title):
        print(title)
        c+=1
print('total ', c)