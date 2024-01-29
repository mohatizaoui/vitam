
// Switch to identity database
db = db.getSiblingDB('offer')

// Create offer user

if (! db.getUser("offer")) {
    db.createUser(
        {
            user: "offer",
            pwd: "azerty5",
            roles: [
                { role: "readWrite", db: "offer" }
            ]
        }
    )
}
else {
    db.updateUser(
        "offer",
        {
            pwd: "azerty5",
            roles: [
                { role: "readWrite", db: "offer" }
            ]
        }
    )
}
