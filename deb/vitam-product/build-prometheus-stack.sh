#!/bin/bash
set -e
WORKING_FOLDER=$(dirname $0)
PROMETHEUS_VERSION="2.49.1"
PROMETHEUS_NODE_EXPORTER_VERSION="1.7.0"
PROMETHEUS_CONSUL_EXPORTER_VERSION="0.11.0"
PROMETHEUS_ELASTICSEARCH_EXPORTER_VERSION="1.7.0"
PROMETHEUS_ALERTMANAGER_VERSION="0.26.0"
PROMETHEUS_BLACKBOX_EXPORTER_VERSION="0.24.0"

PROMETHEUS_URL=https://github.com/prometheus/prometheus/releases/download/v${PROMETHEUS_VERSION}/prometheus-${PROMETHEUS_VERSION}.linux-amd64.tar.gz
PROMETHEUS_NODE_EXPORTER_URL=https://github.com/prometheus/node_exporter/releases/download/v${PROMETHEUS_NODE_EXPORTER_VERSION}/node_exporter-${PROMETHEUS_NODE_EXPORTER_VERSION}.linux-amd64.tar.gz
PROMETHEUS_CONSUL_EXPORTER_URL=https://github.com/prometheus/consul_exporter/releases/download/v${PROMETHEUS_CONSUL_EXPORTER_VERSION}/consul_exporter-${PROMETHEUS_CONSUL_EXPORTER_VERSION}.linux-amd64.tar.gz
PROMETHEUS_ELASTICSEARCH_EXPORTER_URL=https://github.com/prometheus-community/elasticsearch_exporter/releases/download/v${PROMETHEUS_ELASTICSEARCH_EXPORTER_VERSION}/elasticsearch_exporter-${PROMETHEUS_ELASTICSEARCH_EXPORTER_VERSION}.linux-amd64.tar.gz
PROMETHEUS_ALERTMANAGER_URL=https://github.com/prometheus/alertmanager/releases/download/v${PROMETHEUS_ALERTMANAGER_VERSION}/alertmanager-${PROMETHEUS_ALERTMANAGER_VERSION}.linux-amd64.tar.gz
PROMETHEUS_BLACKBOX_EXPORTER_URL=https://github.com/prometheus/blackbox_exporter/releases/download/v${PROMETHEUS_BLACKBOX_EXPORTER_VERSION}/blackbox_exporter-${PROMETHEUS_BLACKBOX_EXPORTER_VERSION}.linux-amd64.tar.gz

if [ ! -d ${WORKING_FOLDER}/target ]; then
	mkdir ${WORKING_FOLDER}/target
fi

## Directory where source files will be downloaded
if [ ! -d ${WORKING_FOLDER}/sources ]; then
	mkdir ${WORKING_FOLDER}/sources
fi


#########################
## Prometheus server
#########################
PACKAGE_NAME=prometheus
PACKAGE_VERSION=${PROMETHEUS_VERSION}
PACKAGE_URL=${PROMETHEUS_URL}

rm -rf ${WORKING_FOLDER}/vitam-${PACKAGE_NAME}/vitam

mkdir -p ${WORKING_FOLDER}/vitam-${PACKAGE_NAME}/vitam/app/${PACKAGE_NAME}
mkdir -p ${WORKING_FOLDER}/vitam-${PACKAGE_NAME}/vitam/bin/${PACKAGE_NAME}
mkdir -p ${WORKING_FOLDER}/vitam-${PACKAGE_NAME}/vitam/conf/${PACKAGE_NAME}

pushd ${WORKING_FOLDER}/sources/
echo "Repertoire courant: $(pwd)"
echo "Récupérer ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
if [ ! -f ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz ]; then
	curl -L -k --max-time 1200 ${PACKAGE_URL} -o ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz
fi

echo "Décompacter ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
tar xzf ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz --strip 1 -C ../vitam-prometheus/vitam/bin/${PACKAGE_NAME}/

popd

pushd ${WORKING_FOLDER}/vitam-prometheus/vitam/bin/${PACKAGE_NAME}/
echo "Install files ..."

mv -f LICENSE ../../app/${PACKAGE_NAME}/LICENSE
mv -f NOTICE ../../app/${PACKAGE_NAME}/NOTICE
mv -f -u consoles/ ../../app/${PACKAGE_NAME}/
mv -f -u console_libraries ../../app/${PACKAGE_NAME}/
mv -f ${PACKAGE_NAME}.yml ../../conf/${PACKAGE_NAME}

