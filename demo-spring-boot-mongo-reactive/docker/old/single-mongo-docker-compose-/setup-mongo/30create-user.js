use admin
db.createUser(
{
    user: "app_user",
    pwd: "app_pass",
    roles: [
      {role: 'dbOwner',  db: 'app_db' }
    ]
});
db.testcollection.insert({ artist: "Mike" });
