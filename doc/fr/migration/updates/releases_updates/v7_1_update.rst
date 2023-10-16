Notes et procédures spécifiques V7.1
####################################

.. caution:: Veuillez appliquer les procédures spécifiques à chacune des versions précédentes en fonction de la version de départ selon la suite suivante: V5 -> V6RC -> V6 -> V7.0.

Adaptation des sources de déploiement ansible
=============================================

N/A

Procédures à exécuter AVANT la montée de version
================================================

Arrêt des timers et des accès externes à Vitam
----------------------------------------------

.. caution:: Cette opération doit être effectuée AVANT la montée de version vers la V7.1.

.. caution:: Cette opération doit être effectuée avec les sources de déploiements de l'ancienne version.

Les timers et les externals de Vitam doivent être arrêtés sur **tous les sites** :

.. code-block:: bash

    ansible-playbook -i environments/<inventaire> ansible-vitam-exploitation/stop_external.yml --ask-vault-pass
    ansible-playbook -i environments/<inventaire> ansible-vitam-exploitation/stop_vitam_scheduling.yml --ask-vault-pass
    ansible-playbook -i environments/<inventaire> ansible-vitam-exploitation/stop_vitam_scheduler.yml --ask-vault-pass

..

Mise à jour des dépôts (YUM/APT)
--------------------------------

.. caution:: Cette opération doit être effectuée AVANT la montée de version

Afin de pouvoir déployer la nouvelle version, vous devez mettre à jour la variable ``vitam_repositories`` sous ``environments/group_vars/all/main/repositories.yml`` afin de renseigner les dépôts à la version cible.

Puis exécutez le playbook suivant **sur tous les sites** :

.. code-block:: bash

    ansible-playbook -i environments/<inventaire> ansible-vitam-extra/bootstrap.yml --ask-vault-pass

..


Montée de version vers mongo 6.0
--------------------------------

.. caution:: Cette montée de version doit être effectuée AVANT la montée de version V7.1 de Vitam et après l'arrêt des tâches planifiées et des externals.

.. caution:: Cette opération doit être effectuée après avoir mis à jour les dépôts Vitam en V7.1.

Exécutez le playbook suivant à partir de l'ansiblerie de la V7.1 **sur tous les sites** :

.. code-block:: bash

    # Mise à jour mongo
    ansible-playbook -i environments/<inventaire> ansible-vitam-migration/migration_mongodb_60.yml --ask-vault-pass

..

Montée de version vers mongo 7.0
--------------------------------

.. caution:: Cette montée de version doit être effectuée AVANT la montée de version V7.1 de vitam et après la montée de version de MongoDB 6.0 ci-dessus.

Exécutez le playbook suivant:

.. code-block:: bash

    ansible-playbook -i environments/<inventaire> ansible-vitam-migration/migration_mongodb_70.yml --ask-vault-pass

..

Une fois les montées de version de MongoDB réalisées, la montée de version Vitam peut être réalisée.

Arrêt complet de Vitam
----------------------

.. caution:: Cette opération doit être effectuée AVANT la montée de version vers la V7.1

.. caution:: Cette opération doit être effectuée avec les sources de déploiements de l'ancienne version.

Vitam doit être arrêté sur **tous les sites** :

.. code-block:: bash

    ansible-playbook -i environments/<inventaire> ansible-vitam-exploitation/stop_vitam.yml --ask-vault-pass

..

Application de la montée de version
===================================

.. caution:: L'application de la montée de version s'effectue d'abord sur les sites secondaires puis sur le site primaire.

Lancement du master playbook vitam
----------------------------------

.. code-block:: bash

    ansible-playbook -i environments/<inventaire> ansible-vitam/vitam.yml --ask-vault-pass

..

Lancement du master playbook extra
----------------------------------

.. code-block:: bash

    ansible-playbook -i environments/<inventaire> ansible-vitam-extra/extra.yml --ask-vault-pass

..

Procédures à exécuter APRÈS la montée de version
================================================

N/A