popd
pushd ${WORKING_FOLDER}

dpkg-deb --build vitam-${PACKAGE_NAME} ${WORKING_FOLDER}/target

popd


#########################
## Prometheus node_exporter
#########################
PACKAGE_NAME=node_exporter
PACKAGE_VERSION=${PROMETHEUS_NODE_EXPORTER_VERSION}
PACKAGE_URL=${PROMETHEUS_NODE_EXPORTER_URL}

rm -rf ${WORKING_FOLDER}/vitam-prometheus-node-exporter/vitam

mkdir -p ${WORKING_FOLDER}/vitam-prometheus-node-exporter/vitam/app/${PACKAGE_NAME}
mkdir -p ${WORKING_FOLDER}/vitam-prometheus-node-exporter/vitam/bin/${PACKAGE_NAME}

pushd ${WORKING_FOLDER}/sources/
echo "Repertoire courant: $(pwd)"
echo "Récupérer ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
if [ ! -f ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz ]; then
	curl -L -k --max-time 1200 ${PACKAGE_URL} -o ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz
fi

echo "Décompacter ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
tar xzf ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz --strip 1 -C ../vitam-prometheus-node-exporter/vitam/bin/${PACKAGE_NAME}/

popd

pushd ${WORKING_FOLDER}/vitam-prometheus-node-exporter/vitam/bin/${PACKAGE_NAME}/
echo "Install files ..."

mv -f LICENSE ../../app/${PACKAGE_NAME}/LICENSE
mv -f NOTICE ../../app/${PACKAGE_NAME}/NOTICE

popd
pushd ${WORKING_FOLDER}

dpkg-deb --build vitam-prometheus-node-exporter ${WORKING_FOLDER}/target

popd

#########################
## Prometheus consul_exporter
#########################
PACKAGE_NAME=consul_exporter
PACKAGE_VERSION=${PROMETHEUS_CONSUL_EXPORTER_VERSION}
PACKAGE_URL=${PROMETHEUS_CONSUL_EXPORTER_URL}

rm -rf ${WORKING_FOLDER}/vitam-prometheus-consul-exporter/vitam

mkdir -p ${WORKING_FOLDER}/vitam-prometheus-consul-exporter/vitam/app/${PACKAGE_NAME}
mkdir -p ${WORKING_FOLDER}/vitam-prometheus-consul-exporter/vitam/bin/${PACKAGE_NAME}


pushd ${WORKING_FOLDER}/sources/
echo "Repertoire courant: $(pwd)"
echo "Récupérer ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
if [ ! -f ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz ]; then
	curl -L -k --max-time 120 ${PACKAGE_URL} -o ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz
fi

echo "Décompacter ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
tar xzf ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz --strip 1 -C ../vitam-prometheus-consul-exporter/vitam/bin/${PACKAGE_NAME}/

popd

pushd ${WORKING_FOLDER}/vitam-prometheus-consul-exporter/vitam/bin/${PACKAGE_NAME}/
echo "Install files ..."

mv -f LICENSE ../../app/${PACKAGE_NAME}/LICENSE
mv -f NOTICE ../../app/${PACKAGE_NAME}/NOTICE

popd
pushd ${WORKING_FOLDER}

dpkg-deb --build vitam-prometheus-consul-exporter ${WORKING_FOLDER}/target

popd

####################################
## Prometheus Elasticsearch Exporter
####################################
PACKAGE_NAME=elasticsearch_exporter
PACKAGE_VERSION=${PROMETHEUS_ELASTICSEARCH_EXPORTER_VERSION}
PACKAGE_URL=${PROMETHEUS_ELASTICSEARCH_EXPORTER_URL}

rm -rf ${WORKING_FOLDER}/vitam-prometheus-elasticsearch-exporter/vitam

mkdir -p ${WORKING_FOLDER}/vitam-prometheus-elasticsearch-exporter/vitam/app/${PACKAGE_NAME}
mkdir -p ${WORKING_FOLDER}/vitam-prometheus-elasticsearch-exporter/vitam/bin/${PACKAGE_NAME}


pushd ${WORKING_FOLDER}/sources/
echo "Repertoire courant: $(pwd)"
echo "Récupérer ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
if [ ! -f ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz ]; then
	curl -L -k --max-time 120 ${PACKAGE_URL} -o ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz
