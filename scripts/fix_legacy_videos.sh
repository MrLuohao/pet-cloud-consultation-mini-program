#!/usr/bin/env bash
set -euo pipefail

# Batch-fix legacy videos for WeChat Mini Program compatibility:
# H.264 (yuv420p) + AAC + faststart, overwrite original file path.
#
# Usage:
#   bash scripts/fix_legacy_videos.sh /absolute/path/to/uploads
# Example:
#   bash scripts/fix_legacy_videos.sh /Users/luohao/Desktop/WeChat_MiniProgramDev/pet-cloud-consultation/uploads

UPLOAD_DIR="${1:-}"
if [[ -z "${UPLOAD_DIR}" ]]; then
  echo "Usage: $0 <uploads_dir>"
  exit 1
fi

if [[ ! -d "${UPLOAD_DIR}" ]]; then
  echo "Directory not found: ${UPLOAD_DIR}"
  exit 1
fi

if ! command -v ffmpeg >/dev/null 2>&1; then
  echo "ffmpeg is required but not found in PATH"
  exit 1
fi

if ! command -v ffprobe >/dev/null 2>&1; then
  echo "ffprobe is required but not found in PATH"
  exit 1
fi

echo "Scanning mp4 files under: ${UPLOAD_DIR}"

mapfile -d '' FILES < <(find "${UPLOAD_DIR}" -type f -name "*.mp4" -print0)
TOTAL="${#FILES[@]}"
echo "Found ${TOTAL} mp4 file(s)"

SUCCESS=0
SKIPPED=0
FAILED=0

for FILE in "${FILES[@]}"; do
  # Skip if already AVC/H.264 to reduce unnecessary re-encode
  VCODEC="$(ffprobe -v error -select_streams v:0 -show_entries stream=codec_name -of csv=p=0 "${FILE}" || true)"
  if [[ "${VCODEC}" == "h264" ]]; then
    SKIPPED=$((SKIPPED + 1))
    continue
  fi

  TMP="${FILE}.transcoding.mp4"
  echo "Transcoding: ${FILE} (codec=${VCODEC:-unknown})"

  if ffmpeg -y -i "${FILE}" \
    -c:v libx264 -pix_fmt yuv420p -profile:v main -level 4.1 \
    -c:a aac -b:a 128k \
    -movflags +faststart \
    "${TMP}" >/dev/null 2>&1; then
    mv -f "${TMP}" "${FILE}"
    SUCCESS=$((SUCCESS + 1))
  else
    rm -f "${TMP}" || true
    echo "Failed: ${FILE}"
    FAILED=$((FAILED + 1))
  fi
done

echo "Done. success=${SUCCESS}, skipped=${SKIPPED}, failed=${FAILED}, total=${TOTAL}"
