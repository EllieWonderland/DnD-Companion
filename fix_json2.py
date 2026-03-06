import json
import re

file_path = 'c:/Users/FiserArbeiter/EllieWonderlandApps/dndcompanion/app/src/main/assets/Rules/Zauberbuch/spellbook.json'

with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

# Fix missing commas between objects in array
text = re.sub(r'}\s+\{', '},\n    {', text)

# Remove citations like [cite: 123] or [cite: 123-125] or [cite: 123, 124] or [cite...]
text = re.sub(r'\[cite[^\]]*\]', '', text)

try:
    data = json.loads(text)
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4, ensure_ascii=False)
    print("Successfully fixed JSON and removed citations.")
except json.JSONDecodeError as e:
    print(f"Error parsing JSON: {e}")
    start = max(0, e.pos - 50)
    end = min(len(text), e.pos + 50)
    snippet = text[start:end]
    print(f"Around location:\n{snippet}")
