#!/bin/bash

DEFAULT_JAVA_HOME=/usr/lib/jvm/jdk-17.0.4

############################################################
# Help                                                     #
############################################################
show_help()
{
   # Display Help
   echo "Backup script for ValuRi\$k databases."
   echo
   echo "Syntax: backup-database.sh [-e|h|v|V]"
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

CURRENT_DATE=$(date +%Y%m%d)
SERVER_URL="valurisq-postgres.postgres.database.azure.com"
BACKUPS_PATH="/media/storage/dumps/riskq/"
DATABASE_NAME="valurisq_${ENVIRONMENT_NAME}"
case $ENVIRONMENT_NAME in
  dev)
  	SERVER_URL="valurisq-postgres-dev.postgres.database.azure.com"
  	;;
  demo)
  	SERVER_URL="valurisq-postgres-dev.postgres.database.azure.com"
  	;;
  qa)
  	SERVER_URL="valurisq-postgres-dev.postgres.database.azure.com"
  	;;
  staging)
  	SERVER_URL="valurisq-postgres-dev.postgres.database.azure.com"
  	;;
  crossriver)
  	SERVER_URL="valurisq-postgres.postgres.database.azure.com"
  	;;
  valurisq)
  	SERVER_URL="valurisq-postgres.postgres.database.azure.com"
  	;;
  quakerhoughton)
  	SERVER_URL="valurisq-postgres.postgres.database.azure.com"
  	;;

  *)
    echo "Unknown environment [$ENVIRONMENT_NAME]. Using Default settings: "
    ;;
esac

DUMP_FILE_NAME="${BACKUPS_PATH}valurisq_${ENVIRONMENT_NAME}.${CURRENT_DATE}.backup.sql"

echo "Starting Backup process for Environment: $ENVIRONMENT_NAME"
echo "========== ========== ========== ========== ========== ========== ========== ========== ========== ========== =========="
echo "    Detected Database server: $SERVER_URL"
echo "    Detected Database name: $DATABASE_NAME"
echo "    Output file name: $DUMP_FILE_NAME"
echo ""
echo "Running DUMP command:"
echo "pg_dump --username=riskqsa -h ${SERVER_URL} -p 5432 -F p -b -v -O -x -f ${DUMP_FILE_NAME} ${DATABASE_NAME}"
echo "========== ========== ========== ========== ========== ========== ========== ========== ========== ========== =========="
echo ""

pg_dump --username=riskqsa -h ${SERVER_URL} -p 5432 -F p -b -v -O -x -f ${DUMP_FILE_NAME} ${DATABASE_NAME}

echo "========== ========== ========== ========== ========== ========== ========== ========== ========== ========== =========="
ls -al ${DUMP_FILE_NAME}
echo "OK"

exit;
