#!/usr/bin/env bash

# requires curl & jq

# upstreamCommit --paper HASH --pufferfish HASH
# flag: --paper HASH - (Optional) the commit hash to use for comparing commits between paper (PaperMC/Paper/compare/HASH...HEAD)
# flag: --pufferfish HASH - the commit hash to use for comparing commits between pufferfish (pufferfish-gg/Pufferfish/compare/HASH...HEAD)

function getCommits() {
    curl -H "Accept: application/vnd.github.v3+json" https://api.github.com/repos/"$1"/compare/"$2"..."$3" | jq -r '.commits[] | "'"$1"'@\(.sha[:7]) \(.commit.message | split("\r\n")[0] | split("\n")[0])"'
}

(
set -e
PS1="$"

purpurHash=$(git diff gradle.properties | awk '/^-purpurRef =/{print $NF}')

TEMP=$(getopt --long purpur:,pufferfish: -o "" -- "$@")
eval set -- "$TEMP"
while true; do
    case "$1" in
        --purpur)
            purpurHash="$2"
            shift 2
            ;;
        *)
            break
            ;;
    esac
done

purpur=""
updated=""
logsuffix=""

# Purpur updates
if [ -n "$purpurHash" ]; then
    newHash=$(git diff gradle.properties | awk '/^+purpurRef =/{print $NF}')
    purpur=$(getCommits "PurpurMC/Purpur" "$purpurHash" $(echo $newHash | grep . -q && echo $newHash || echo "HEAD"))

    # Updates found
    if [ -n "$purpur" ]; then
        updated="Purpur"
        logsuffix="$logsuffix\n\Purpur Changes:\n$purpur"
    fi
fi

if [ -z "${updated}" ]; then
  echo "No changes detected"
  exit 1
fi

disclaimer="Upstream has released updates that appear to apply and compile correctly"
log="Updated Upstream ($updated)\n\n${disclaimer}${logsuffix}"

git add gradle.properties

echo -e "git commit -m $log"

) || exit 1