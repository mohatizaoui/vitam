// Switch to collect database
db = db.getSiblingDB('collect')

// Create collect user

if (! db.getUser("collect")) {
    db.createUser(
        {
            user: "collect",
            pwd: "azerty6",
            roles: [
                { role: "readWrite", db: "collect" }
            ]
        }
    )
}
else {
    db.updateUser(
        "collect",
        {
            pwd: "azerty6",
            roles: [
                { role: "readWrite", db: "collect" }
            ]
        }
    )
}
