## Mattermost OKR

### Roadmap
+ [x] Permit using MM slash command `/o` to remind users creating OKRs for teams.
+ [x] The reminded users can interact with mm-okr bot to create OKRs for team,
    then the created OKRs will be post to the corresponding MM channel (of the team).
+ [ ] Edit OKRs
+ [ ] Check-in
+ [ ] Auto remind user according to company's OKR process settings
+ [ ] Measure the OKR applying process of the company (according to company's OKR process settings)
+ [ ] Publish to docker.io

### Dev guide
+ run mattermost locally for dev 
```bash
docker run --name mattermost-preview -d --publish 8065:8065 --add-host dockerhost:127.0.0.1 mattermost/mattermost-preview
docker exec -ti mattermost-preview /bin/bash
```
```bash
cd bin
./mattermost user create --email shepherd@sandinh.net --username shepherd --system_admin --password ttpublic
```

+ goto http://`<your ip>`:8065
(`ifconfig` to show your ip)

add user `thanhbv` & team `Ohze`
add some channel, ex `okr-shepherd`

+ Team Settings / Allow any user with an account on this server to join this team

+ System Console / ACDVANCED / Developer Settings / Allow untrusted internal connections to:
add `<your ip>`

+ System Console / Custom Integrations / Enable integrations to override usernames

+ login using user `shepherd`, join `Ohze`

add personal token, incomming webhook, slash command `/o` with Request URL: http://`<your ip>`:9000/o

Then update into application.conf

+ run mm-okr
```bash
sbt run
```
+ compile & check running
http://`<your ip>`:9000

+ remind user to add okrs
In MM (any user, in any channel), send the following slash command:
```
/o okr thanhbv,Team Chăn cừu,okr-shepherd; lamnt,Team 52 lá, team-52-la
```
With:
  - `/o` is the MM slash command
  - `okr` for reminding user to add okrs
  - `thanhbv,Team Chăn cừu,okr-shepherd` remind `thanhbv` to add OKRs cho `Team Chăn cừu` & the created OKRs
    will be posted into `okr-shepherd` channel
  - remind other users similarly (separated by `;`)

### Licence
This software is licensed under the Apache 2 license:
http://www.apache.org/licenses/LICENSE-2.0

Copyright (C) 2018 Sân Đình (https://sandinh.com)
