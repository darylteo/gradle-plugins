package com.darylteo.gradle.watcher.tasks

import com.darylteo.nio.DirectoryChangedSubscriber
import com.darylteo.nio.DirectoryWatchService
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.joda.time.Instant
import org.joda.time.Interval

class WatcherTask extends DefaultTask {
  boolean block = true

  def tasks = []
  def includes = ['src/**']
  def excludes = []

  def runImmediately = false

  // private vars
  def _changed = true

  def _builder
  def _mutex = new Object()

  public WatcherTask() {
    this.group = null
  }

  @TaskAction
  public def action() {
    _changed = runImmediately

    // setup project connection
    ProjectConnection connection = GradleConnector.newConnector()
      .forProjectDirectory(project.projectDir)
      .connect()

    // setup watcher
    DirectoryWatchService service = project.watcher.service
    def watcher = service.newWatcher(project.projectDir.path)

    includes?.each { path -> watcher.include path }
    excludes?.each { path -> watcher.excludes path }

    // setup builder
    // coerce into String array to pass into varargs parameter -> BuildLauncher only accepts the Task from .model package.
    def tasks = this.tasks.collect { task ->
      task instanceof Task ? "$task.project.path:$task.name" : task
    } as String[]

    _builder = connection.newBuild()
      .forTasks(tasks)

    watcher.subscribe({ Object[] args ->
      if (!_changed) {
        _changed = true
        synchronized (_mutex) {
          _mutex.notifyAll()
        }
      }
    } as DirectoryChangedSubscriber)

    _scheduleBuild()
    _blockTask()
  }

  def _scheduleBuild() {
    Thread.start {
      while (1) {
        synchronized (_mutex) {
          if (!_changed) {
            _mutex.wait()
            continue
          }
        }

        // the value of this could be changed by a separate ping
        _changed = false

        try {
          // Errors must be absorbed else the watcher will crap itself.
          _running = true
          _builder.run()
        } catch (Throwable e) {
          e.printStackTrace()
        }
      }
    }
  }

  def _blockTask() {
    if (this.block) {
      def lock = {}

      synchronized (lock) {
        lock.wait() // block until Ctrl-C
      }
    }
  }
}