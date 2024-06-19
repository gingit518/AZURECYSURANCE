@ECHO OFF

call cd ..
set REACT_APP_ENV=demo
setx REACT_APP_ENV demo
call npm prune
call npm i
call npm run build

call rm -rfv "builds/demo"
call mkdir "builds/demo/build"
call cp -Rv ./build "builds/demo"
call cd bin
