# Making Rest request on VITAM
## Prerequisite
1. Having VSCode that can make Https request with certificate see [#18557](https://github.com/electron/electron/issues/18557) and [#18380](https://github.com/electron/electron/issues/18380)
2. Having RestClient VSCode extension installed
3. Having the VITAM certificate for the environments
4. Having GIT

## Installation
1. Put VITAM certificate in `/vitam/external_key.pem` and `/vitam/external_pub.pem`
2. Create a symbolic link (or copy) the `settings.json` file in `requests-workspace/.vscode/`.
3. Open the `requests-workspace` directory in VSCode

## Usage
1. In VSCode, specify the `INT` environment (click on the `No Environnment` on the bottom right of VSCode).
2. Try the search request (located in `vitam-conf-dev/making-vitam-request/requests-workspace/unit/search.http`).

## Update settings.json
Change environments in `update-settings.js` and run it with Node. It'll update the `settings.json` file. Copy it in `requests-workspace/.vscode/` if you didn't create a symbolic link.
