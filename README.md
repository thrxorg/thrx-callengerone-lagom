# lagom-java.g8

A Lagom Java seed template for sbt

## TODO review README and create DOKU

Basic informations have a look at:

 [thrx-callengerone-lagom-experimental - README.md](https://raw.githubusercontent.com/thrxorg/thrx-callengerone-lagom-experimental/develop/README.md)

 
## lagom development 

### Lagom ServiceGateway

http://localhost:9000 is running a Service Gateway server that acts as a reverse proxy to all of the services running in your project.

#### Make Service visible over ServiceGateway 

The Service Gateway can be configured to forward service calls onto your service, but it does not by default. You configure it by defining ACLs (access control lists) in your service descriptors.

Most commonly, you'll call `withAutoAcl(true)` to automatically forward all service call paths to your service:

	trait TwitterSchedulerService extends Service {
	  def doWork: ServiceCall[NotUsed, Done]
	  override def descriptor: Descriptor = {
	    import Service._
	    named("scheduler").withCalls(
	      call(doWork)
	    ).withAutoAcl(true)
	  }
	}

If you want more control over which paths get forwarded from the Service Gateway to the back-end service, you can call withAcls to pass a list of explicit methods and path regular expressions that should be forwarded from the Service Gateway:

	trait TwitterSchedulerService extends Service {
	  def doWork: ServiceCall[NotUsed, Done]
	  override def descriptor: Descriptor = {
	    import Service._
	    named("scheduler").withCalls(
	      call(doWork)
	    ).withAcls(
	      ServiceAcl.forPathRegex("/doWork")
	    )
	  }
	}

Reference: 

[lagom -- unable to hit rest endpoint of lagom services](https://stackoverflow.com/questions/42281846/lightbend-lagom-and-akka-unable-to-hit-rest-endpoint-of-lagom-services/42290735#42290735)



### JAVA Services

### Scala Services