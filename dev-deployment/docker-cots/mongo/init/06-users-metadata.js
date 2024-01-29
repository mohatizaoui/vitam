
// Switch to metadata database
db = db.getSiblingDB('metadata')

// Create metadata user

if (! db.getUser("metadata")) {
    db.createUser(
        {
            user: "metadata",
            pwd: "azerty1",
            roles: [
                { role: "readWrite", db: "metadata" }
            ]
        }
    )
}
else {
    db.updateUser(
        "metadata",
        {
            pwd: "azerty1",
            roles: [
                { role: "readWrite", db: "metadata" }
            ]
        }
    )
}
