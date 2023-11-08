#!node
const fs = require('fs');

const localEnvId = 'LOCAL';
const localSettings = {
    'url-access': 'access-external.service.consul:8444',
    "url-collect": 'localhost:8030',
    "url-ingest": 'ingest-external.service.consul:8443'
};
const environments = [
    'bu',
    'ga',
    'int',
    'int5rcx',
    'int5x',
    'int6rcx',
    'int6x',
    'int70x',
    'itrec',
    'itrec-2',
    'meu',
    'tac',
    'tic',
    'tic-2',
    'toc',
    'zo',
];

const settings = {
    "rest-client.environmentVariables": {
        [localEnvId]: Object.entries(localSettings).reduce((acc, [key, value]) => {
            acc[key] = `https://${value}`;
            return acc;
        }, {}),
        ...environments.reduce((acc, environment) => {
            acc[environment] = {
                "url-access": `https://${environment}.env-api.programmevitam.fr`,
                "url-collect": `https://${environment}.env-api.programmevitam.fr`,
                "url-ingest": `https://${environment}.env-api.programmevitam.fr`
            }
            return acc;
        }, {})
    },
    "rest-client.certificates": {
        ...Object.values(localSettings).reduce((acc, domain) => {
            acc[domain] = {
                "pfx": "/vitam/conf/ihm-demo/keystore_ihm-demo.p12",
                "passphrase": "azerty4"
            }
            return acc;
        }, {}),
        ...environments.reduce((acc, domain) => {
            acc[`${domain}.env-api.programmevitam.fr`] = {
                "cert": "/vitam/external_pub.pem",
                "key": "/vitam/external_key.pem"
            }
            return acc;
        }, {})
    },
    "rest-client.excludeHostsForProxy": []
}

fs.writeFileSync('settings.json', JSON.stringify(settings, null, 2), {encoding: 'utf8'})
