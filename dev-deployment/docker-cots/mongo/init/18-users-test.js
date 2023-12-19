
// Switch to test database
db = db.getSiblingDB('vitam-test')

// Create user-metadata user

if (! db.getUser("user-metadata")) {
    db.createUser(
        {
            user: "user-metadata",
            pwd: "user-metadata",
            roles: [
                { role: "readWrite", db: "vitam-test" }
            ]
        }
    )
}
else {
    db.updateUser(
        "user-metadata",
        {
            pwd: "user-metadata",
            roles: [
                { role: "readWrite", db: "vitam-test" }
            ]
        }
    )
}
