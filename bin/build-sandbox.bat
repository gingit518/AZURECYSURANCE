@ECHO OFF

set REACT_APP_ENV=sandbox
setx REACT_APP_ENV sandbox

call cd ..
call npm prune
call npm i
call npm run build

call rm -rfv "builds/sandbox"
call mkdir "builds/sandbox/build"
call cp -Rv ./build "builds/sandbox"
call cd bin
