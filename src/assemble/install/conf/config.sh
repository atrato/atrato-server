#
# Copyright (c) 2017 Atrato, Inc. ALL Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

# DO NOT MODIFY THIS FILE - customize settings in env.sh

# load custom settings
PWD="$( cd -P "$( dirname "$BASH_SOURCE" )" && pwd )"
[[ -f "${PWD}/env.sh" ]] && . "${PWD}/env.sh"

realDir() {
  SOURCE="${1:-${BASH_SOURCE[0]}}"
  while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
    SOURCE_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
    SOURCE="$(readlink "$SOURCE")"
    [[ $SOURCE != /* ]] && SOURCE="$SOURCE_DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
  done
  SOURCE_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  echo "$SOURCE_DIR"
}

findHadoop() {
  HADOOP_SEARCH_PATH="${HADOOP_PREFIX}/bin:${HADOOP_HOME}/bin:${PATH}:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:."
  HADOOP=`PATH=${HADOOP_SEARCH_PATH} && command -v hadoop 2>/dev/null`
  [[ $? -eq 0 ]] && echo "${HADOOP}"
}

findJava() {
  JAVA_SEARCH_PATH="${JAVA_HOME}/bin:${PATH}:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:."
  JAVA=`PATH=${JAVA_SEARCH_PATH} && command -v java 2>/dev/null`
  [[ $? -eq 0 ]] && echo "${JAVA}"
}

ATRATO_HADOOP_CMD="${ATRATO_HADOOP_CMD:-$(findHadoop)}"
ATRATO_JAVA_CMD="${ATRATO_JAVA_CMD:-$(findJava)}"

ATRATO_VERSION=${project.version}
ATRATO_ROOT_DIR="${ATRATO_ROOT_DIR:-${HOME}/atrato}"
ATRATO_LOG_DIR="${ATRATO_LOG_DIR:-${ATRATO_ROOT_DIR}/logs}"
ATRATO_RUN_DIR="${ATRATO_RUN_DIR:-${ATRATO_ROOT_DIR}/run}"
ATRATO_RELEASES_DIR="${ATRATO_ROOT_DIR}/releases"
ATRATO_RELEASE_DIR="${ATRATO_RELEASES_DIR}/${ATRATO_VERSION}"
ATRATO_HOME="${ATRATO_RELEASE_DIR}"

# Export environment variables
for var in ATRATO_HOME ATRATO_LOG_DIR ATRATO_RUN_DIR ATRATO_HADOOP_CMD ATRATO_JAVA_CMD; do export $var; done

# for apex script compatibility
DT_HADOOP=$ATRATO_HADOOP_CMD

