#####
VITAM
#####


.. section-numbering::

Une présentation plus complète en français est disponible `ici <README.rst>`_.


.. image:: doc/fr/logo_vitam.png
        :alt: Logo Vitam
        :align: center

.. contents::

Vitam : A French program
========================

The three ministries responsible for public digital archiving in France (Culture, Defense, and Foreign Affairs) decided to develop specialised software, called Vitam, to preserve their valuable digital information. This software is installed in these three ministries to collect all the data produced by their services, store it, manage it long-term, and enable the search and retrieval of items within this vast and valuable source of information.

The Vitam project has three main objectives : 
* The mutualised conception, development and maintenance of a generic digital preservation software solution;
* The installation and update of archival platforms using the Vitam software in each of the ministries responsible for the project;
* The reuse of the software by the largest possible number of public actors, in the widest possible contexts.

This software is also used by other entities in France and elsewhere, as their digital archival system.


Vitam : A digital preservation software 
=======================================

The digital preservation software developed by the Vitam program will allow to collect, preserve, search and securely access a very large amount of digital content. In compliance with the `OAIS <http://www.oais.info/>`_ reference model and `ISO-14641-1 <https://www.iso.org/obp/ui/#iso:std:iso:14641:-1:ed-1:v1:en>`_, it will maintain the legal value of the data in throughout information lifecycle. It will be used for all kinds of archives, including restricted materials. 

This software is free and open source: the Vitam core code is published on Github under the `CeCILL V2.1 open-source license <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>`_ and the documentation is published under the `Etalab Open Licence 2.0 <https://www.etalab.gouv.fr/wp-content/uploads/2018/11/open-licence.pdf>`_ which is compatible with the `CC-by-SA 2.0 license <https://creativecommons.org/licenses/by-sa/2.0/>`_.

-------------------------------------------------------------

Repository structure
=====================

The project is structured into several subdirectories :

* ``sources``: contains the components developped by the Vitam team ;
* ``rpm/vitam-product``: contains rpm packages of external components (when no official rpm exists);
* ``rpm/vitam-external``: constitutes a dependance cache pointing to rpm packages
* ``deb/vitam-product``: contains deb packages of external components (when no officiel deb exists) ;
* ``deb/vitam-external``: constitutes a dependance cache pointing to deb packages (when the exist) ;
* ``deployment``: contains ansible deployment scripts ;
* ``doc``: technical documentation ;
* ``dev-deployment``: contains a Docker environment for development ;
* ``packaging`` : creates a standalone package of the solution

Vitam Core Installation procedure
=================================

Requirements
------------

Operating system : Linux (x86) or macOS (including Apple Silicon)

Software: 
- IntelliJ IDEA 24 (Ultimate recommended) 
  - multiRun plugin 
  - Code Style: `VitamStyle_Spotless.xml <https://assistance.programmevitam.fr/plugins/document/projetvitam/folder/50>`_
- Maven 3.9.X and `nvm <https://github.com/nvm-sh/nvm?tab=readme-ov-file>`__
- Java 17 JDK
- Docker (Use Rancher Desktop on macOS)

Hardware:
- RAM: 16 GiB 
- Storage: 20 GiB

Initialisation of the work environment
--------------------------------------


Clone the code from https://github.com/programmevitam/vitam. Please note that this repository is updated every major release. This directory will be referred to as ``vitam_repo`` from now on.
Clone the data from https://github.com/programmevitam/vitam-itests as well, to get the test data to initialise the installation.

Make sure that the environment variables JAVA_HOME and M2_HOME are set in the ``.bashrc`` or in your current shell. (JAVA_HOME points to your Java 17 install and M2_HOME can be obtained by running ``mvn -version``. )

Install node 18 with the command ``nvm install 18 && nvm use 18``. 

In the  IntelliJ settings: Editor > Code Style > Scheme > Import Scheme > IntelliJ IDEA code style XML > select the Code Style downloaded earlier. 

Restart IntelliJ IDEA.

In the ``/etc/hosts/`` file add the following entries: 

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


Creating the Vitam arborescence
-------------------------------

To operate properly, Vitam Core needs to access folder in the root directory of your computer. Here is the needed arborescence:

