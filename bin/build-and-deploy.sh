#!/bin/bash

DEFAULT_JAVA_HOME=/usr/lib/jvm/jdk-17.0.4

############################################################
# Help                                                     #
############################################################
show_help()
{
   # Display Help
   echo "Deployment script for ValuRi$k application."
   echo
   echo "Syntax: build-and-deploy.sh [-e|h|v|V]"
   echo "options:"
   echo "	-e     Environment name. Allowed values: demo, dev, qa, staging, nip, melon"
   echo "	-h     Print this Help."
   echo "	-v     Verbose mode."
   echo "	-V     Print software version and exit."
   echo
}
############################################################

# Get the options
while getopts ":he:vV" OPTION; do
   case $OPTION in
      h) # display Help
         show_help
         exit 0;;
      e) # setup environment
         ENVIRONMENT_NAME=${OPTARG};;
     \?) # Invalid option
         echo "Error: Invalid option"
         exit;;
   esac
done

echo "Detecting ENVIRONMENT"

SERVER_URL=""
REMOTE_DESTINATION_PATH="/home/azureuser/opt"
case $ENVIRONMENT_NAME in
  dev)
  	SERVER_URL="dev.app.risk-q.com"
  	;;
  demo)
  	SERVER_URL="demo.app.risk-q.com"
  	;;
  qa)
  	SERVER_URL="qa.app.risk-q.com"
  	;;
  smb-demo)
  	SERVER_URL="smb-demo.app.risk-q.com"
  	;;
  staging)
  	SERVER_URL="staging.app.risk-q.com"
  	;;
  nip)
  	SERVER_URL="nip.app.risk-q.com"
  	;;
  mellon)
  	SERVER_URL="mellon.app.risk-q.com"
  	;;
  crossriver)
  	SERVER_URL="crossriver.app.risk-q.com"
  	;;
  valurisq)
  	SERVER_URL="valurisq.app.risk-q.com"
  	;;

  *)
    echo "Invalid environment [$ENVIRONMENT_NAME]. Aborting!"
    exit 2
    ;;
esac

echo "Starting build process for Environment: $ENVIRONMENT_NAME"
echo "Detected remote server: $SERVER_URL"

CURRENT_DIR=$(pwd)
cd ..
WORK_DIR=$(pwd)

echo "Setting up working dir: ${WORK_DIR}"
echo "Set JAVA_HOME to [${DEFAULT_JAVA_HOME}]"
echo "Building executable SPRINGBOOT applications"
JAVA_HOME=$DEFAULT_JAVA_HOME mvn clean package -DskipTests=true -e -P spring-boot || exit 1


rm -rfv "build/*.jar"
mkdir "build"
cp -Rv ./api/target/api-core.jar "build"
cp -Rv ./idp/target/idp.jar "build"
echo "Copying target file to remove destination: $SERVER_URL:$REMOTE_DESTINATION_PATH/api/api-core.jar"
## scp -C ./api/target/api-core.jar "staging.app.risk-q.com:/home/azureuser/opt/api/api-core.jar"
scp -C ./api/target/api-core.jar "$SERVER_URL:$REMOTE_DESTINATION_PATH/api/api-core.jar"
echo "Copying target file to remove destination: $SERVER_URL:$REMOTE_DESTINATION_PATH/idp/idp.jar"
scp -C ./idp/target/idp.jar "$SERVER_URL:$REMOTE_DESTINATION_PATH/idp/idp.jar"
echo "Restarting API service on the Remote Server"
ssh $SERVER_URL "sudo systemctl restart api-core.service"
ssh $SERVER_URL "sudo systemctl restart idp.service"
cd ${CURRENT_DIR}

exit;
