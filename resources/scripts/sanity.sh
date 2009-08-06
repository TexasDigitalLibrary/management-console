#!/bin/sh

echo "========================="
echo "Starting sanity tests...."
echo "========================="
echo ""

scriptdir=`dirname "$0"`
. "$scriptdir"/common.sh

echo "================================"
echo "Installing UnHosted Dependencies"
echo "================================"
echo ""
cd $BUILD_HOME 
resources/scripts/installLibs.sh

echo "=================="
echo "Starting Tomcat..."
echo "=================="
echo ""
if [ -z $CATALINA_HOME ]; then
  echo "ERROR: Need to set CATALINA_HOME"
  exit 1
fi

$CATALINA_HOME/bin/startup.sh

echo ""
echo "==================================="
echo "Compiling & running unit & integration tests..."
echo "==================================="
$M2_HOME/bin/mvn clean install -Dtomcat.port.default=9090 -Ddatabase.home.default=/home/bamboo/duraspace-home/derby/duraspaceDB -Dlog.level.default=DEBUG

if [ $? -ne 0 ]; then
  echo ""
  echo "ERROR: Integration test(s) failed; see above"
  $CATALINA_HOME/bin/shutdown.sh
  exit 1
fi

echo "======================="
echo "Shutting Down Tomcat..."
echo "======================="
echo ""
$CATALINA_HOME/bin/shutdown.sh


echo ""
echo "===================================="
echo "Completed sanity tests successfully!"
echo "===================================="