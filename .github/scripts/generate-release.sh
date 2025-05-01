#!/bin/bash

set -euo pipefail

REPO="digital-preservation/droid"
RELEASE_TAG="develop"

# Make sure the local branches are up-to-date
git fetch origin develop main

# Get SHAs of develop and main
DEVELOP_SHA=$(git rev-parse origin/develop)
MAIN_SHA=$(git rev-parse origin/main)

# Get all PRs merged into develop
merged_prs=$(gh pr list --repo "$REPO" \
  --base develop \
  --state merged \
  --json number,title,mergeCommit,author \
  --jq '.[]')
echo $merged_prs
new_prs=""

while read -r pr_json; do
  pr_number=$(echo "$pr_json" | jq -r '.number')
  pr_title=$(echo "$pr_json" | jq -r '.title')
  pr_author=$(echo "$pr_json" | jq -r '.author.login')
  pr_merge_commit=$(echo "$pr_json" | jq -r '.mergeCommit.oid')
  if ! git merge-base --is-ancestor "$pr_merge_commit" "$MAIN_SHA"; then
    new_prs+="- PR [#$pr_number] $pr_title (@$pr_author)"'\n'
  fi
done < <(echo "$merged_prs" | jq -c '.')
if [ -z "$new_prs" ]; then
  exit 0
fi

now=$(date -u +"%Y-%m-%d %H:%M UTC")
new_prs+="_Last updated: $now""_"
echo -e $new_prs > notes

gh release edit "$RELEASE_TAG" \
  --repo "$REPO" \
  --notes-file notes

ASSET_IDS=$(gh api "repos/$REPO/releases/tags/$RELEASE_TAG" | jq -r '.assets[].id')

if [ -z "$ASSET_IDS" ]; then
else
  for ASSET_ID in $ASSET_IDS; do
    gh api -X DELETE "repos/$REPO/releases/assets/$ASSET_ID"
  done
fi

windows_jar=$(ls droid-binary/target/*win64-with-jre.zip)
jar=$(ls droid-binary/target/*bin.zip)
gh release upload "$RELEASE_TAG" $windows_jar --repo "$REPO" --clobber
gh release upload "$RELEASE_TAG" $jar --repo "$REPO" --clobber
rm notes