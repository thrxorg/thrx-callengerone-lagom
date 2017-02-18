/*
 * written by thrx.de
 */
package org.thrx.challenger.conelagomjavastream.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.thrx.challenger.conelagomjava.api.ConelagomjavaService;
import org.thrx.challenger.conelagomjavastream.api.ConelagomjavaStreamService;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Implementation of the ConelagomjavaStreamService.
 */
public class ConelagomjavaStreamServiceImpl implements ConelagomjavaStreamService {

  private final ConelagomjavaService conelagomjavaService;

  @Inject
  public ConelagomjavaStreamServiceImpl(ConelagomjavaService conelagomjavaService) {
    this.conelagomjavaService = conelagomjavaService;
  }

  @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> stream() {
    return hellos -> completedFuture(
        hellos.mapAsync(8, name -> conelagomjavaService.hello(name).invoke()));
  }
}
