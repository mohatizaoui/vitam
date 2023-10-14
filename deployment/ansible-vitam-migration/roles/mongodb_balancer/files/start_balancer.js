// https://www.mongodb.com/docs/manual/tutorial/manage-sharded-cluster-balancer/#enable-the-balancer

// Script to start the mongodb balancer
sh.startBalancer();
// Check if the balancer is properly started
if (!sh.getBalancerState()) {
    print("Failed to start the balancer");
    quit(1);
}
// Wait until the balancer is started
// The timeout is managed by ansible, not by the script
while (sh.isBalancerRunning().mode === 'off') {
    sleep(1000);
}
