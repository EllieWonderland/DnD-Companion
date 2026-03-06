import json
import re

filepath = 'c:/Users/FiserArbeiter/EllieWonderlandApps/dndcompanion/app/src/main/assets/Rules/Zauberbuch/spellbook.json'
with open(filepath, 'r', encoding='utf-8') as f:
    text = f.read()

# Try to extract the array contents
m = re.match(r'^\s*\[\s*(.*)\s*\]\s*$', text, flags=re.DOTALL)
if not m:
    print("Could not match array wrapper!")
else:
    inner = m.group(1)
    
    # We split by '    },\n    {\n' or similar. 
    # But wait, it's safer to just iterate and use json.JSONDecoder().raw_decode() repeatedly!
    decoder = json.JSONDecoder()
    
    idx = 0
    inner_len = len(inner)
    count = 0
    last_idx = 0
    try:
        while idx < inner_len:
            # Skip whitespace and commas
            while idx < inner_len and inner[idx] in ' \n\r\t,':
                idx += 1
            if idx >= inner_len:
                break
            
            last_idx = idx
            obj, end_idx = decoder.raw_decode(inner, idx)
            idx = end_idx
            count += 1
            
        print(f"Successfully parsed {count} objects isolated.")
    except json.JSONDecodeError as e:
        print(f"Error parsing object #{count+1} at index {last_idx}")
        print(f"Error details: {e}")
        # Print the start of the object that failed
        failed_obj_start = inner[last_idx:last_idx+200]
        print(f"Failed object starts with:\n{failed_obj_start}")
