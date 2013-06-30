package com.darylteo.gradle.vertx

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.Handler
import org.vertx.java.platform.PlatformLocator

class VertxLauncher implements Plugin<Project>{

  @Override
  public void apply(Project project) {
    project.with {
      task('start') << {
        def mutex = new Object()

        def pm = PlatformLocator.factory.createPlatformManager()
        pm.deployModule("main", null, 1, new Handler<AsyncResult<String>>() {
              public void handle(AsyncResult<String> result) {
                if (!result.succeeded()){
                  println 'Verticle failed to deploy.'
                  println result.cause()

                  // Wake the main thread
                  synchronized(mutex){
                    mutex.notify()
                  }
                  return
                }

                println "Verticle deployed! Deployment ID is: $deploymentID"
                println 'CTRL-C to stop server'
              }
            });

        // Waiting thread so that Verticle will continue running
        synchronized (mutex){
          mutex.wait()
        }
      }
    }

  }

}
