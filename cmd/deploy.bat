REM https://console.ng.bluemix.net/docs/starters/upload_app.html
bluemix api https://api.ng.bluemix.net
bluemix login -u x@x.com

cf push app-name -p build/libs/watson-poc-1.0-SNAPSHOT.jar

REM http://appname.mybluemix.net/