az ssh vm --resource-group VALURISQ-PRODUCTION --vm-name VALURISQ-WESTORANGE --subscription 39cf4421-39df-4262-a32f-cb1baaf5555a

sudo apt update
sudo apt install mc
wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.deb
sudo dpkg -i jdk-17_linux-x64_bin.deb
sudo ln -s /usr/lib/jvm/jdk-17.0.12-oracle-x64/ /usr/lib/jvm/jdk-17
sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-17/bin/java 1
sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk-17/bin/javac 1
sudo update-alternatives --config java
sudo update-alternatives --config javac
sudo apt-get install nginx
sudo apt install -y certbot
# sudo certbot certonly -a webroot --webroot-path=/var/www/html --agree-tos -d devazure-api.app.risk-q.com -d devazure.app.risk-q.com -d devazure-admin.app.risk-q.com --email eugene@risk-q.com --server="https://acme-v02.api.letsencrypt.org/directory" --email=eugene@risk-q.com
sudo certbot certonly -a webroot --webroot-path=/var/www/html --agree-tos -d insurq-api.app.risk-q.com -d insurq.app.risk-q.com -d insurq-admin.app.risk-q.com --email eugene@risk-q.com --server="https://acme-v02.api.letsencrypt.org/directory" --email=eugene@risk-q.com
sudo cp -r ./etc/* /etc
sudo cp -r ./home/* /home
