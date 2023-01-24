use admin
db.createUser(
{
    user: "admin",
    pwd: "admin",
    roles: [
      { role: "userAdminAnyDatabase", db: "admin" },
      { role: "readWrite", db: "admin" },
      "readWriteAnyDatabase"
    ]
});
db.createUser(
{
    user: "app_user",
    pwd: "app_pass",
    roles: [
      {role: 'dbOwner',  db: 'app_db' }
    ]
});
db.testcollection.insert({ artist: "Mike" });

