package com.darylteo.gradle.watcher.tasks

import com.darylteo.nio.DirectoryChangedSubscriber
import com.darylteo.nio.DirectoryWatchService
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.joda.time.Instant
import org.joda.time.Interval

class WatcherTask extends DefaultTask {
  boolean block = true

  def tasks = []
  def includes = ['src/**']
  def excludes = []

  /**
   * how long to wait for changes to settle before triggering a build in seconds
   */
  def delay = 2

  def _timer
  def _lastChange

  def _builder
  def _running

  public WatcherTask() {
    this.group = null
  }

  @TaskAction
  def action() {
    _timer = new Timer()

    // setup project connection
    ProjectConnection connection = GradleConnector.newConnector()
      .forProjectDirectory(project.projectDir)
      .connect()

    // setup watcher
    DirectoryWatchService service = project.watcher.service
    def watcher = service.newWatcher(project.projectDir.path)

    includes?.each { path -> watcher.include path  }
    excludes?.each { path -> watcher.excludes path }

    // setup builder
    // coerce into String array to pass into varargs parameter -> BuildLauncher only accepts the Task from .model package.
    def tasks = this.tasks.collect { task ->
      task instanceof Task ? "$task.project.path:$task.name" : task
    } as String[]

    _builder = connection.newBuild()
      .forTasks(tasks)

    watcher.subscribe({ Object[] args ->
      def src, path = args
      _lastChange = new Instant()

      if(!_running) {
        _scheduleBuild()
      }
    } as DirectoryChangedSubscriber)

    blockTask()
  }

  def _scheduleBuild() {
    def currentTask = {
      def lastKnownChange = _lastChange;

      // ensure there haven't been any more recent changes
      def elapsed = new Interval(lastKnownChange, new Instant())
      if (elapsed.toDurationMillis() < (delay * 1000)) {
        return
      }

      try {
        // Errors must be absorbed else the watcher will crap itself.
        _running = true
        _builder.run()
      } catch (Throwable e) {
        e.printStackTrace()
      } finally {
        _running = false
      }

      // check if a change occurred during the build
      if(!lastKnownChange.equals(_lastChange)) {
        _scheduleBuild()
      }
    } as TimerTask

    _timer.schedule(currentTask, delay * 1000)
  }

  private def blockTask() {
    if(this.block) {
      def lock = {}

      synchronized(lock) {
        lock.wait() // block until Ctrl-C
      }
    }
  }
}