import os
import glob
from bs4 import BeautifulSoup
import json

INPUT_DIR = "src/main/resources/pages/"
OUTPUT_FILE = "src/main/resources/pages/javascript/search_index.js"
HEADING_TAGS = ["h1", "h2", "h3", "h4", "h5", "h6"]

def extract_sections(filepath):
    with open(filepath, "r", encoding="utf-8", errors="replace") as f:
        soup = BeautifulSoup(f, "html.parser")

    filename = os.path.basename(filepath)
    sections = []

    # Find all heading tags with an ID
    heading_tags = [tag for tag in soup.find_all(HEADING_TAGS) if tag.has_attr("id")]

    for i, heading in enumerate(heading_tags):
        section_title = heading.get_text(strip=True)
        section_id = heading["id"]
        content = []

        # Gather all content until the next heading
        for sibling in heading.find_next_siblings():
            if sibling.name in HEADING_TAGS:
                break
            if sibling.name not in ["script", "style"]:
                content.append(sibling.get_text(strip=True))

        sections.append({
            "title": section_title,
            "url": f"{filename}#{section_id}",
            "content": " ".join(content)
        })

    return sections

def build_index():
    entries = []
    for path in glob.glob(os.path.join(INPUT_DIR, "*.html")):
        entries.extend(extract_sections(path))
    return entries

def write_search_index(entries):
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write("const searchIndex = ")
        json.dump(entries, f, indent=2)
        f.write(";")

if __name__ == "__main__":
    index = build_index()
    write_search_index(index)
    print(f"Generated {OUTPUT_FILE} with {len(index)} entries.")
