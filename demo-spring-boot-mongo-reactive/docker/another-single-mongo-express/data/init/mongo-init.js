db = db.getSiblingDB('admin')
db.createUser(   { "user": "admin_user", "pwd": "admin_pass", "roles": [ "userAdminAnyDatabase", { "role": "readWrite", "db": "config" }, "clusterAdmin" ] } );
db = db.getSiblingDB('app_db') 
db.createUser({ "user": "app_user","pwd": "app_pass","roles": [ { "role": "dbOwner", "db": "app_db" },{ "role": "userAdmin", "db": "app_db" },{ "role": "readWrite", "db": "app_db" } ] });
db.Employee.insert
(
	{
		"Employeeid" : 1,
		"EmployeeName" : "Martin"
	}
);
