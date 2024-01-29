// Switch to identity database
db = db.getSiblingDB('identity')

// Create security-internal user

if (! db.getUser("security-internal")) {
    db.createUser(
        {
            user: "security-internal",
            pwd: "azerty4",
            roles: [
                { role: "readWrite", db: "identity" }
            ]
        }
    )
}
else {
    db.updateUser(
        "security-internal",
        {
            pwd: "azerty4",
            roles: [
                { role: "readWrite", db: "identity" }
            ]
        }
    )
}
