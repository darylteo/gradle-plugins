package com.darylteo.gradle.vertx

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Sync

/**
 * Plugin for handling support of external vertx modules
 * Responsible for handling dependencies, setting up classpaths, tasks etc.
 * @author Daryl Teo
 */
class VertxModulesPlugin implements Plugin<Project>{
  @Override
  public void apply(Project project) {

    project.convention.plugins.modulesPlugin = new VertxModulesPluginConvention(project)

    project.configurations {
      modules
      explodedModules

      compile.extendsFrom modules
      provided.extendsFrom explodedModules
    }

    project.afterEvaluate {
      // Establishing the dependent projects
      project.dependentProjects = project.configurations
        .compile
        .dependencies
        .withType(ProjectDependency)
        .collect { dep -> dep.dependencyProject }

      afterEvaluation(project)
    } // end afterEvaluate

  }

  def afterEvaluation(Project project) {
    project.with {
      // Setting up the classpath for compilation
      configurations.modules.dependencies.each { dep ->
        configurations.modules.files(dep)
          // ignore non zips
          .findAll { file ->
            return file.name.endsWith('.zip')
          }.each { file ->
            dependencies.explodedModules rootProject.zipTree(file)
              .matching { include 'lib/*.jar' }
          } // end artifacts .each

      } // end dependencies .each

      sourceSets {
        all {
          dependentProjects.each { dependentProject ->
            compileClasspath += dependentProject.configurations.modules
            compileClasspath += dependentProject.configurations.explodedModules
          }

          compileClasspath += configurations.modules
          compileClasspath += configurations.explodedModules
        }
      } // end sourceSets

    } // end .with
  }

  private class VertxModulesPluginConvention {
    private Project project

    def dependentProjects = []

    VertxModulesPluginConvention(Project project) {
      this.project = project
    }

    // Convenience Methods for converting vertx notation dependencies
    // into regular ones
    def vertxModule(String notation) {
      def (group, name, version) = notation.split('~')

      return vertxModule(group, name, version)
    }

    def vertxModule(Map module) {
      return vertxModule(module.group, module.name, module.version)
    }

    String vertxModule(String group, String name, String version) {
      // Force zip artifact, in case jar exists
      return "$group:$name:$version@zip"
    }
  }

}
