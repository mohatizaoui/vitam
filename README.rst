#####
VITAM
#####

.. section-numbering::

For a quick presentation in english, please follow `this link <README.en.rst>`_.


.. image:: doc/fr/logo_vitam.png
        :alt: Logo Vitam
        :align: center

Le programme interministériel Vitam
===================================

Il a pour objectif :

* la conception, la réalisation et la maintenance mutualisées d’une solution logicielle générique d’archivage électronique. Cette solution logicielle est appelée Vitam. Elle est l'objet du document ;
* la mise en place ou la mise à jour, dans chacun des trois ministères porteurs, de plates-formes d’archivage utilisant la solution logicielle Vitam ;
* la réutilisation de la solution logicielle Vitam par le plus grand nombre d’acteurs publics possible, en veillant à sa capacité d'usage dans des contextes divers.
* les documents sont sous `"La Licence Ouverte V2" <https://www.etalab.gouv.fr/wp-content/uploads/2017/04/ETALAB-Licence-Ouverte-v2.0.pdf>`_ et le code sous `CeCILL 2.1 <http://www.cecill.info/licenses/Licence_CeCILL_V2.1.html>`_.
* La "Licence Ouverte V2" est compatible avec la license `Creative Commons CC-By-SA 2.0 <https://creativecommons.org/licenses/by-sa/2.0/>`_; Une copie de cette licence est disponible dans le fichier `<./Licence_CC-By-SA_2.0-en.txt>`_.

Pour plus d’information sur le programme, voir `www.programmevitam.fr <http://www.programmevitam.fr/pages/presentation/>`_.


La solution logicielle Vitam
============================

La solution logicielle développée dans le programme Vitam permettra la prise en charge, la conservation, la pérennisation et la consultation sécurisée de très gros volumes d’archives numériques. Elle assurera la gestion complète du cycle de vie des archives et donc la garantie de leur valeur probante. Elle pourra être utilisée pour tout type d'archive, y compris pour des documents classifiés de défense.

Cette solution est développée en logiciel libre pour faciliter sa réutilisation, son évolution, son adaptation à des contextes particuliers si nécessaire, sa maintenance et donc globalement sa pérennité.

L’obligation de mettre en œuvre une solution d’archivage numérique dans les contextes très différents des trois ministères porteurs, tant en termes de pratiques archivistiques qu’en termes de production informatique, a orienté notre choix vers la réalisation d’un back-office. L’objectif est de prendre en compte dans la solution logicielle Vitam, le maximum de fonctions mutualisables et technologiquement complexes, d’autant plus quand elles s’appliquent à des très grands nombres d’objets, et de laisser chaque entité porter ses propres spécificités de processus. Cette vision permet ainsi la réutilisation plus large, tout en assurant la réalisation d’un outil générique intégrable selon les besoins d’acteurs variés dans leur système d’information.

Positionnée comme une brique d’infrastructure, elle prendra en charge toutes les opérations nécessaires pour assurer la pérennisation des documents numériques versés et pour garantir le maintien de leur valeur probante.

C’est le code de ce back-office central qui est ici publié. Des outils annexes sont aussi publiés selon les besoins dans d’autres dépôts (Cf. organisation GitHub `ProgrammeVitam <https://github.com/ProgrammeVitam>`_).


-------------------------------------------------------------


.. contents::


Structure du projet
==================

Le projet se compose des sous-dossiers suivants :

* ``sources`` : code source des composants développés dans le cadre du programme Vitam ;
* ``rpm/vitam-product`` : packages rpm de composants externes ;
* ``rpm/vitam-external`` : constitution du cache de dépendances vers des packages rpm éditeur ;
* ``deb/vitam-product`` : packages deb de composants externes ;
* ``deb/vitam-external`` : constitution du cache de dépendances vers des packages deb éditeur ;
* ``deployment`` : scripts de déploiement ansible pour la solution Vitam ;
* ``doc`` : documentation technique du projet ;
* ``dev-deployment`` : environnement Docker de développement ;
* ``packaging`` : constitution d'un package standalone de la solution logicielle.

Procédure d’installation manuelle de Vitam Core
===============================================

Prérequis
---------

Linux (x86) ou MacOS (y compris Apple Silicon)

Logiciels: 
- IntelliJ IDEA 24 (Ultimate recommandé) 
  - Plugin multirun
  - Code Style: `VitamStyle_Spotless.xml <https://assistance.programmevitam.fr/plugins/document/projetvitam/folder/50>`__
