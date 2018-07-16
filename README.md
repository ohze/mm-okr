## mm-okr

#### Dev guide
+ run mattermost locally for dev 
```bash
docker run --name mattermost-preview -d --publish 8065:8065 --add-host dockerhost:127.0.0.1 mattermost/mattermost-preview
docker exec -ti mattermost-preview /bin/bash
```
```bash
cd bin
./mattermost user create --email shepherd@sandinh.net --username shepherd --system_admin --password ttpublic
```

+ goto http://<your ip>:8065
(`ifconfig` to show your ip)

add user `thanhbv` & team `Ohze`
add some channel, ex `okr-shepherd`

+ Team Settings / Allow any user with an account on this server to join this team

+ System Console / ACDVANCED / Developer Settings / Allow untrusted internal connections to:
add <your ip>

+ System Console / Custom Integrations / Enable integrations to override usernames

+ login using user `shepherd`, join `Ohze`

add personal token, incomming webhook, slash command `/o` with Request URL: http://<your ip>:9000/o

Then update into application.conf

+ run mm-okr
```bash
sbt run
```
+ compile & check running
http://<your ip>:9000

+ Nhắc mọi người thêm okr
vào MM (bằng user bất kỳ & ở kênh bất kỳ), gửi slash command sau:
```
/o okr thanhbv,Team Chăn cừu,okr-shepherd; lamnt,Team 52 lá, team-52-la
```
với:
  - `/o` là command
  - `okr` là để nhắc user thêm okr
  - `thanhbv,Team Chăn cừu,okr-shepherd` là để nhắc `thanhbv` thêm OKR cho `Team Chăn cừu` & sau khi thêm thì các OKRs
    sẽ được notify vào channel `okr-shepherd`
  - Nhắc các user khác tương tự (ngăn cách bằng dấu `;`)
  