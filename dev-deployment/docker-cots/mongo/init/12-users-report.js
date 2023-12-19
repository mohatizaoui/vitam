// Switch to report database
db = db.getSiblingDB('report')

// Create report user

if (! db.getUser("report")) {
    db.createUser(
        {
            user: "report",
            pwd: "azerty5",
            roles: [
                { role: "readWrite", db: "report" }
            ]
        }
    )
}
else {
    db.updateUser(
        "report",
        {
            pwd: "azerty5",
            roles: [
                { role: "readWrite", db: "report" }
            ]
        }
    )
}
