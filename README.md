# session-manager-fit-redis
refactor the tomcat session-manager which supports the redis cluster, simplify it and made it fit for the redis..

Redis Session Manager for Apache Tomcat

Overview


Supports for the redis cluster 
Simplify the implementation against the old one, removes the Dirty Tracking Model, the reason for this is, redis is based on memory, the performance is good enough and don't need performance tuning via Dirty Tracking Model. And finally, the code was made as more readable and understandable, that's the benefit.
