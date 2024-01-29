
// Switch to logbook database
db = db.getSiblingDB('logbook')

// Create logbook user

if (! db.getUser("logbook")) {
    db.createUser(
        {
            user: "logbook",
            pwd: "azerty2",
            roles: [
                { role: "readWrite", db: "logbook" }
            ]
        }
    )
}
else {
    db.updateUser(
        "logbook",
        {
            pwd: "azerty2",
            roles: [
                { role: "readWrite", db: "logbook" }
            ]
        }
    )
}
