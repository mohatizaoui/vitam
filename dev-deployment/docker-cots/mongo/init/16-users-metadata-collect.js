
// Switch to metadataCollect database
db = db.getSiblingDB('metadataCollect')

// Create metadataCollect user

if (! db.getUser("metadata-collect")) {
    db.createUser(
        {
            user: "metadata-collect",
            pwd: "azerty1",
            roles: [
                { role: "readWrite", db: "metadataCollect" }
            ]
        }
    )
}
else {
    db.updateUser(
        "metadata-collect",
        {
            pwd: "azerty1",
            roles: [
                { role: "readWrite", db: "metadataCollect" }
            ]
        }
    )
}