- Maven 3.9.X et `nvm <https://github.com/nvm-sh/nvm?tab=readme-ov-file>`__ 
- Java 17 JDK
- Docker (utiliser Rancher Desktop pour Mac)

Matériels: - Mémoire vive: 16GiB - Stockage: 20GiB

Récupération du code et mise en place de l’environnement de travail
-------------------------------------------------------------------

Cloner le code sur https://gitlab.dev.programmevitam.fr/vitam/vitam.git ainsi que le jeu de donnéesde test sur https://github.com/ProgrammeVitam/vitam-itests.git. Nous présumons que le dossier du code source se nomme **vitam_repo** par la suite.

S’assurer que JAVA_HOME et M2_HOME sont renseignés dans le ``.bashrc`` ou équivalent (JAVA_HOME pointe vers le répertoire de Java 17, et on peut obtenir M2_HOME avec la commande ``mvn -version``
)

Dans le fichier vitam_repo/sources/pom.xml désactiver les modules ``ihm-demo`` et ``ihm-recette``

Installer node 18 avec la commande ``nvm install 18 && nvm use 18``

Dans les paramètres IntelliJ: Editor > Code Style > Scheme > Import Scheme > IntelliJ IDEA code style XML et sélectionner le code style téléchargé plus tôt

Copier le répertoire vitam_repo/vitam-conf-dev/intellig-conf/runConfigurations vers vitam_repo/.idea/runConfigurations (si le dossier existe déjà, remplacer le contenu.)

Relancer Intellij IDEA.

Editer le fichier ``/etc/hosts`` (ou équivalent) et y ajouter les enregistrements suivants:

::

   # Vitam 

  127.0.0.1 access-external.service
  127.0.0.1 access-internal.service
  127.0.0.1 functional-administration.service
  127.0.0.1 scheduler.service
  127.0.0.1 elastic-kibana-interceptor.service
  127.0.0.1 batch-report.service
  127.0.0.1 ingest-external.service
  127.0.0.1 ingest-internal.service
  127.0.0.1 ihm-demo.service
  127.0.0.1 logbook.service
  127.0.0.1 metadata.service
  127.0.0.1 processing.service
  127.0.0.1 security-internal.service
  127.0.0.1 storage.service
  127.0.0.1 worker.service
  127.0.0.1 collect-internal.service
  127.0.0.1 collect-external.service
  127.0.0.1 metadata-collect.service
  127.0.0.1 workspace-collect.serviceccess-internal.service
  127.0.0.1 worker.service  
  127.0.0.1 workspace.service
  127.0.0.1 workspace-collect.service


Création de l’arborescence /vitam
---------------------------------

Vitam Core a besoin d’avoir un dossier stockant des données dans la racine de l’ordinateur. Voici l’arborescence nécessaire :

::

   /vitam/
   ├── conf
   ├── data
   │   ├── ihm-recette
   │   │   └── test-data
   │   └── storage
   ├── log
   └── tmp

S’assurer que tous les répertoires appartiennent à votre utilisateur (chown).


Ouvrir les paramètres IntelliJ IDEA > Appearance and Behavior > Path Variables et y ajouter une variable nommée ``vitamLocalShareFolder`` pointant vers ``/vitam``.

Créer dans ``/vitam/data/storage`` un fichier nommé ``fr.gouv.vitam.storage.offers.workspace.driver.DriverImpl`` avec le contenu ``offer-fs-1.service.consul``

Créer un lien symbolique de ``/vitam/data/ihm-recette/test-data/data`` vers ``vitam-itests/data`` (vitam-itests étant le deuxième dépôt cloné au départ.)

Faire un lien symbolique de ``vitam_repo/vitam-conf-dev/conf`` vers ``/vitam/conf``. 
Aller dans ``/vitam/conf/metadata/mapping`` et vérifier que les fichiers ``unit-es-mapping.json`` et ``og-es-mapping.json`` sont des liens symboliques vers les fichiers dans ``vitam_repo/deployment/ansible-vitam/roles/elasticsearch-mapping/files/``.

Aller dans ``/vitam/conf/worker`` et s’assurer que ``plugins.json`` est bien un lien vers ``vitam_repo/deployment/ansible-vitam/roles/vitam/files/worker/plugins.json``

