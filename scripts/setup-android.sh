#!/usr/bin/env bash
set -Eeuo pipefail
umask 022

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
tool_root="$repo_root/.gradle/hoplite-android"
downloads="$tool_root/downloads"
staging="$tool_root/staging"
sdk_root="$tool_root/sdk"
jdk_home="$tool_root/jdk-21.0.9+10"
jdk_archive='OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz'
jdk_checksum='OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz.sha256.txt'
stage=''

die() {
  printf 'setup-android: %s\n' "$*" >&2
  exit 1
}

cleanup() {
  if [[ -n "$stage" && -d "$stage" ]]; then
    rm -rf -- "$stage"
  fi
}
trap cleanup EXIT

for command_name in curl sha1sum sha256sum stat tar unzip grep; do
  command -v "$command_name" >/dev/null || die "required command not found: $command_name"
done
[[ "$(uname -s)" == Linux && "$(uname -m)" == x86_64 ]] || die 'this pinned toolchain is for Linux x86_64'

mkdir -p "$downloads" "$staging"

download() {
  local name="$1" url="$2" expected_size="$3"
  local file="$downloads/$name" part="$downloads/$name.part" actual_size

  if [[ -f "$file" ]]; then
    actual_size="$(stat -c '%s' "$file")"
    [[ "$actual_size" == "$expected_size" ]] && return
    rm -f -- "$file"
  fi
  if [[ -f "$part" && "$(stat -c '%s' "$part")" == "$expected_size" ]]; then
    mv -- "$part" "$file"
    return
  fi
  touch "$part"
  curl --fail --location --proto '=https' --proto-redir '=https' \
    --retry 5 --retry-delay 2 --connect-timeout 30 --continue-at - \
    --silent --show-error --output "$part" "$url"
  actual_size="$(stat -c '%s' "$part")"
  [[ "$actual_size" == "$expected_size" ]] || die "$name size mismatch: expected $expected_size, got $actual_size"
  mv -- "$part" "$file"
}

verify_sha1() {
  local name="$1" digest="$2" expected_size="$3" file="$downloads/$1"
  [[ -f "$file" ]] || die "missing archive: $name"
  [[ "$(stat -c '%s' "$file")" == "$expected_size" ]] || die "$name has an unexpected size"
  printf '%s  %s\n' "$digest" "$file" | sha1sum -c -
}

verify_sha256() {
  local name="$1" digest="$2" expected_size="$3" file="$downloads/$1"
  [[ -f "$file" ]] || die "missing archive: $name"
  [[ "$(stat -c '%s' "$file")" == "$expected_size" ]] || die "$name has an unexpected size"
  printf '%s  %s\n' "$digest" "$file" | sha256sum -c -
}

ensure_properties() {
  local properties_file="$1"
  shift
  [[ -f "$properties_file" ]] || die "missing package metadata: $properties_file"
  local property
  for property in "$@"; do
    grep -Fqx -- "$property" "$properties_file" || die "expected '$property' in $properties_file"
  done
}

