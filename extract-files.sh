#!/bin/bash
#
# Copyright (C) 2016 The CyanogenMod Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if [ "${BASH_SOURCE[0]}" != "${0}" ]; then
    return
fi

set -e

DEVICE=potter
VENDOR=motorola

# Load extractutils and do some sanity checks
MY_DIR="${BASH_SOURCE%/*}"
if [[ ! -d "${MY_DIR}" ]]; then MY_DIR="${PWD}"; fi

ANDROID_ROOT="${MY_DIR}/../../.."

HELPER="${ANDROID_ROOT}/tools/extract-utils/extract_utils.sh"
if [ ! -f "$HELPER" ]; then
    echo "Unable to find helper script at $HELPER"
    exit 1
fi
. "$HELPER"

if [ $# -eq 0 ]; then
  SRC=adb
else
  if [ $# -eq 1 ]; then
    SRC=$1
  else
    echo "$0: bad number of arguments"
    echo ""
    echo "usage: $0 [PATH_TO_EXPANDED_ROM]"
    echo ""
    echo "If PATH_TO_EXPANDED_ROM is not specified, blobs will be extracted from"
    echo "the device using adb pull."
    exit 1
  fi
fi

function blob_fixup() {
    case "${1}" in
    vendor/lib/libwvhidl.so)
        patchelf --replace-needed "libprotobuf-cpp-lite.so" "libprotobuf-cpp-lite-v28.so" "${2}"
    ;;
    vendor/lib64/libsettings.so)
        patchelf --replace-needed "libprotobuf-cpp-full.so" "libprotobuf-cpp-full-v28.so" "${2}"
    ;;
    esac
}

# Initialize the helper
setup_vendor "$DEVICE" "$VENDOR" "$ANDROID_ROOT"

extract "${MY_DIR}"/proprietary-files.txt "$SRC" "$SECTION"

BLOB_ROOT="$AOSP_ROOT"/vendor/"$VENDOR"/"$DEVICE"/proprietary

if [ -s "${MY_DIR}"/../$DEVICE/proprietary-files.txt ]; then
    # Reinitialize the helper for device
    setup_vendor "$DEVICE" "$VENDOR" "$ANDROID_ROOT" false "$CLEAN_VENDOR"

    extract "${MY_DIR}"/../$DEVICE/proprietary-files.txt "$SRC" "$SECTION"
fi

# Load libmot_gpu_mapper shim
MOT_GPU_MAPPER="$BLOB_ROOT"/vendor/lib/libmot_gpu_mapper.so
patchelf --add-needed libgpu_mapper_shim.so "$MOT_GPU_MAPPER"


"${MY_DIR}"/setup-makefiles.sh
