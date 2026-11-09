#!/usr/bin/env bash
# Finite title/GUI probe. Outer workflow enforces a 420-second timeout.
set -u
bash ./gradlew runClient --no-daemon --args="--username InteriumSmoke --width 960 --height 540" > smoke.log 2>&1 &
pid=$!
for ((i=0;i<400;i++)); do
  if grep -q 'INTERIUM_GUI_SMOKE_RENDERED' smoke.log; then
    if command -v import >/dev/null; then import -window root build/smoke-gui.png || true; fi
    break
  fi
  if ! kill -0 "$pid" 2>/dev/null; then break; fi
  sleep 1
done
wait "$pid"
exit $?
