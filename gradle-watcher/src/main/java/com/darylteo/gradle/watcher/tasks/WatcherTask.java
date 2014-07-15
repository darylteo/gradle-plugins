package com.darylteo.gradle.watcher.tasks;

import com.darylteo.gradle.watcher.extensions.WatcherExtension;
import com.darylteo.nio.DirectoryChangedSubscriber;
import com.darylteo.nio.DirectoryWatchService;
import com.darylteo.nio.DirectoryWatcher;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.Task;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class WatcherTask extends DefaultTask {
  private WatcherTask that = this;

  private boolean block = true;
  private boolean runImmediately = false;

  private List<Object> tasks = new LinkedList<>();
  private List<String> includes = new LinkedList<>();
  private List<String> excludes = new LinkedList<>();

  private long lastChange;
  private long lastBuild;

  private BuildLauncher builder;
  private Object mutex = new Object();

  public boolean getBlock() {
    return block;
  }

  public void setBlock(boolean block) {
    this.block = block;
  }

  public List<Object> getTasks() {
    return tasks;
  }

  public void setTasks(List<Object> tasks) {
    this.tasks = tasks;
  }

  public List<String> getIncludes() {
    return includes;
  }

  public void setIncludes(List<String> includes) {
    this.includes = includes;
  }

  public List<String> getExcludes() {
    return excludes;
  }

  public void setExcludes(List<String> excludes) {
    this.excludes = excludes;
  }

  public boolean getRunImmediately() {
    return runImmediately;
  }

  public void setRunImmediately(boolean runImmediately) {
    this.runImmediately = runImmediately;
  }

  public WatcherTask() {
    this.includes.add("src/**");
  }

  @TaskAction
  public void action() throws Exception {
    // setup privates
    this.lastChange = 0;
    this.lastBuild = runImmediately ? -1 : 0;

    // setup watcher and connection
    Project project = this.getProject();
    WatcherExtension extension = project.getExtensions().findByType(WatcherExtension.class);

    DirectoryWatchService service = extension.getService();
    ProjectConnection connection = extension.getConnection();

    // setup watcher
    DirectoryWatcher watcher = service.newWatcher(getProject().getProjectDir().getPath());

    for (Object o : this.includes) {
      watcher.include(o.toString());
    }

    for (Object o : this.excludes) {
      watcher.exclude(o.toString());
    }

    // setup builder
    // Note: BuildLauncher only accepts the Task from .model package.
    String[] tasks = generateTaskArguments(project, this.tasks);
    this.builder = connection.newBuild().forTasks(tasks);

    watcher.subscribe(new DirectoryChangedSubscriber() {
      @Override
      public void directoryChanged(DirectoryWatcher watcher, Path entry) {
        synchronized (mutex) {
          WatcherTask.this.lastChange = System.currentTimeMillis();
          mutex.notifyAll();
        }
      }
    });

    scheduleBuild();

    if (this.block) {
      blockTask();
    }
  }

  private String[] generateTaskArguments(Project project, List<Object> tasks) {
    List<String> result = new LinkedList<>();

    for (Object task : tasks) {
      if (task instanceof Task) {
        result.add(((Task) task).getPath());
      } else {
        result.add(String.format("%s:%s", project.getPath(), task));
      }
    }

    return result.toArray(new String[result.size()]);
  }

  private Thread scheduleBuild() {
    final long delay = 1000l;
    final Object lock = new Object();

    Runnable builderThread = new Runnable() {
      public void run() {
        while (true) {
          build();
          waitForChange();
        }
      }

      private void waitForChange() {
        synchronized (that.mutex) {
          try {
            that.mutex.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }

      private void build() {
        while (that.lastBuild < that.lastChange) {
          that.lastBuild = System.currentTimeMillis();

          try {
            // Errors must be absorbed else the watcher will crap itself.
            builder.run();
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
      }
    };

    Thread thread = new Thread(builderThread);
    thread.start();

    return thread;
  }

  private void blockTask() {
    Object lock = new Object();

    synchronized (lock) {
      try {
        lock.wait();// block until Ctrl-C
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
