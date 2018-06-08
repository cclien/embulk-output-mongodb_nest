package org.embulk.output.mongodb_nest;
import java.util.List;
import com.google.common.base.Optional;
import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.spi.*;
import org.slf4j.Logger;

/**
 * <pre>
 *
 * </pre>
 *
 * @Author : 이상호(focuschange@gmail.com)
 * @Date : 2018. 5. 28
 * @Version : 1.0
 * @see :
 */
public class MongodbNestOutputPlugin implements OutputPlugin
{
	private static final Logger logger = Exec.getLogger(MongodbNestOutputPlugin.class);

	public interface DefineChildDocument extends Task
	{
		@Config("name")
		String getName();

		@Config("field")
		String getField();
	}

	public interface PluginTask extends Task
	{
		@Config("collection")
		String getCollection();

		@Config("host")
		String getHost();

		@Config("port")
		@ConfigDefault("27017")
		int getPort();

		@Config("database")
		String getDatabase();

		@Config("user")
		String getUser();

		@Config("password")
		String getPassword();

		@Config("key")
		List<String> getKey();

		@Config("child")
		@ConfigDefault("null")
		Optional<List<DefineChildDocument>> getChild();

		@Config("bulk_size")
		@ConfigDefault("1000")
		int getBulkSize();

	}

	@Override public ConfigDiff transaction(ConfigSource config, Schema schema, int taskCount, OutputPlugin.Control control)
	{
		PluginTask task = config.loadConfig(PluginTask.class);

		// retryable (idempotent) output:
		// return resume(task.dump(), schema, taskCount, control);

		// non-retryable (non-idempotent) output:

		System.out.println(config.toString());

		control.run(task.dump());
		return Exec.newConfigDiff();
	}

	@Override public ConfigDiff resume(TaskSource taskSource, Schema schema, int taskCount, OutputPlugin.Control control)
	{
		throw new UnsupportedOperationException("mongodb_nest output plugin does not support resuming");
	}

	@Override public void cleanup(TaskSource taskSource, Schema schema, int taskCount, List<TaskReport> successTaskReports)
	{
	}

	@Override public TransactionalPageOutput open(TaskSource taskSource, Schema schema, int taskIndex)
	{
		PluginTask task = taskSource.loadTask(PluginTask.class);

		return new PluginPageOutput(task, schema);
	}
}
