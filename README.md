# area51-akka
Test area for my experiments with Akka and its sub-projects.   
Also works as general notes for myself on my findings.

#Findings

##Akka Cluster

* All members in the cluster must have the same name on the actor system.  
Trying to join in to a cluster with a different actor system name will render logging and dropped messages
* Roles are configured in the Akka config  
This makes it rather hard wired, one can't just add a new "service" ad-hoc into the cluster from an already started actor system
 
