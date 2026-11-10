# Source this file from an interactive zsh. Sourcing does not start services or create personal logs.
OTT_MANUAL_JAR=/Users/maria/Documentos/Francisco/ESR/ESR---TP2/Projeto/target/ott-streaming-1.0-SNAPSHOT.jar
OTT_MANUAL_PROFILES=/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/020-overlay/manual-profiles
OTT_MANUAL_PERSONAL_DIR=/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/020-overlay/manual-Francisco-execucao-01

ott_manual_run() {
    setopt localoptions localtraps
    # Keep the interactive function alive to record statuses when the foreground pipeline gets Ctrl-C.
    local -a ott_sigint_status
    trap 'ott_sigint_status=( "${pipestatus[@]}" )' INT
    if (( $# < 1 )); then print -u2 -- 'Usage: ott_manual_run unique-label [CLI arguments]'; return 2; fi
    local ott_label="$1"
    shift
    if [[ ! "$ott_label" =~ '^[A-Za-z0-9_-]+$' ]]; then print -u2 -- 'Invalid log label'; return 2; fi
    local ott_sha_line ott_actual_sha
    ott_sha_line=$(command shasum -a 256 "$OTT_MANUAL_JAR") || return 2
    ott_actual_sha=${ott_sha_line%% *}
    if [[ "$ott_actual_sha" != 303facafc148b96bd2cc0d98ea6afc7a773a50cb2b49d1d5c6c6c4c481de5b52 ]]; then print -u2 -- 'JAR hash mismatch; no Java process started'; return 2; fi
    ott_sha_line=$(command shasum -a 256 "$OTT_MANUAL_PROFILES/N0.properties") || return 2
    if [[ "${ott_sha_line%% *}" != 3ef471a3f80355271f0a24252f1970b7cbb9f9a38007c7d38c977bb117ab4864 ]]; then print -u2 -- 'Profile hash mismatch: N0.properties; no Java process started'; return 2; fi
    ott_sha_line=$(command shasum -a 256 "$OTT_MANUAL_PROFILES/N1.properties") || return 2
    if [[ "${ott_sha_line%% *}" != 9f8c2985a3265fc38779ce2b3e831617851fb0c86dcde3a78d5d9e2776af3889 ]]; then print -u2 -- 'Profile hash mismatch: N1.properties; no Java process started'; return 2; fi
    ott_sha_line=$(command shasum -a 256 "$OTT_MANUAL_PROFILES/N2.properties") || return 2
    if [[ "${ott_sha_line%% *}" != 7e73bfbed9da1d1b9535d7c7668afaa442d787de3d1de43f81bb526b881df0b6 ]]; then print -u2 -- 'Profile hash mismatch: N2.properties; no Java process started'; return 2; fi
    ott_sha_line=$(command shasum -a 256 "$OTT_MANUAL_PROFILES/bootstrap.properties") || return 2
    if [[ "${ott_sha_line%% *}" != d5f9c7b003b512f5043405066c2d3df18cc7cd1d6567942d3c769f5034a11c18 ]]; then print -u2 -- 'Profile hash mismatch: bootstrap.properties; no Java process started'; return 2; fi
    ott_sha_line=$(command shasum -a 256 "$OTT_MANUAL_PROFILES/streams-s1.properties") || return 2
    if [[ "${ott_sha_line%% *}" != 0850103529cae7cb7285612f1f9a1b6cd2b13ed2aaffc2a88d60e8d0d99c4445 ]]; then print -u2 -- 'Profile hash mismatch: streams-s1.properties; no Java process started'; return 2; fi
    ott_sha_line=$(command shasum -a 256 "$OTT_MANUAL_PROFILES/topology.properties") || return 2
    if [[ "${ott_sha_line%% *}" != 26719d5b185d9b7eadca95160d5be8c6b0c1a854cfc21c2675b1d9018219947e ]]; then print -u2 -- 'Profile hash mismatch: topology.properties; no Java process started'; return 2; fi
    command mkdir -p "$OTT_MANUAL_PERSONAL_DIR" || return 2
    local ott_log="$OTT_MANUAL_PERSONAL_DIR/$ott_label.log"
    local ott_exit="$OTT_MANUAL_PERSONAL_DIR/$ott_label.exit"
    if [[ -e "$ott_log" || -e "$ott_exit" ]]; then print -u2 -- 'Existing personal log: choose a new label; no overwrite'; return 2; fi
    {
        print -r -- 'OPERATOR=Francisco'
        command date -u '+STARTED_AT=%Y-%m-%dT%H:%M:%SZ'
        print -r -- "JAR_SHA256=$ott_actual_sha"
        printf 'COMMAND='
        printf '%q ' java -cp "$OTT_MANUAL_JAR" pt.uminho.esr.ott.app.OttApplication "$@"
        printf '\n'
    } > "$ott_log"
    local -a ott_pipeline_status
    ott_sigint_status=()
    command java -cp "$OTT_MANUAL_JAR" pt.uminho.esr.ott.app.OttApplication "$@" 2>&1 | command tee -a "$ott_log"
    ott_pipeline_status=( "${pipestatus[@]}" )
    if (( ${#ott_sigint_status} >= 2 )); then ott_pipeline_status=( "${ott_sigint_status[@]}" ); fi
    {
        printf 'JAVA_EXIT=%s\nTEE_EXIT=%s\n' "${ott_pipeline_status[1]}" "${ott_pipeline_status[2]}"
        command date -u '+FINISHED_AT=%Y-%m-%dT%H:%M:%SZ'
    } > "$ott_exit"
    command cat "$ott_exit"
    command cat "$ott_exit" >> "$ott_log"
    return "${ott_pipeline_status[1]}"
}
