#!/bin/bash

if [ ! -f "environment/demo01.json" ]; then
  printf "{\n    \"DEBUG\": false,\n    \"production\": false,\n    \"clientTimeFormat\": \"D MMMM YYYY\",\n    \"serviceUrl\": \"https://demo-api.app.cyberinnovativetech.com\",\n    \"websiteUrl\": \"https://demo.app.cyberinnovativetech.com\",\n    \"apiPath\": \"/api\",\n    \"base\": \"com.vrisk:21827392bacff\",\n    \"GOOGLE_CLIENT_ID\": \"70875112487-c3mfg6jejui45vb66m6nvaed79mrib1e.apps.googleusercontent.com\",\n    \"MICROSOFT_CLIENT_ID\": \"4af250c7-965d-4b0b-aafa-3bfbcb4213d5\"\n}" > environment/demo01.json

  echo "Creating Environment file: environment/demo01.json ..."
else
  echo "Environment file already exists. Skipping ..."
fi

npm i
REACT_APP_ENV=demo01 npm run build
tar -cvzf ../core-ui.tgz ./build
cp ../core-ui.tgz ./
rm ../core-ui.tgz