fi

echo "Décompacter ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
tar xzf ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz --strip 1 -C ../vitam-prometheus-elasticsearch-exporter/vitam/bin/${PACKAGE_NAME}/

popd

pushd ${WORKING_FOLDER}/vitam-prometheus-elasticsearch-exporter/vitam/bin/${PACKAGE_NAME}/
echo "Install files ..."

mv -f LICENSE ../../app/${PACKAGE_NAME}/LICENSE

popd
pushd ${WORKING_FOLDER}

dpkg-deb --build vitam-prometheus-elasticsearch-exporter ${WORKING_FOLDER}/target

popd

#########################
## Prometheus alertmanager
#########################
PACKAGE_NAME=alertmanager
PACKAGE_VERSION=${PROMETHEUS_ALERTMANAGER_VERSION}
PACKAGE_URL=${PROMETHEUS_ALERTMANAGER_URL}


rm -rf ${WORKING_FOLDER}/vitam-prometheus-${PACKAGE_NAME}/vitam

mkdir -p ${WORKING_FOLDER}/vitam-prometheus-${PACKAGE_NAME}/vitam/app/${PACKAGE_NAME}
mkdir -p ${WORKING_FOLDER}/vitam-prometheus-${PACKAGE_NAME}/vitam/bin/${PACKAGE_NAME}
mkdir -p ${WORKING_FOLDER}/vitam-prometheus-${PACKAGE_NAME}/vitam/conf/${PACKAGE_NAME}

pushd ${WORKING_FOLDER}/sources/
echo "Repertoire courant: $(pwd)"
echo "Récupérer ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
if [ ! -f ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz ]; then
	curl -L -k --max-time 1200 ${PACKAGE_URL} -o ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz
fi

echo "Décompacter ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
tar xzf ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz --strip 1 -C ../vitam-prometheus-${PACKAGE_NAME}/vitam/bin/${PACKAGE_NAME}/

popd

pushd ${WORKING_FOLDER}/vitam-prometheus-${PACKAGE_NAME}/vitam/bin/${PACKAGE_NAME}/
echo "Install files ..."

mv -f LICENSE ../../app/${PACKAGE_NAME}/LICENSE
mv -f NOTICE ../../app/${PACKAGE_NAME}/NOTICE
mv -f ${PACKAGE_NAME}.yml ../../conf/${PACKAGE_NAME}

popd
pushd ${WORKING_FOLDER}

dpkg-deb --build vitam-prometheus-${PACKAGE_NAME} ${WORKING_FOLDER}/target

popd


#########################
## Prometheus blackbox_exporter
#########################
PACKAGE_NAME=blackbox_exporter
PACKAGE_VERSION=${PROMETHEUS_BLACKBOX_EXPORTER_VERSION}
PACKAGE_URL=${PROMETHEUS_BLACKBOX_EXPORTER_URL}

rm -rf ${WORKING_FOLDER}/vitam-prometheus-blackbox-exporter/vitam

mkdir -p ${WORKING_FOLDER}/vitam-prometheus-blackbox-exporter/vitam/app/${PACKAGE_NAME}
mkdir -p ${WORKING_FOLDER}/vitam-prometheus-blackbox-exporter/vitam/bin/${PACKAGE_NAME}


pushd ${WORKING_FOLDER}/sources/
echo "Repertoire courant: $(pwd)"
echo "Récupérer ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
if [ ! -f ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz ]; then
	curl -L -k --max-time 120 ${PACKAGE_URL} -o ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz
fi

echo "Décompacter ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz"
tar xzf ${PACKAGE_NAME}-${PACKAGE_VERSION}.linux-amd64.tar.gz --strip 1 -C ../vitam-prometheus-blackbox-exporter/vitam/bin/${PACKAGE_NAME}/

popd

pushd ${WORKING_FOLDER}/vitam-prometheus-blackbox-exporter/vitam/bin/${PACKAGE_NAME}/
echo "Install files ..."

mv -f LICENSE ../../app/${PACKAGE_NAME}/LICENSE
mv -f NOTICE ../../app/${PACKAGE_NAME}/NOTICE

popd
pushd ${WORKING_FOLDER}

dpkg-deb --build vitam-prometheus-blackbox-exporter ${WORKING_FOLDER}/target

popd
