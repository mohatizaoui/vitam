# Making Rest request on VITAM
## Prerequisite

1. Having VSCode with RestClient extension installed or a Jetbrains IDE in version 2023.3 or later.
2. Having GIT
3. Having NodeJS

FIXME : générer depuis le script de vitam-internal-toolbox

## Installation

1. Clone both `vitam-internal-toolbox` and `vitam`:
```shell
git clone https://gitlab.dev.programmevitam.fr/vitam/vitam-internal-toolbox.git
git clone https://gitlab.dev.programmevitam.fr/vitam/vitam.git
```
2. Run the `generate-vitam-request-settings.js` script from `vitam-internal-toolbox` to generate the VSCode and Jetbrains configurations for making http requests on Vitam environments (supposing projects have been cloned next to each other, respectively in `vitam-internal-toolbox` and `vitam` directories):
```shell
./vitam-internal-toolbox/vitam-core/scripts/generate-vitam-request-settings/generate-vitam-request-settings.js ./vitam/
```
3. If using VSCode, open the `vitam/vitam-conf-dev/making-vitam-request/requests-workspace` directory with VSCode. If using Jetbrains, open the same directory or `vitam` directory to open the whole project.

## Usage

1. In VSCode or Jetbrains, open any `*.http` file (for example `units/search.http`)
2. Specify the `int.env-api.programmevitam.fr` environment
   - in VSCode, click on the `No Environnment` on the bottom right of VSCode
   - in Jetbrains, click on the `Run with:` select on the top of the opened file
3. Try to run a request, for example a search query in `units/search.http`
