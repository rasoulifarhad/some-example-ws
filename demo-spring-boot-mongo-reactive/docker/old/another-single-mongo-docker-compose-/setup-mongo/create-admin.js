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
db.testcollection.insert({ artist: "Mike" });
