#! /bin/expect -f

# Usage:
# publishnoaa.sh <filename> <directory>

set mbhost [mbhost]
set username [username]
set password [normalpass]
set timeout -1

cd /java/workspace1/SnarfNOAA

spawn sftp $username@$mbhost
waitForPasswordPrompt
sleep 1

send "$password\r"
waitForShellPrompt

send "cd mainwebsite_html/noaa\r"
waitForShellPrompt

send "mput noaaStormCoords20*gpx\r"
waitForShellPrompt

send "quit\r"

