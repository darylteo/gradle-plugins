package com.darylteo.gradle.javassist.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.darylteo.gradle.javassist.tasks.TransformationTask

public class JavassistPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    def container = project.container(TransformationSpec)

    container.whenObjectAdded { spec ->
      def task = project.task("transform${spec.name.capitalize()}", type: TransformationTask)
      spec.transformationTask = task
      task.spec = spec
    }

    container.whenObjectRemoved { spec ->
      project.tasks.remove(spec.transformationTask)
    }

    project.extensions.transformations = container
  }
}
