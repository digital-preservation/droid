import os
import sys
import subprocess
import requests
from datetime import datetime, timezone
import glob

# === CONFIGURATION ===
REPO = "digital-preservation/droid"  # e.g., "octocat/Hello-World"
ASSET_FILES = sorted(
    glob.glob("droid-binary/target/droid-binary-*-bin-win64-with-jre.zip") +
    glob.glob("droid-binary/target/droid-binary-*-bin.zip")
)

if not ASSET_FILES:
    print("No matching files found for upload.")
    sys.exit(1)

TOKEN = os.getenv("GITHUB_TOKEN")

if not TOKEN:
    print("GITHUB_TOKEN environment variable not set.")
    sys.exit(1)

if len(sys.argv) != 2:
    print("Usage: python create_github_release_raw.py <release_version>")
    sys.exit(1)

RELEASE_VERSION = sys.argv[1]
API_BASE = f"https://api.github.com/repos/{REPO}"
HEADERS = {"Authorization": f"Bearer {TOKEN}", "Accept": "application/vnd.github+json"}

# === FETCH RELEASES ===
print("Fetching existing releases...")
releases_resp = requests.get(f"{API_BASE}/releases", headers=HEADERS)
releases_resp.raise_for_status()
releases = releases_resp.json()

def parse_github_datetime(dt_str):
    # Format: "2023-12-01T10:00:00Z"
    return datetime.strptime(dt_str, "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)

last_release_date = datetime(1970, 1, 1, tzinfo=timezone.utc)
for r in releases:
    if r["tag_name"] != RELEASE_VERSION and r["tag_name"] != 'develop':
        last_release_date = parse_github_datetime(r["created_at"])
        break

print(f"Last release date: {last_release_date.isoformat()}")

# === GET MERGED PRs SINCE LAST RELEASE ===
print("Fetching merged pull requests...")
merged_prs = []
page = 1
while True:
    resp = requests.get(
        f"{API_BASE}/pulls",
        headers=HEADERS,
        params={"state": "closed", "sort": "updated", "direction": "desc", "per_page": 100, "page": page}
    )
    resp.raise_for_status()
    prs = resp.json()
    if not prs:
        break

    for pr in prs:
        merged_at = pr.get("merged_at")
        if not merged_at:
            continue
        merged_dt = parse_github_datetime(merged_at)
        if merged_dt <= last_release_date:
            break
        if pr["user"]["login"].startswith("dependabot"):
            continue
        merged_prs.append(f"- #{pr['number']} {pr['title']}")

    page += 1

release_notes = "\n".join(reversed(merged_prs)) or "No non-Dependabot PRs since last release."

# === CREATE RELEASE ===
print(f"Creating GitHub release for {RELEASE_VERSION}...")
release_resp = requests.post(
    f"{API_BASE}/releases",
    headers=HEADERS,
    json={
        "tag_name": RELEASE_VERSION,
        "name": f"Release {RELEASE_VERSION}",
        "body": release_notes,
        "draft": False,
        "prerelease": False
    }
)
release_resp.raise_for_status()
release = release_resp.json()
upload_url = release["upload_url"].split("{")[0]

# === UPLOAD FILES ===
for file_path in ASSET_FILES:
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        continue

    file_name = os.path.basename(file_path)
    print(f"Uploading {file_name}...")
    with open(file_path, "rb") as f:
        upload_resp = requests.post(
            f"{upload_url}?name={file_name}",
            headers={
                "Authorization": f"Bearer {TOKEN}",
                "Content-Type": "application/octet-stream"
            },
            data=f.read()
        )

        if upload_resp.status_code == 201:
            print(f"Uploaded {file_name}")
        else:
            print(f"Failed to upload {file_name}: {upload_resp.status_code}\n{upload_resp.text}")

print(f"✅ Release {RELEASE_VERSION} created and assets uploaded.")
