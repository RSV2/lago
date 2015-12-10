#Lago
#### A minimal convenience layer on the RabbitMQ Java api
The taxonomic order of rabbits is 'Lagomorph' (along with Pikas). Lago is, incidentally, Greek for 'hare'.

###Goals

The goal of Lago is to provide a minimal abstraction layer on top of the RabbitMQ java client that facilitates connection, message transfer, and construction of consumers... all without framework dependencies.

It aims to provide the following features:

* Automatic serialization / deserialization of objects to and from rabbit (JSON via Jackson)
* Connection, Exchange, Queue, and Consumer Binding declarations done via configuration file (currently YAML only)
*  Easily set number of threads (channels) per Consumer
*  Built in RPC functionality 
*  TODO: An optional, built in rpc consumer which returns all Incoming and Outgoing messages per queue / consumer. Effectively an API guide.

In other words, it provides a high level way of publishing / rpc'ing java objects via RabbitMQ. while it can be used out of the box, it is intended to be built up on to implement a flow or communication patterns specific to your org.
