#!/usr/bin/env bash
set -euo pipefail

# Usage: ./produce-reserve.sh [COUNT] [EVENT_ID]
COUNT="${1:-9}"
EVENT_ID="${2:-6}"

# Get kafka container id
KAFKA_CID="$(docker ps --filter 'ancestor=wurstmeister/kafka:2.13-2.8.1' --format '{{.ID}}' | head -n1)"
if [[ -z "$KAFKA_CID" ]]; then
  echo "Kafka container not found. Is docker-compose up?"
  exit 1
fi

TOPIC="ticket.booking.reserve"

user_for() {
  local i="$1"
  case $(( i % 3 )) in
    0) echo "sophiab" ;;
    1) echo "jamesd" ;;
    *) echo "emmaj" ;;
  esac
}

payload() {
  local i="$1"
  local user q
  user="$(user_for "$i")"
  q=$(( (i % 3) + 1 ))  # 1..3
  printf '{"user":"%s","eventId":%d,"quantity":%d}' "$user" "$EVENT_ID" "$q"
}

# Map to 3 partitions
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

for ((i=1;i<=COUNT;i++)); do
  key="k$(( (i-1) % 3 ))"
  echo "${key}|$(payload "$i")" >> "$TMP"
done

echo "Producing $COUNT message(s) to $TOPIC (eventId=$EVENT_ID)…"
docker exec -i "$KAFKA_CID" bash -lc "/opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic '$TOPIC' \
  --property parse.key=true \
  --property key.separator='|'" < "$TMP"

echo "Done."
