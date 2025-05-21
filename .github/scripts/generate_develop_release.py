import os
import subprocess
import requests
import datetime
import json
from pathlib import Path

REPO = "digital-preservation/droid"
RELEASE_TAG = "develop"
API_BASE = "https://api.github.com"
TOKEN = os.environ["GITHUB_TOKEN"]
HEADERS = {
    "Authorization": f"Bearer {TOKEN}",
    "Accept": "application/vnd.github+json"
}

def run(cmd):
    return subprocess.check_output(cmd, text=True).strip()

def get_commit_sha(branch):
    return run(["git", "rev-parse", f"origin/{branch}"])

def is_ancestor(ancestor, descendant):
    return subprocess.call(["git", "merge-base", "--is-ancestor", ancestor, descendant]) == 0

def fetch_merged_prs():
    url = f"{API_BASE}/repos/{REPO}/pulls"
    params = {"state": "closed", "base": "develop", "per_page": 100}
    prs = requests.get(url, headers=HEADERS, params=params)
    prs.raise_for_status()
    return [pr for pr in prs.json() if pr.get("merged_at")]

def get_release():
    url = f"{API_BASE}/repos/{REPO}/releases/tags/{RELEASE_TAG}"
    release = requests.get(url, headers=HEADERS)
    release.raise_for_status()
    return release.json()

def update_release_notes(release_id, body):
    url = f"{API_BASE}/repos/{REPO}/releases/{release_id}"
    data = {"body": body}
    r = requests.patch(url, headers=HEADERS, json=data)
    r.raise_for_status()

def delete_assets(asset_ids):
    for asset_id in asset_ids:
        url = f"{API_BASE}/repos/{REPO}/releases/assets/{asset_id}"
        r = requests.delete(url, headers=HEADERS)
        r.raise_for_status()
        print(f"🗑️  Deleted asset {asset_id}")

def upload_asset(upload_url, file_path):
    file_name = Path(file_path).name
    with open(file_path, "rb") as f:
        print(f"⬆️  Uploading {file_name}...")
        headers = HEADERS.copy()
        headers["Content-Type"] = "application/zip"
        r = requests.post(f"{upload_url}?name={file_name}", headers=headers, data=f)
        r.raise_for_status()

def main():
    # Ensure Git branches are up to date
    run(["git", "fetch", "origin", "develop", "main"])
    develop_sha = get_commit_sha("develop")
    main_sha = get_commit_sha("main")

    print("🔍 Fetching merged PRs...")
    prs = fetch_merged_prs()
    new_prs = ""

    for pr in prs:
        pr_number = pr["number"]
        pr_title = pr["title"]
        pr_author = pr["user"]["login"]
        merge_commit = pr["merge_commit_sha"]

        if not is_ancestor(merge_commit, main_sha):
            new_prs += f"- PR [#{pr_number}] {pr_title} (@{pr_author})\n"

    if not new_prs:
        print("✅ No new PRs to include.")
        return

    timestamp = datetime.datetime.utcnow().strftime("%Y-%m-%d %H:%M UTC")
    body = f"### Changes not yet in main:\n\n{new_prs}\n_Last updated: {timestamp}_"

    release = get_release()
    release_id = release["id"]
    upload_url = release["upload_url"].split("{")[0]

    update_release_notes(release_id, body)

    print("🗑️ Deleting existing assets...")
    asset_ids = [a["id"] for a in release.get("assets", [])]
    delete_assets(asset_ids)

    print("⬆️ Uploading new assets...")
    print("⬆️ Uploading Windows Jar...")
    windows_jar = next(Path("droid-binary/target").glob("*win64-with-jre.zip"))
    print("⬆️ Uploading Generic Jar...")
    generic_jar = next(Path("droid-binary/target").glob("*bin.zip"))
    upload_asset(upload_url, windows_jar)
    upload_asset(upload_url, generic_jar)

    print("✅ Release updated successfully.")

if __name__ == "__main__":
    main()
