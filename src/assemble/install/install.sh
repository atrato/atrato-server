#!/bin/bash -x

DEBUG=0

log() { printf "%b\n" "$*"; }
debug() { (( ${DEBUG} )) && log "DEBUG: $*"; }
error() { log "\nERROR: $*\n" ; }

realDir() {
  SOURCE="${1:-${BASH_SOURCE[0]}}"
  while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
    SOURCE_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
    SOURCE="$(readlink "$SOURCE")"
    [[ $SOURCE != /* ]] && SOURCE="$SOURCE_DIR/$SOURCE" # if $SOURCE was a relative symlink, resolve it relative to the path where the symlink file was located
  done
  SOURCE_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  echo "$SOURCE_DIR"
}
# find physical source directory
SCRIPT_DIR=$(realDir "${BASH_SOURCE[0]}")
SCRIPT_NAME=$(basename "${BASH_SOURCE[0]}")

abortInstall() {
  kill -s TERM $$
}

# only local install supported right now
if [ $(id -ur) -ne 0 ]; then
  LOCAL_INSTALL=1
  debug "Installing locally in home directory."
else
  error "Root install currently not supported."
  abortInstall
fi

# replace or add variables in env file
updateEnvFile() {
  CONF_FILE=${1:-${SCRIPT_DIR}/conf/env.sh}
  for var in ATRATO_HADOOP_CMD; do 
    var_arg="${var}_ARG"
    var_arg_value="${!var_arg}"
    if [[ -n "${var_arg_value}" ]]; then
      debug "Modifying $var=$var_arg_value in $CONF_FILE"
      grep -q "^\s*${var}=" "${CONF_FILE}" 2>/dev/null && sed -i "s|^\s*$var=.*$|$var=$var_arg_value|" "${CONF_FILE}" || echo "${var}=${var_arg_value}" >> "${conf_file}"
    fi
  done
}

# record custom install variables
updateEnvFile "${SCRIPT_DIR}/conf/env.sh"
. "${SCRIPT_DIR}/conf/env-system.sh" || ( error "Unable to find ${SCRIPT_DIR}/conf/env-system.sh"; abortInstall )

if [[ -z ${ATRATO_HADOOP_CMD} ]]; then
  debug "hadoop command not found"
  if [[ -z ${ATRATO_JAVA_CMD} ]]; then
    error "java not found"
    log "Please ensure java or hadoop are installed and available in PATH environment variable before proceeding with this installation."
    abortInstall
  else
    JAVA_VERSION=$("$ATRATO_JAVA_CMD" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "$JAVA_VERSION" > "1.7" ]]; then
        debug "Using java: $ATRATO_JAVA_CMD version ${JAVA_VERSION}"
    else 
        error "Unsupported $ATRATO_JAVA_CMD version ${JAVA_VERSION}."
        log "Please ensure java 1.7 or later is installed and available in PATH environment variable before proceeding with this installation."
        abortInstall
    fi
  fi
else
  debug "Using hadoop: $ATRATO_HADOOP_CMD"
  HADOOP_VERSION=`$ATRATO_HADOOP_CMD version | head -1`
  if [[ $? != 0 ]]; then
    error "Failed to execute '$ATRATO_HADOOP_CMD version'. Please check the Hadoop installation."
    abortInstall
  fi
  if ! [[ "$HADOOP_VERSION" =~ 'Hadoop 2.'[6-9]+ ]]; then
    error "Atrato Server ${ATRATO_VERSION} only supports Hadoop 2.6 or later. We found $HADOOP_VERSION installed."
    abortInstall
  fi
fi

copyFilesToTargetDir() {
  for d in "${ATRATO_RELEASES_DIR}" "${ATRATO_LOG_DIR}" "${ATRATO_RUN_DIR}"; do
    mkdir -p "${d}" || (error "Unable to create directory ${d}"; exit_install; )
  done
  [[ "${SCRIPT_DIR}" == "${ATRATO_RELEASE_DIR}"  ]] && return 0
  [[ -e "${ATRATO_RELEASE_DIR}" ]] && rm -rf "${ATRATO_RELEASE_DIR}"
  debug "copying from ${SCRIPT_DIR} to ${ATRATO_RELEASE_DIR}"
  cp -r "${SCRIPT_DIR}"  "${ATRATO_RELEASE_DIR}" || (error "Unable to copy installation to ${ATRATO_RELEASE_DIR}"; abortInstall; )
  # for apex script compatibility
  ln -nsf "env-system.sh" "${ATRATO_RELEASE_DIR}"/conf/dt-env.sh
}

localStart() {
  debug "Starting server.."
  (cd ${ATRATO_RELEASE_DIR} && "${ATRATO_RELEASE_DIR}/bin/atrato-daemon" start server)
  if [ $? -ne 0 ]; then
    error "Failed to start the server. Please check ${ATRATO_LOG_DIR} for errors".
    abortInstall
  fi
}

localStop() {
  if [[ -f "${ATRATO_RELEASE_DIR}/bin/atrato-daemon" ]]; then
    debug "Stopping server (if running)."
    "${ATRATO_RELEASE_DIR}/bin/atrato-daemon" stop server
  fi      
}

linkAsCurrentRelease() {
  rm -f "${ATRATO_RELEASES_DIR}"/current
  ln -nsf "${ATRATO_RELEASE_DIR}" "${ATRATO_RELEASES_DIR}"/current
}

# Run installation
echo ""
echo "Atrato ${ATRATO_VERSION} will be installed under ${ATRATO_RELEASE_DIR}"
echo ""
debug "---------- Performing installation ----------"
localStop
copyFilesToTargetDir
linkAsCurrentRelease
localStart

exit 0
