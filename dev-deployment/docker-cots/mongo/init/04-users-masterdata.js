
// Switch to masterdata database
db = db.getSiblingDB('masterdata')

// Create functional-admin user

if (! db.getUser("functional-admin")) {
    db.createUser(
        {
            user: "functional-admin",
            pwd: "azerty3",
            roles: [
                { role: "readWrite", db: "masterdata" }
            ]
        }
    )
}
else {
    db.updateUser(
        "functional-admin",
        {
            pwd: "azerty3",
            roles: [
                { role: "readWrite", db: "masterdata" }
            ]
        }
    )
}
