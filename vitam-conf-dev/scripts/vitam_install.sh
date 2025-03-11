#!/bin/bash

##### Installation de nvm
echo "Installation de nvm"
if ! command -v nvm &> /dev/null; then
  curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
fi

export NVM_DIR="$([ -z "${XDG_CONFIG_HOME-}" ] && printf %s "${HOME}/.nvm" || printf %s "${XDG_CONFIG_HOME}/nvm")"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

##### Mise en place Node
echo "Mise en place de node..."
nvm install 18 &> /dev/null
nvm use 18 &> /dev/null

##### Vérification si docker tourne

if ! command -v docker &> /dev/null; then
  echo "Docker non détecté ! Veuillez l'installer."
  exit 1
fi

if ! systemctl is-active --quiet docker
then
    echo "Docker Docker est installé. Démarrage"

##### Démarrage Docker
    if sudo systemctl start docker
    then
        echo "Docker à démarré avec succès."
    else
        echo "Démarrage échoué"
        exit 1
    fi
else
    echo "Docker est installé et fonctionne."
fi


##### Création arborescence
echo "création de l'arborescence..."
if [ -d '/vitam/' ]; then
  echo "Vitam existe déjà, veuillez le supprimer!"
  exit 7
fi
sudo mkdir -p /vitam/
sudo mkdir -p /vitam/conf/
sudo mkdir -p /vitam/data/ihm-recette/test-data/
sudo mkdir -p /vitam/data/storage/
sudo mkdir -p /vitam/log/
sudo mkdir -p /vitam/tmp/

##### TODO: de-hard code
sudo chown -R $USER:$USER /vitam

touch /vitam/data/storage/fr.gouv.vitam.storage.offers.workspace.driver.DriverImpl
echo "offer-fs-1.service.consul" >> "/vitam/data/storage/fr.gouv.vitam.storage.offers.workspace.driver.DriverImpl"

cp -r ../../vitam-conf-dev/conf/* /vitam/conf/

rm /vitam/conf/metadata/mapping/unit-es-mapping.json /vitam/conf/metadata/mapping/og-es-mapping.json


ln -s  ../../deployment/environments/files/elasticsearch-mappings/og-es-mapping.json /vitam/conf/metadata/mapping/
ln -s ../../deployment/environments/files/elasticsearch-mappings/unit-es-mapping.json /vitam/conf/metadata/mapping/

rm /vitam/conf/worker/plugins.json

ln -s ../../deployment/environments/files/elasticsearch-mappings/plugins.json /vitam/conf/worker/plugins.json


##### Compilation

echo "Compilation"

 mvn -f ../../sources/pom.xml clean install -DskipTests -P-vitam

##### COTS

docker compose -f ../../dev-deployment/docker-cots/docker-compose.yml up
