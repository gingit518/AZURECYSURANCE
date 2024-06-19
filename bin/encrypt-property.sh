#!/bin/bash

############################################################
# Help                                                     #
############################################################
show_help()
{
   # Display Help
   echo "Property Encryption script for ValuRi$k application."
   echo
   echo "Syntax: encrypt-property.sh [-e|h|v|V]"
   echo "options:"
   echo "	-s     Secret string"
   echo "	-p     Property value"
   echo "	-h     Print this Help."
   echo "	-v     Verbose mode."
   echo "	-V     Print software version and exit."
   echo
}
############################################################

VERBOSE=""
# Get the options
while getopts ":hs:p:vV" OPTION; do
   case $OPTION in
      h) # display Help
         show_help
         exit 0;;
      s) # setup environment
         SECRET_ARG=${OPTARG};;
      p) # setup environment
         PROPERTY_VALUE=${OPTARG};;
      v)
         VERBOSE="YES";;
     \?) # Invalid option
         echo "Error: Invalid option"
         exit;;
   esac
done

echo "Detecting encryption options"

SECRET_VALUE=${RISKQ_SECRET}
if [ -z "$SECRET_VALUE" ];
then
  SECRET_VALUE=${SECRET_ARG}
fi

if [ -z "$SECRET_VALUE" ];
then
	echo "Secret is not defined"
	exit 1;
fi

if [ -z "$VERBOSE" ];
then
	echo "Encryption secret: ${SECRET_VALUE}";
fi

echo "Encryption property value is: ${PROPERTY_VALUE}";

mvn -f "./jasypt-pom.xml" jasypt:encrypt-value -Djasypt.encryptor.password=${SECRET_VALUE} -Djasypt.plugin.value="${PROPERTY_VALUE}"
