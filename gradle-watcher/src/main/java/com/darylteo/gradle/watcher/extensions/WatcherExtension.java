package com.darylteo.gradle.watcher.extensions;

import com.darylteo.nio.DirectoryWatchService;
import com.darylteo.nio.ThreadPoolDirectoryWatchService;
import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.IOException;

public class WatcherExtension {
  private final Project project;

  private static ThreadPoolDirectoryWatchService service = null;

  public synchronized DirectoryWatchService getService() throws IOException {
    if (this.service == null) {
      this.service = new ThreadPoolDirectoryWatchService();
    }

    return this.service;
  }

  private static ProjectConnection connection = null;

  public synchronized ProjectConnection getConnection() {
    if (connection == null) {
      this.connection = GradleConnector.newConnector().forProjectDirectory(this.project.getRootDir()).connect();
    }

    return this.connection;
  }

  public WatcherExtension(Project project) {
    this.project = project;
  }

  public void configure(Closure closure) {
    closure.setDelegate(service);
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.call(service);
  }
}
