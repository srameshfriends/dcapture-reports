```
 sudo nano /etc/systemd/system/dcapture-reports.service
```

```
[Unit]
Description=DCapture Reports Service
After=syslog.target

[Service]
User=dcapture
Group=dcapture
WorkingDirectory=/opt/dcapture/reports/data
ExecStart=java -jar /opt/dcapture/reports/dcapture-reports.jar
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target

```

```
sudo systemctl daemon-reload
```

```
sudo groupadd dcapture
sudo useradd -s /bin/false -g dcapture -d /opt/dcapture dcapture
```

```
chmod g+rx /opt/dcapture/reports/dcapture-reports.jar

sudo chgrp -R dcapture /opt/dcapture
sudo chown -R dcapture /opt/dcapture

sudo systemctl enable dcapture-reports
sudo systemctl start dcapture-reports
sudo systemctl stop dcapture-reports

```