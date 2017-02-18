/*
 * written by thrx.de
 */
package org.thrx.challenger.conelagomjavastream.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import org.thrx.challenger.conelagomjava.api.ConelagomjavaService;
import org.thrx.challenger.conelagomjavastream.api.ConelagomjavaStreamService;

/**
 * The module that binds the ConelagomjavaStreamService so that it can be served.
 */
public class ConelagomjavaStreamModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    // Bind the ConelagomjavaStreamService service
    bindServices(serviceBinding(ConelagomjavaStreamService.class, ConelagomjavaStreamServiceImpl.class));
    // Bind the ConelagomjavaService client
    bindClient(ConelagomjavaService.class);
  }
}
