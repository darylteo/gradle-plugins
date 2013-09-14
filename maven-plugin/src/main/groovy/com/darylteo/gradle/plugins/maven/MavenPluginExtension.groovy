package com.darylteo.gradle.plugins.maven

import org.gradle.api.Project

class MavenPluginExtension {
  // repository configuration
  String username = ''
  String password = ''
  String repository = ''
  String snapshotRepository = ''

  // artifact identifier
  String groupId = null
  String artifactId = null
  String version = null

  final Project project 
  
  void username(String username) {
    this.username = username
  }

  void password(String password) {
    this.password = password
  }

  void repository(String repository) {
    this.repository = repository
  }

  void snapshotRepository(String repository) {
    this.snapshotRepository = repository
  }

  void groupId(String groupId) {
    this.groupId = groupId
  }

  void artifactId(String artifactId) {
    this.artifactId = artifactId
  }

  void version(String version) {
    this.version = version
  }
  
  public MavenPluginExtension(Project project) {
    this.project = project
  }
  
  // pom configuration
  def pomConfigClosures = []
  void project(Closure<?> closure) {
    this.pomConfigClosures += closure
  }
  
  boolean getRelease() {
    return !(this.version ?: this.project.version).endsWith('-SNAPSHOT')
  }
}