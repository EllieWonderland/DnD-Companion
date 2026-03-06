import json
import tokenize
from io import StringIO

filepath = 'c:/Users/FiserArbeiter/EllieWonderlandApps/dndcompanion/app/src/main/assets/Rules/Zauberbuch/spellbook.json'
with open(filepath, 'r', encoding='utf-8') as f:
    text = f.read()

def find_missing_commas(json_string):
    # This is a naive bracket/quote tracker to find the real error
    in_string = False
    escape = False
    depth_obj = 0
    depth_arr = 0
    last_value_end = False
    
    for i, char in enumerate(json_string):
        if in_string:
            if escape:
                escape = False
            elif char == '\\':
                escape = True
            elif char == '"':
                in_string = False
                last_value_end = True
            continue
            
        if char == '"':
            if last_value_end:
                print(f"Error: Missing comma before string at char {i}")
                print(f"Around: {json_string[max(0, i-50):min(len(json_string), i+50)]}")
                return
            in_string = True
            last_value_end = False
        elif char == '{':
            if last_value_end:
                print(f"Error: Missing comma before {{ at char {i}")
                print(f"Around: {json_string[max(0, i-50):min(len(json_string), i+50)]}")
                return
            depth_obj += 1
            last_value_end = False
        elif char == '[':
            if last_value_end:
                print(f"Error: Missing comma before [ at char {i}")
                print(f"Around: {json_string[max(0, i-50):min(len(json_string), i+50)]}")
                return
            depth_arr += 1
            last_value_end = False
        elif char in '}]':
            if char == '}': depth_obj -= 1
            if char == ']': depth_arr -= 1
            last_value_end = True
        elif char == ',':
            last_value_end = False
        elif char.isdigit() or char in 'tfn': # true, false, null, numbers
            if last_value_end and not (json_string[i:i+4] in ('true', 'null') or json_string[i:i+5] == 'false' or char.isdigit()):
                continue
            if last_value_end: # basic check for words
                pass # let's not make it too complex, just check { and "
            # we skip proper value end tracking for bare words for now
        elif not char.isspace():
            last_value_end = False

find_missing_commas(text)