::
  
  /vitam/
  ├── conf
  ├── data
  │   ├── ihm-recette
  │   │   └── test-data
  │   └── storage
  ├── log
  └── tmp

Open the IntelliJ IDEA settings > Appearance and Behavior > Path Variables to set a variable named ``vitamLocalShareFolder`` pointing towards your ``/vitam`` directory.

Ensure that the whole working tree belongs to your user (chown).

In ``/vitam/data/storage`` create a file named ``fr.gouv.vitam.storage.offers.workspace.driver.DriverImpl`` with the content ``offer-fs-1.service.consul``

Create a symlink from ``/vitam/data/ihm-recette/test-data/data`` pointing to ``vitam-itests/data``

Make a symlink from ``vitam_repo/vitam-conf-dev/conf`` toward ``/vitam/conf``.
Make sure that inside of ``/vitam/conf/metadata/mapping/`` the symlinks to ``vitam_repo/deployment/ansible-vitam/roles/elasticsearch-mapping/files`` point ``unit-es-mapping.json`` and ``og-es-mapping.json`` 

Likewise, check that in ``/vitam/conf/worker/``, ``plugins.json`` is indeed a link to ``vitam_repo/deployment/ansible-vitam/roles/vitam/files/worker/plugins.json``

Lastly, create a symlink in ``/vitam/data/ihm-recette/test-data/data`` pointing to ``vitam-itests/data`` (vitam-itests being the second repository cloned earlier.)

Compilation 
-----------

From the ``vitam_repo/sources`` directory run the command 

``mvn clean install -D-skipTests -P-vitam``

The build process takes between 10 to 20 minutes.

Building the COTS 
-----------------

COTS are external dependencies necessaroy to run the project locally.
The simplest option is to use docker compose, by running ``docker compose up`` in ``vitam_repo/dev-deployment/docker-cots``.

(Nearly) automated installation
===============================

Requirements
------------

Linux (x86) ou MacOS (including Apple Silicon)

Software: 
- IntelliJ IDEA 24 (Ultimate recommended) 
  - Multirun plugin 
  - Code Style: `VitamStyle_Spotless.xml <https://assistance.programmevitam.fr/plugins/document/projetvitam/folder/50>`_
- Maven 3.9.X and `nvm <https://github.com/nvm-sh/nvm?tab=readme-ov-file>`__
- Java 17 JDK
- Docker (Use Rancher Desktop on MacOS)

Hardware:
- RAM: 16 GiB 
- Storage: 20 GiB

Initialisation of the work environment
--------------------------------------

Clone the code from https://github.com/programmevitam/vitam. Please note that this repository is updated every major release. This directory will be referred to as ``vitam_repo`` from now on.
Clone the data from https://github.com/programmevitam/vitam-itests as well, to get the test data to initialise the installation.

Make sure that the environment variables JAVA_HOME and M2_HOME are set in the ``.bashrc`` or in your current shell. (JAVA_HOME points to your Java 17 install and M2_HOME can be obtained by running ``mvn -version``. )

Install node 18 with the command ``nvm install 18 && nvm use 18``. 

In the  IntelliJ settings: Editor > Code Style > Scheme > Import Scheme > IntelliJ IDEA code style XML > select the Code Style downloaded earlier. 

Restart IntelliJ IDEA.

In the ``/etc/hosts/`` file add the following entries: 

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

Run ``vitam_install.sh`` from the ``vitam_repo/vitam-conf-dev/scripts/``

Vitam Core launch
=================
In IntelliJ IDEA launch the multiRun configuration named "Vitam" which should execute the 13 services composing it.

Test data initialisation
========================

Run the ``init_data_vitam.sh`` script found in ``vitam_repo/vitam-conf-dev/scripts``. This script should not raise any error.

Launch the multirun config Cucumber Init, which should also run its course with no errors.

Test request
============

When Vitam is done starting you can then run test requests, which are found in ``vitam-conf-dev/making-vitam-requests``. 
To check that everything is running properly, try the request ``referential/access-contracts.http``.
This request should return a code 200 (OK).
Some requests cannot be ran as they require Vitam External services that are not started up. For instance the ones in the "Collect" directory require you to launch the CollectExternal, Metadata and Metadata Collect services. 
