// https://www.mongodb.com/docs/manual/release-notes/6.0-upgrade-sharded-cluster/#refresh-the-cached-routing-table-for-each-mongos.

print(db.adminCommand({ flushRouterConfig: 1 }));