install_zip() {
  local archive="$1" top_dir="$2" destination="$3" required_file="$4" label="$5"
  shift 5
  local properties_file="$destination/source.properties"

  if [[ -e "$destination" ]]; then
    ensure_properties "$properties_file" "$@"
    [[ -e "$destination/$required_file" ]] || die "$label is incomplete at $destination"
    printf 'already installed: %s\n' "$label"
    return
  fi

  stage="$(mktemp -d "$staging/${label//[^[:alnum:]]/_}.XXXXXX")"
  unzip -q "$downloads/$archive" -d "$stage"
  [[ -d "$stage/$top_dir" ]] || die "$archive does not contain $top_dir/"
  ensure_properties "$stage/$top_dir/source.properties" "$@"
  [[ -e "$stage/$top_dir/$required_file" ]] || die "$archive is missing $required_file"
  mkdir -p "$(dirname -- "$destination")"
  mv -- "$stage/$top_dir" "$destination"
  rm -rf -- "$stage"
  stage=''
  printf 'installed: %s\n' "$label"
}

download "$jdk_archive" \
  'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.9%2B10/OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz' \
  207157802
download "$jdk_checksum" \
  'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.9%2B10/OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz.sha256.txt' \
  116

download 'commandlinetools-linux-13114758_latest.zip' \
  'https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip' 164760899
download 'platform-36_r02.zip' \
  'https://dl.google.com/android/repository/platform-36_r02.zip' 65878410
download 'build-tools_r36_linux.zip' \
  'https://dl.google.com/android/repository/build-tools_r36_linux.zip' 63737259
download 'platform-tools_r37.0.1-linux.zip' \
  'https://dl.google.com/android/repository/platform-tools_r37.0.1-linux.zip' 9054187
download 'emulator-linux_x64-16740729.zip' \
  'https://dl.google.com/android/repository/emulator-linux_x64-16740729.zip' 349658551
download 'x86_64-30_r11.zip' \
  'https://dl.google.com/android/repository/sys-img/google_apis/x86_64-30_r11.zip' 1438274971

printf '%s\n' 'Verifying pinned downloads before extracting or running vendor code'
# Temurin values are from the release checksum sidecar and GitHub asset digest.
printf '%s  %s\n' \
  '1838ac1c4a0ad4a7eeff0fcc6605bb3c47f7567d783699497bee128727c8ee68' \
  "$downloads/$jdk_checksum" | sha256sum -c -
grep -Fqx -- \
  '810d3773df7e0d6c4394e4e244b264c8b30e0b05a0acf542d065fd78a6b65c2f  OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz' \
  "$downloads/$jdk_checksum" || die 'Temurin checksum sidecar does not match the pinned release'
verify_sha256 "$jdk_archive" \
  '810d3773df7e0d6c4394e4e244b264c8b30e0b05a0acf542d065fd78a6b65c2f' 207157802

# SHA-1 values below are the official Android repository2-3.xml package checksums.
verify_sha1 'commandlinetools-linux-13114758_latest.zip' \
  '5fdcc763663eefb86a5b8879697aa6088b041e70' 164760899
verify_sha1 'platform-36_r02.zip' \
  '2c1a80dd4d9f7d0e6dd336ec603d9b5c55a6f576' 65878410
verify_sha1 'build-tools_r36_linux.zip' \
  'b0b6376977657e8ad9b969bacf4093601da2c6fb' 63737259
verify_sha1 'platform-tools_r37.0.1-linux.zip' \
  '477254aa5f903c15cf51001717bdf347fb6b53e0' 9054187
verify_sha1 'emulator-linux_x64-16740729.zip' \
  'f41f9e31eb5ca64926603c03265c50bbc0ff4172' 349658551

# r11 is no longer in Google's live manifest. This hash is from the 2022-11-05 archived Google manifest:
# https://web.archive.org/web/20221105051221id_/https://dl.google.com/android/repository/sys-img/google_apis/sys-img2-1.xml
verify_sha1 'x86_64-30_r11.zip' \
  '8ec579d5fe31804dd80132f1678655bfc015609b' 1438274971

if [[ -e "$jdk_home" ]]; then
  [[ -x "$jdk_home/bin/java" ]] || die "incomplete JDK at $jdk_home"
  grep -Fqx 'JAVA_VERSION="21.0.9"' "$jdk_home/release" || die "unexpected JDK at $jdk_home"
  printf 'already installed: Temurin JDK 21.0.9+10\n'
else
  stage="$(mktemp -d "$staging/jdk.XXXXXX")"
  tar --no-same-owner --no-same-permissions -xzf "$downloads/$jdk_archive" -C "$stage"
  [[ -x "$stage/jdk-21.0.9+10/bin/java" ]] || die 'Temurin archive has an unexpected layout'
  mv -- "$stage/jdk-21.0.9+10" "$jdk_home"
  rm -rf -- "$stage"
  stage=''
  printf 'installed: Temurin JDK 21.0.9+10\n'
fi

install_zip 'commandlinetools-linux-13114758_latest.zip' 'cmdline-tools' \
  "$sdk_root/cmdline-tools/19.0" 'bin/sdkmanager' 'Command-line Tools 19.0' \
  'Pkg.Revision=19.0' 'Pkg.Path=cmdline-tools;19.0'
install_zip 'platform-36_r02.zip' 'android-36' \
  "$sdk_root/platforms/android-36" 'android.jar' 'Platform 36 r2' \
  'Pkg.Revision=2' 'AndroidVersion.ApiLevel=36'
install_zip 'build-tools_r36_linux.zip' 'android-16' \
  "$sdk_root/build-tools/36.0.0" 'aapt2' 'Build Tools 36.0.0' \
  'Pkg.Revision=36.0.0'
install_zip 'platform-tools_r37.0.1-linux.zip' 'platform-tools' \
  "$sdk_root/platform-tools" 'adb' 'Platform Tools 37.0.1' \
  'Pkg.Revision=37.0.1'
install_zip 'emulator-linux_x64-16740729.zip' 'emulator' \
  "$sdk_root/emulator" 'emulator' 'Emulator 37.2.13' \
  'Pkg.Revision=37.2.13' 'Pkg.BuildId=16740729'
install_zip 'x86_64-30_r11.zip' 'x86_64' \
  "$sdk_root/system-images/android-30/google_apis/x86_64" 'system.img' \
  'Android 30 Google APIs x86_64 r11' 'Pkg.Revision=11' \
  'AndroidVersion.ApiLevel=30' 'SystemImage.Abi=x86_64' 'SystemImage.TagId=google_apis'

cat > "$tool_root/env.sh" <<EOF
export JAVA_HOME="$jdk_home"
export ANDROID_HOME="$sdk_root"
export ANDROID_SDK_ROOT="$sdk_root"
export ANDROID_USER_HOME="$tool_root/android-user-home"
export PATH="$jdk_home/bin:$sdk_root/cmdline-tools/19.0/bin:$sdk_root/platform-tools:$sdk_root/emulator:$sdk_root/build-tools/36.0.0:\${PATH:-}"
EOF
chmod 0644 "$tool_root/env.sh"
mkdir -p "$tool_root/android-user-home"
# shellcheck disable=SC1090
source "$tool_root/env.sh"

mkdir -p "$sdk_root/licenses"
write_license() {
  local name="$1" hash="$2" file="$sdk_root/licenses/$1"
  if [[ -f "$file" ]]; then
    grep -Fqx -- "$hash" "$file" && return
    printf '%s\n' "$hash" >> "$file"
  else
    printf '%s\n' "$hash" > "$file"
  fi
}
# These are the only license IDs referenced by the pinned SDK repository entries.
write_license 'android-sdk-license' '24333f8a63b6825ea9c5514f83c2829b004d1fee'
write_license 'android-sdk-preview-license' '84831b9409646a918e30573bab4c9c91346d8abd'

printf '\n%s\n' '--- installed tool versions ---'
java -version
sdkmanager --version
adb version
emulator -no-window -version
printf '\nPinned JDK/SDK packages installed. Source %s\n' "$tool_root/env.sh"
