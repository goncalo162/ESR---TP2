#!/usr/bin/env bash
set -euo pipefail
demo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_dir="$(cd -- "$demo_dir/.." && pwd)"
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
javac_bin="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
jar_path="${DEMO_JAR:-$repo_dir/Projeto/target/ott-streaming-1.0-SNAPSHOT.jar}"
if [[ ! -f "$jar_path" ]]; then
    echo 'Falta o JAR. Executa primeiro: cd Projeto && mvn -B -ntp clean verify' >&2
    exit 2
fi
classes_dir="$demo_dir/.classes"
mkdir -p "$classes_dir"
"$javac_bin" --release 21 -cp "$jar_path" -d "$classes_dir" "$demo_dir/src/demo/Json.java" "$demo_dir/src/demo/DelayProxy.java" "$demo_dir/src/demo/CheckpointDemo.java" "$demo_dir/src/pt/uminho/esr/ott/overlay/DemoNode.java"
exec "$java_bin" -cp "$jar_path:$classes_dir" demo.CheckpointDemo "$repo_dir" "$jar_path" "${1:-cenario2}" "${@:2}"
