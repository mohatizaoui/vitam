Notes et procédures spécifiques V8.0
####################################

.. caution:: Veuillez appliquer les procédures spécifiques à chacune des versions précédentes en fonction de la version de départ selon la suite suivante: V6 -> V7.0 -> V7.1 -> V8.0

Adaptation des sources de déploiement ansible
=============================================

Configuration des jobs d'audit
-----------------------------------------------

Il est maintenant possible de configurer les jobs d'audit d'intégrité et d'existence par tenant, et par conséquent la définition des jobs d'audit a changé.

L'ancienne configuration définissait la fréquence d'exécution et les paramètres des jobs dans le fichier ``deployment/environments/group_vars/all/advanced/vitam_vars.yml`` :

.. code-block:: yaml

vitam_timers:
  functional_administration:
    frequency_integrity_audit: "0 0 0 1 JAN ? 2020"
    frequency_existence_audit: "0 0 0 1 JAN ? 2020"

scheduler:
  job_parameters:
    integrity_audit:
      operations_delay_in_minutes: 1440
    existence_audit:
      operations_delay_in_minutes: 1440

..

La nouvelle configuration n'utilise plus les valeurs de ``vitam_timers.functional_administration.frequency_integrity_audit`` et ``vitam_timers.functional_administration.frequency_existence_audit``, et prend la forme suivante :

.. code-block:: yaml

scheduler:
  job_parameters:
    integrity_audit:
      - key: SYSTEM
        selected_tenants: [1]
        operations_delay_in_minutes: 1440
        frequency: "0 0 2 ? * * *" # Every day at 2am
      - key: TENANT2
        selected_tenants: [2]
        operations_delay_in_minutes: 1440
        frequency: "0 0 4 ? * * *" # Every day at 4am
    existence_audit:
      - key: SYSTEM
        selected_tenants: [1,2]
        operations_delay_in_minutes: 1440
        frequency: "0 0 6 ? * * *" # Every day at 6am

..

Pour ``integrity_audit`` et ``existence_audit``, il conviendra de définir une entrée par tenant ou groupe de tenant devant s'exécuter à la même fréquence. Les différents champs sont décrits ci-dessous :

Le paramètre ``key`` doit être unique et sera ajouté au nom du job. Il doit donc être de forme alphanumérique, sans espaces.

Le paramètre ``selected_tenants`` liste les identifiants de tenants auquel la fréquence s'applique. S'il est vide ou non renseigné, la fréquence s'appliquera à tous les tenants.

Le paramètre ``operations_delay_in_minutes`` correspond au paramètre de la version précédente.

Le paramètre ``frequency`` correspond aux fréquences précédemment définies dans les paramètres ``vitam_timers.functional_administration.frequency_integrity_audit`` et ``vitam_timers.functional_administration.frequency_existence_audit``.
