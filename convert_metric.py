import os
import re

ASSET_DIR = r"c:\Users\FiserArbeiter\EllieWonderlandApps\dndcompanion\app\src\main\assets"

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Weight: match "X Pfd." or "X Pfund" where X can be integer or float
    def weight_replacer(match):
        val_str = match.group(1).replace(",", ".")
        try:
            val = float(val_str)
            kg_val = val / 2.0
            # format nicely: if integer, show integer
            if kg_val.is_integer():
                kg_str = str(int(kg_val))
            else:
                kg_str = str(kg_val).replace(".", ",")
            return f"{kg_str} kg"
        except ValueError:
            return match.group(0) # Keep original if parse fails

    content = re.sub(r'\b(\d+(?:[.,]\d+)?)\s*(?:Pfd\.|Pfund)\b', weight_replacer, content)

    # Distance: match "X Fuß", "X ft.", "X ft"
    def distance_replacer(match):
        val_str = match.group(1).replace(",", ".")
        try:
            val = float(val_str)
            m = val * 0.3
            f = int(val / 5.0)
            if m.is_integer():
                m_str = str(int(m))
            else:
                m_str = str(m).replace(".", ",")
            return f"{m_str} m / {f} Felder"
        except ValueError:
            return match.group(0)

    content = re.sub(r'\b(\d+(?:[.,]\d+)?)\s*(?:Fuß|ft\.|ft)\b', distance_replacer, content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == "__main__":
    for root, dirs, files in os.walk(ASSET_DIR):
        for file in files:
            if file.endswith(".md"):
                process_file(os.path.join(root, file))
    print("Done converting metric units.")
