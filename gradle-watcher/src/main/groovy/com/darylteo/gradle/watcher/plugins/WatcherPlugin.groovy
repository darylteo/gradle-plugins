package com.darylteo.gradle.watcher.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

public class WatcherPlugin implements Plugin<Project>{
  @Override
  public void apply(Project project) {
  }
}

class WatcherExtension {
  def watch(boolean block = true) {
    Thread.start { Thread.sleep(10000) }
  }
}