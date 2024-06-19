if [ ! -f "environment/sandbox01.json" ]; then
  printf "{\n    \"DEBUG\": false,\n    \"production\": false,\n    \"clientTimeFormat\": \"D MMMM YYYY\",\n    \"serviceUrl\": \"https://sandbox-api.app.cyberinnovativetech.com\",\n    \"websiteUrl\": \"https://sandbox.app.cyberinnovativetech.com\",\n    \"apiPath\": \"/api\",\n    \"base\": \"com.vrisk:21827392bacff\",\n    \"GOOGLE_CLIENT_ID\": \"70875112487-c3mfg6jejui45vb66m6nvaed79mrib1e.apps.googleusercontent.com\",\n    \"MICROSOFT_CLIENT_ID\": \"1aaac7c7-e391-4fb1-93e6-5a1c1db70368\"\n}" > environment/sandbox01.json

  echo "Creating Environment file: environment/sandbox01.json ..."
else
  echo "Environment file already exists. Skipping ..."
fi

npm i
REACT_APP_ENV=sandbox01 npm run build
tar -cvzf ../core-ui.tgz ./build
cp ../core-ui.tgz ./
rm ../core-ui.tgz