Créer un lien symbolique de ``/vitam/data/ihm-recette/test-data/data`` vers ``vitam-itests/data`` (vitam-itests étant le deuxième repository cloné au départ).


Compilation
-----------

Depuis le répertoire vitam_repo/sources exécuter la commande

``mvn clean install -D-skipTests -P-vitam``

La procédure peut prendre environ 10 à 20 minutes.

Compilation des COTS
--------------------

Les COTS sont des dépendances externes nécessaires pour lancer le projet en local (mongo et elasticSearch)

L’option la plus simple est d’utiliser docker compose, dont le fichier .yaml se trouve dans ``vitam_repo/dev-deployment/docker-cots`` Puis lancer avec IntelliJ ou docker compose: ``docker compose up``

Installation avec l’aide du script
=====================================

Prérequis
----------

Linux (x86) ou MacOS (y compris Apple Silicon)

Logiciels: 
* IntelliJ IDEA 24 (Ultimate recommandé) 
* Plugin multirun 
* Code style: `VitamStyle_Spotless.xml <https://assistance.programmevitam.fr/plugins/document/projetvitam/folder/50>`__
* Maven 3.9.X et `nvm <https://github.com/nvm-sh/nvm?tab=readme-ov-file>`__ 
* Java 17 JDK
* Docker (utiliser Rancher Desktop pour Mac)

Matériels:
* Mémoire vive: 16GiB
* Stockage: 20GiB

Mise en place de l’espace de travail
------------------------------------

Cloner le code sur https://gitlab.dev.programmevitam.fr/vitam/vitam.git ainsi que le jeu de données de test sur https://github.com/ProgrammeVitam/vitam-itests.git. Nous présumons que le dossier du code source se nomme **vitam_repo** par la suite.

S’assurer que JAVA_HOME (Pointant vers le dossier de Java 17) et M2_HOME sont renseignés dans le ``.bashrc`` ou équivalent (On peut obtenir M2_HOME avec la commande ``mvn -version`` )

Dans les paramètres IntelliJ: Editor > Code Style > Scheme > Import Scheme > IntelliJ IDEA code style XML et sélectionner le code style téléchargé plus tôt

Dans le fichier /etc/hosts ajouter ces enregistrements:

::

   # Vitam 

  127.0.0.1 access-external.service
  127.0.0.1 access-internal.service
  127.0.0.1 functional-administration.service
  127.0.0.1 scheduler.service
  127.0.0.1 elastic-kibana-interceptor.service
  127.0.0.1 batch-report.service
  127.0.0.1 ingest-external.service
  127.0.0.1 ingest-internal.service
  127.0.0.1 ihm-demo.service
  127.0.0.1 logbook.service
  127.0.0.1 metadata.service
  127.0.0.1 processing.service
  127.0.0.1 security-internal.service
  127.0.0.1 storage.service
  127.0.0.1 worker.service
  127.0.0.1 collect-internal.service
  127.0.0.1 collect-external.service
  127.0.0.1 metadata-collect.service
  127.0.0.1 workspace-collect.serviceccess-internal.service
  127.0.0.1 worker.service  
  127.0.0.1 workspace.service
  127.0.0.1 workspace-collect.service

Compilation
===========

Se mettre dans le dossier ``vitam_repo/vitam-conf-dev/scripts/`` et exécuter ``vitam_install.sh``

Lancement
=========

Dans IntelliJ, lancer la configuration multiRun Vitam, qui doit normalement éxecuter les 13 services le composant.

Initialisation des données
==========================

Lancer le script ``init_data_vitam.sh`` depuis ``vitam-conf-dev/scripts``. Celui-ci doit s’exécuter sans erreurs.

Lancer la configuration multirun Cucumber Init, qui doit également se dérouler sans erreurs. 

Test de requêtes 
================

Lorsque Vitam est démarré vous pouvez ensuite effectuer des requêtes. La liste de ces requêtes se trouve dans ``vitam-conf-dev/making-vitam-requests`` 
Pour vérifier que tout est en place, lancer la requête ``referential/access-contracts.http``.
Cette requête doit se terminer par un code 200 (OK). 
Certaines requêtes ne sont pas possibles car des services Vitam external ne sont pas lancés. Les requêtes dans "Collect" requièrent le lancement des services CollectExternal, Metadata, Metadata Collect.
