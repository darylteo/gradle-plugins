package com.darylteo.gradle.plugins.maven;

public class MavenPluginRepositoryExtension {
  String username = ''
  String password = ''
  String repository = ''

  void username(String username) {
    this.username = username
  }

  void password(String password) {
    this.password = password
  }

  void repository(String repository) {
    this.repository = repository
  }
}
