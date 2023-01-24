print("Started Adding the Users.");
use admin
db.createUser({
  user: "app_user",
  pwd: "app_pass",
  roles: [{ role: "readWrite", db: "app_db" }],
});
print("End Adding the User Roles.");
db.testcollection.insert({ artist: "Mike" });

