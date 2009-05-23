#!/bin/sh

# Written for cygwin, would work under Unix with slight mods.
# Assumes that gpsbabel is installed and in the path.  Tested with version 1.2.3.
# Assumes junit is in lib/junit.jar.
# Assumes log4j is in lib/log4j-1.2.8.jar.
YEAR=`grep ^index.year fetchnoaa.properties | sed -e 's/^.*=//' -e 's/\/[^/]*$//'`
echo
echo Convert noaaStormCoords$YEAR.gpx to noaaStormCoords$YEAR.wpt
echo

gpsbabel -i gpx -f noaaStormCoords$YEAR.gpx -o mapsend -F noaaStormCoords$YEAR.wpt
if [ $? -eq 0 ]; then
	echo gpsbabel conversion from XML to Magellan mapsend.
	echo See gpslabel help for converting to other formats.
else
	echo gpsbabel is either not installed or not in your PATH.
	echo If you need to download it, go to http://gpsbabel.sourceforge.net/
fi

cp -p noaaStormCoords$YEAR.wpt /c/Program\ Files/Magellan/MapSend\ Topo\ US/DOCS
