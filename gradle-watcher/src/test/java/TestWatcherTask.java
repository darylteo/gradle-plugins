import com.darylteo.gradle.watcher.tasks.WatcherTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

/**
 * Created by dteo on 15/07/2014.
 */
public class TestWatcherTask {

  @Test
  public void Test() {
    ProjectBuilder builder = ProjectBuilder.builder();
    builder.withName("root");

    Project project = builder.build();
    project.getTasks().create("watch", WatcherTask.class);
  }
}
