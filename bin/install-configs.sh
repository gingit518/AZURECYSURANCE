#!/bin/bash

ACTION_NAME=$1
APPLICATION_NAME=$2
TARGET_DIR=$3
LINUX_USER=azureuser

if [ -z "$DATABASE_PORT" ];
then
  DATABASE_PORT=23306
fi

if [ "$ACTION_NAME" == "--help" ]; then
   echo "Usage:"
   echo "./install-configs.sh [command] [application-name] [work-dir]"
   echo ""
   echo "Commands:"
   echo "install  Install configurations for Application"

   exit 0;
fi

if [ "$ACTION_NAME" == "install" ]; then
    echo "Installing NGinx config for: ${APPLICATION_NAME}"

    mkdir -p ${TARGET_DIR}/etc/nginx/
   echo "user ${LINUX_USER};
 worker_processes auto;
 pid /run/nginx.pid;
 include /etc/nginx/modules-enabled/*.conf;

 events {
		 worker_connections 768;
		 # multi_accept on;
 }

 http {

		 ##
		 # Basic Settings
		 ##

		 sendfile on;
		 tcp_nopush on;
		 types_hash_max_size 2048;
		 # server_tokens off;

		 # server_names_hash_bucket_size 64;
		 # server_name_in_redirect off;

		 # Set Client Body parameters
		 client_body_buffer_size 512m;
		 client_header_buffer_size 8k;
		 large_client_header_buffers 64 16k;
		 client_max_body_size 20m;

		 include /etc/nginx/mime.types;
		 default_type application/octet-stream;

		 ##
		 # SSL Settings
		 ##

		 ssl_protocols TLSv1 TLSv1.1 TLSv1.2 TLSv1.3; # Dropping SSLv3, ref: POODLE
		 ssl_prefer_server_ciphers on;

		 ##
		 # Logging Settings
		 ##

		 access_log /var/log/nginx/access.log;
		 error_log /var/log/nginx/error.log;

		 ##
		 # Gzip Settings
		 ##

		 gzip on;

		 # gzip_vary on;
		 # gzip_proxied any;
		 # gzip_comp_level 6;
		 # gzip_buffers 16 8k;
		 # gzip_http_version 1.1;
		 # gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

		 ##
		 # Virtual Host Configs
		 ##

		 include /etc/nginx/conf.d/*.conf;
		 include /etc/nginx/sites-enabled/*;
 }" > ${TARGET_DIR}/etc/nginx/nginx.conf

    mkdir -p ${TARGET_DIR}/etc/nginx/sites-available/
    mkdir -p ${TARGET_DIR}/etc/nginx/sites-enabled/
    echo "server
 {
   listen 80 default_server;
   return 301 https://\$host\$request_uri;
 }

 server
 {
   listen 443 ssl;
   server_name ${APPLICATION_NAME}.app.risk-q.com;
   add_header Access-Control-Allow-Origin \"*\";
   root /home/${LINUX_USER}/opt/core-ui/build;
   include le.conf;
   ssl on;
   ssl_certificate /etc/letsencrypt/live/${APPLICATION_NAME}-api.app.risk-q.com/fullchain.pem;
   ssl_certificate_key /etc/letsencrypt/live/${APPLICATION_NAME}-api.app.risk-q.com/privkey.pem;
   ssl_session_timeout 5m;
   ssl_protocols SSLv3 TLSv1 TLSv1.1 TLSv1.2;
   ssl_ciphers \"HIGH:!aNULL:!MD5 or HIGH:!aNULL:!MD5:!3DES\";
   ssl_prefer_server_ciphers on;

   location / {
	 index index.html;
	 add_header Last-Modified \"\";
	 add_header Cache-Control \"public, max-age=0, must-revalidate\";

	 try_files \$uri \$uri/ /index.html =404;
   }
 }

 server
 {
   listen 443 ssl;
   server_name ${APPLICATION_NAME}-admin.app.risk-q.com;
   add_header Access-Control-Allow-Origin \"*\";
   root /home/${LINUX_USER}/opt/admin-ui/build;
   include le.conf;
   ssl on;
   ssl_certificate /etc/letsencrypt/live/${APPLICATION_NAME}-api.app.risk-q.com/fullchain.pem;
   ssl_certificate_key /etc/letsencrypt/live/${APPLICATION_NAME}-api.app.risk-q.com/privkey.pem;
   ssl_session_timeout 5m;
   ssl_protocols SSLv3 TLSv1 TLSv1.1 TLSv1.2;
   ssl_ciphers \"HIGH:!aNULL:!MD5 or HIGH:!aNULL:!MD5:!3DES\";
   ssl_prefer_server_ciphers on;


   location / {
	 index index.html;
	 add_header Last-Modified \"\";
	 add_header Cache-Control \"public, max-age=0, must-revalidate\";

	 try_files \$uri \$uri/ /index.html =404;
   }
 }

 server
 {
   listen 443 ssl;
   server_name ${APPLICATION_NAME}-api.app.risk-q.com;
   #add_header 'Access-Control-Allow-Origin' '*';
   include le.conf;
   ssl on;
   ssl_certificate /etc/letsencrypt/live/${APPLICATION_NAME}-api.app.risk-q.com/fullchain.pem;
   ssl_certificate_key /etc/letsencrypt/live/${APPLICATION_NAME}-api.app.risk-q.com/privkey.pem;
   ssl_session_timeout 5m;
   ssl_protocols SSLv3 TLSv1 TLSv1.1 TLSv1.2;
   ssl_ciphers \"HIGH:!aNULL:!MD5 or HIGH:!aNULL:!MD5:!3DES\";
   ssl_prefer_server_ciphers on;

   # IDP server
   location ~ ^/(login|logout|oauth|actuator|2factor) {
	 proxy_set_header X-Real-IP \$remote_addr;
	 proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
	 proxy_set_header Host \$http_host;
	 proxy_set_header X-NginX-Proxy true;
	 proxy_pass http://127.0.0.1:8081;
   }

   # API Server
   location / {
	 proxy_set_header X-Real-IP \$remote_addr;
	 proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
	 proxy_set_header Host \$http_host;
	 proxy_set_header X-NginX-Proxy true;
	 proxy_pass http://127.0.0.1:8080;
   }
 }" > ${TARGET_DIR}/etc/nginx/sites-enabled/riskq.conf
# }" > ${TARGET_DIR}/etc/nginx/sites-available/riskq.conf
# ln -s ${TARGET_DIR}/etc/nginx/sites-available/riskq.conf ${TARGET_DIR}/etc/nginx/sites-enabled/riskq.conf
rm -rf ${TARGET_DIR}/etc/nginx/sites-enabled/default
#
echo "location ~ /.well-known {
	try_files \$uri \$uri/ =404;
	allow all;
	log_not_found off;
	root /var/www/html/;
}" > ${TARGET_DIR}/etc/nginx/le.conf

echo "Installing Java Service configurations for: ${APPLICATION_NAME}"
mkdir -p ${TARGET_DIR}/home/${LINUX_USER}/opt/api/config
echo "JAVA_OPTS=\"-Djdk.tls.client.protocols=TLSv1.2 -Dspring.profiles.active=apiapp,scheduler,azure\"
RUN_ARGS=-Dserver.port=8188
" > ${TARGET_DIR}/home/${LINUX_USER}/opt/api/api-core.conf
echo "
# Server Configuration
server.port=8080
server.tomcat.max-threads=1000

vrisk.api.url=https://${APPLICATION_NAME}-api.app.risk-q.com
vrisk.ui.url=https://${APPLICATION_NAME}.app.risk-q.com
vrisk.admin-ui.url=https://${APPLICATION_NAME}-admin.app.risk-q.com

vrisk.setup-mode.enabled=true
vrisk.notifications.email.enabled=true
vrisk.notifications.email.from=Do Not Reply <noreply@cyberinnovativetech.com>

# Thymeleaf settings
spring.thymeleaf.cache=false
spring.thymeleaf.check-template-location=true
spring.thymeleaf.prefix=classpath:/templates/

# DataBase
spring.datasource.url=jdbc:postgresql://valurisq-postgres.postgres.database.azure.com:5432/valurisq_${APPLICATION_NAME}?stringtype=unspecified
spring.datasource.username=ENC(1f48McS33rQSjPtlfovf+ZprHb0vXCEpXmd8ON4UbpMhHQcErnUTch5SA7H7Jx1i)
spring.datasource.password=ENC(EDhNe2Raq352LYB24P1AvkyH6uUwBvDZw+D/9B50FN5uJHEyoSGNINzJD/5ecAVD)
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.platform=postgresql
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.database=postgresql
spring.jpa.generate-ddl=false
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=none
spring.jpa.hibernate.enable_lazy_load_no_trans=true
spring.security.oauth2.resource.filter-order=3

## JMS Settings
spring.jms.servicebus.connection-string=ENC(Sy6GTOvJ48Wi5mRrbKKd3j9g4nrEN2FhgwM6lfd0ODmwPu8g2JhOw88gyLAF7FiDcMXzppcmLYVhs6g9D2K3RRrSqFxx5ZSClhRSzmp1/H8KcDHN5nHCtaVxJcb6YFHhjDgwrCYiQA1vlDDQVc9iw2UO0qGiZgXxTPMFOeonEYILn+uXvok2u1P++Ljevt2nF29Hgile5mF2z+8CSfUhFoDF682MSkpuRMVxydZcg5TmbkJ4qMOtSMWB7hQhkKk8)
spring.jms.servicebus.idle-timeout=1800000
spring.jms.servicebus.pricing-tier=standard
application.queue.common=riskq-live-${APPLICATION_NAME}

# Analytics Settings
#analytics.powerbi.authenticationType=ServicePrincipal
#analytics.powerbi.authorityUrl=https://login.microsoftonline.com/
#analytics.powerbi.scopeBase=https://analysis.windows.net/powerbi/api/.default
#analytics.powerbi.tenantId=fc371555-8bd2-4c98-9e42-e82be54ddb36
#analytics.powerbi.subscriptionId=39cf4421-39df-4262-a32f-cb1baaf5555a
#analytics.powerbi.resourceGroup=PowerBI_Reports
#analytics.powerbi.clientId=b0ff74e0-818d-4b3e-8756-5f1c5ec5b35e
#analytics.powerbi.appSecret=ENC(3KyfRCwd2YazMs8NjL9uN7j3urMv/DJWIY3J35kewO6B9xgUYNu+caemM0Fdzn+Kz8PzAn9BagEuyDwul+P7m3lEZ8UTYc7YH45C2WxGtjI=)
#analytics.powerbi.capacity=${APPLICATION_NAME}
#analytics.powerbi.capacityExpirationMinutes=30

# Azure Settings
cloud.azure.communication.email.connectionString=ENC(4zu3GOb6BwDjBeemLbTvkP8b9KxHt7gILXs9Y6QG1SFUaiyQUeaJ7TO8uAS3Ys2i6cW0gW/aFE5mYyykuwX34dmKiO8LePkzSf0W5P3TPmYWf24hdzsKItQ68yRcz2ofTrbmcW7obKQ3Q91GyFB9LzwFtkoUdrLKM0uuNuAFlNU1go4NMoQ/EBjftdmxnepEQImHtJmA6rvDkNQqDktXyPpwoNgoeAWwi+DdjT3Rc/7stGqnII3Am//6U6lJd7fDSwnbQwileFfkhM58p0ERXchB7aPjupun0V5+lxcN6cfhZeTngsIgYfm2sL5aK5y/)
cloud.azure.communication.email.from=DoNotReply@azure-email.app.risk-q.com
cloud.azure.storage.accountEndpoint=https://riskqstoragelive.blob.core.windows.net
cloud.azure.storage.connectionString=ENC(qDa4907O6weWZRTgYwLEqVyABCOfLL6MrlXxpQ9I19l4le3mbgdUl2/h/FiTci27KzD9E32HOhDvk7JAXEhAZ7mvoyb3qMpmcVj4O6/la66uiQn9q0OC+oOTH9bMg2D+zzNyKoRiX3m4X4Lw+z7RJfx5qj41vhsVfcmE07OffmYtL67fWibnjHUYVD1vYYH+DqFdjqOfvJELEgtzFK0iEVBzTIZkB8nqFDqyTc0Fy/gTIxd3lAn9I6HfP5NXYxbr8cSTOWxsrRz8NEewj4cBmdAJMSR97OyINbd0llJSD/M=)
cloud.azure.storage.blobContainer=riskq-application-${APPLICATION_NAME}
cloud.azure.storage.blobContainerRead=riskq-application-${APPLICATION_NAME}-read

# Actuator Test
management.endpoints.enabled-by-default=false
management.endpoint.info.enabled=true
management.endpoint.health.enabled=true
management.endpoint.health.show-details=always
management.health.db.enabled=true

# Twilio SMS Provider
twilio.sid=AC58ff579ddaa81d02418ce05ce0124ab4
twilio.token=ENC(iABTF9HiDRQI5iUBhEnWChwqShDq6NR0L8fAgGXqxasEaeqXmUROeV9Dub7NSjcLas9ONxqlmQPpKQ/vaLvp+cWBqjDtHim2J1BMIENuED8=)
twilio.from=+18482510604

logging.level.org.springframework=WARN
logging.level.org.apache.http=DEBUG
logging.level.com.cyberintech=DEBUG
logging.level.com.cyberintech.vrisk.server.model.dto=WARN

# Default Logging
logging.level.=DEBUG
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN
" > ${TARGET_DIR}/home/${LINUX_USER}/opt/api/config/application-apiapp.properties

mkdir -p ${TARGET_DIR}/home/${LINUX_USER}/opt/idp/config
echo "
# Server Configuration
server.port=8081
server.tomcat.max-threads=1000

vrisk.idp.url=https://${APPLICATION_NAME}-api.app.risk-q.com
vrisk.api.url=https://${APPLICATION_NAME}.app.risk-q.com
vrisk.ui.url=https://${APPLICATION_NAME}.app.risk-q.com
vrisk.admin-ui.url=https://${APPLICATION_NAME}-admin.app.risk-q.com

# DataBase
spring.datasource.url=jdbc:postgresql://valurisq-postgres.postgres.database.azure.com:5432/valurisq_${APPLICATION_NAME}?stringtype=unspecified
spring.datasource.username=ENC(1f48McS33rQSjPtlfovf+ZprHb0vXCEpXmd8ON4UbpMhHQcErnUTch5SA7H7Jx1i)
spring.datasource.password=ENC(EDhNe2Raq352LYB24P1AvkyH6uUwBvDZw+D/9B50FN5uJHEyoSGNINzJD/5ecAVD)
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.platform=postgresql
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.database=postgresql
spring.jpa.generate-ddl=false
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=none
spring.jpa.hibernate.enable_lazy_load_no_trans=true
spring.security.oauth2.resource.filter-order=3

## JMS Settings
spring.jms.servicebus.connection-string=ENC(Sy6GTOvJ48Wi5mRrbKKd3j9g4nrEN2FhgwM6lfd0ODmwPu8g2JhOw88gyLAF7FiDcMXzppcmLYVhs6g9D2K3RRrSqFxx5ZSClhRSzmp1/H8KcDHN5nHCtaVxJcb6YFHhjDgwrCYiQA1vlDDQVc9iw2UO0qGiZgXxTPMFOeonEYILn+uXvok2u1P++Ljevt2nF29Hgile5mF2z+8CSfUhFoDF682MSkpuRMVxydZcg5TmbkJ4qMOtSMWB7hQhkKk8)
spring.jms.servicebus.idle-timeout=1800000
spring.jms.servicebus.pricing-tier=standard
application.queue.common=riskq-live-${APPLICATION_NAME}

application.oauth.client.id=com.vrisk
application.oauth.client.signature=21827392bacff

# Twilio SMS Provider
twilio.sid=AC58ff579ddaa81d02418ce05ce0124ab4
twilio.token=ENC(iABTF9HiDRQI5iUBhEnWChwqShDq6NR0L8fAgGXqxasEaeqXmUROeV9Dub7NSjcLas9ONxqlmQPpKQ/vaLvp+cWBqjDtHim2J1BMIENuED8=)
twilio.from=+18482510604

# Actuator Test
management.endpoints.enabled-by-default=false
management.endpoint.info.enabled=true
management.endpoint.health.enabled=true
management.endpoint.health.show-details=always
management.health.db.enabled=true

# Loggins

logging.level.org.springframework=WARN
logging.level.com.cyberintech=INFO
logging.level.com.cyberintech.vrisk.server.model.dto=INFO

# Default Logging
logging.level.=WARN
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN

" > ${TARGET_DIR}/home/${LINUX_USER}/opt/idp/config/application.properties

# Creating UI Folders
mkdir -p ${TARGET_DIR}/home/${LINUX_USER}/opt/admin-ui
mkdir -p ${TARGET_DIR}/home/${LINUX_USER}/opt/core-ui

echo "Installing SystemD Service files for: ${APPLICATION_NAME}"
mkdir -p ${TARGET_DIR}/etc/systemd/system/
echo "[Unit]
Description=\"api-core\"
After=syslog.target

[Service]
User=${LINUX_USER}
Environment=\"JAVA_HOME=/usr/lib/jvm/jdk-17\"
WorkingDirectory=/home/${LINUX_USER}/opt/api
ExecStart=/home/${LINUX_USER}/opt/api/api-core.jar
SuccessExitStatus=143

StandardOutput=truncate:/var/log/riskq/api.log
StandardError=truncate:/var/log/riskq/api.log

[Install]
WantedBy=multi-user.target
" > ${TARGET_DIR}/etc/systemd/system/api-core.service
echo "[Unit]
Description=\"idp\"
After=syslog.target

[Service]
User=${LINUX_USER}
Environment=\"JAVA_HOME=/usr/lib/jvm/jdk-17\"
WorkingDirectory=/home/${LINUX_USER}/opt/idp
ExecStart=/home/${LINUX_USER}/opt/idp/idp.jar
SuccessExitStatus=143

StandardOutput=truncate:/var/log/riskq/idp.log
StandardError=truncate:/var/log/riskq/idp.log

[Install]
WantedBy=multi-user.target" > ${TARGET_DIR}/etc/systemd/system/idp.service
# systemctl enable api-core.service
# systemctl enable idp.service

   exit 0;
fi
