package com.darylteo.gradle.watcher.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

class AutoGradleBuild extends DefaultTask {
  def tasks = []

  @TaskAction
  def action() {
    ProjectConnection connection = GradleConnector.newConnector()
      .forProjectDirectory(project.projectDir)
      .connect()

    // coerce into String array to pass into varargs parameter -> List get caught by the Iterable<> overload
    def tasks = this.tasks instanceof String ? [this.tasks]: this.tasks
    tasks = tasks.collect { task -> 
      task instanceof Task ? task.name : task
    } as String[];

    BuildLauncher build = connection.newBuild()
      .forTasks(tasks)

    Thread.start {
      while(true){
        Thread.sleep(5000)
        build.run()
      }
    }
  }
}