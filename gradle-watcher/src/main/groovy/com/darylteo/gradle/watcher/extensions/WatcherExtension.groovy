package com.darylteo.gradle.watcher.extensions

import org.gradle.api.Project

import com.darylteo.nio.DirectoryWatchService
import com.darylteo.nio.ThreadPoolDirectoryWatchService

class WatcherExtension {
  final Project project

  private ThreadPoolDirectoryWatchService service = null

  public synchronized DirectoryWatchService getService() {
    if(!service) {
      service = new ThreadPoolDirectoryWatchService()
    }

    return service
  }

  public WatcherExtension(Project project) {
    this.project = project
  }

  def configure(Closure closure) {
    closure.delegate = service
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call(service)
  }
}