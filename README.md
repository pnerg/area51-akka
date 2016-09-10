# area51-akka
Test area for my experiments with Akka and its sub-projects.   
Also works as general notes for myself on my findings.

#Links
http://doc.akka.io/docs/akka/2.4.0/common/cluster.html   
http://doc.akka.io/docs/akka/2.4.0/scala/cluster-usage.html

#Findings

##Akka Cluster

* All members in the cluster must have the same name on the actor system.  
Trying to join in to a cluster with a different actor system name will render logging and dropped messages
* Roles are configured in the Akka config  
This makes it rather hard wired, one can't just add a new "service" ad-hoc into the cluster from an already started actor system
* Cluster.joinSeedNodes allows for manual joining without the need to configure in the akka config
* Creating multiple actors that start a cluster in the same actor system will not start more clusters.   
One will only end up with mutliple actors receiving the same cluster events

##Cluster Extensions

* Sending a message using the mediator towards a non-existing service/actor or sending before the cluster is setup just results in the message to be lost into void. No logging or nothing
* Attempting to join into a cluster using the __DistributedPubSub__ mediator will yield error logs on the seed side if the seed side is not configured in the same way. So one can't have a generic set of seed nodes just to serve as seeds for the members to join in.
```[INFO] [09/10/2016 08:23:37.827] [ClusterTest-akka.actor.default-dispatcher-23] [akka://ClusterTest/system/distributedPubSubMediator] Message [akka.cluster.pubsub.DistributedPubSubMediator$Internal$Status] from Actor[akka.tcp://ClusterTest@127.0.0.1:46925/system/distributedPubSubMediator#605893105] to Actor[akka://ClusterTest/system/distributedPubSubMediator] was not delivered. [1] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.```

