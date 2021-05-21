package org.embulk.output.mongodb_nest;
import java.text.SimpleDateFormat;
import java.util.*;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.mongodb.BasicDBObject;
import org.embulk.config.*;
import org.embulk.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

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

	public interface DefineNullValue extends Task
	{
		@Config("string")
		@ConfigDefault("")
		String getString();

		@Config("json")
		@ConfigDefault("{}")
		BasicDBObject getJson();

		@Config("boolean")
		@ConfigDefault("false")
		Boolean getBoolean();

		@Config("double")
		@ConfigDefault("0.0")
		Double getDouble();

		@Config("long")
		@ConfigDefault("0")
		Integer getLong();

		@Config("timestamp")
		@ConfigDefault("0")
		java.sql.Timestamp getTimestamp();
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

		@Config("ssl")
		@ConfigDefault("false")
		boolean getSsl();

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
		@ConfigDefault("10000")
		int getBulkSize();

		@Config("null_value")
		@ConfigDefault("{\"string\": \"\", \"boolean\":\"false\"}")
		Optional<DefineNullValue> getNullValue();

	}

	@Override public ConfigDiff transaction(ConfigSource config, Schema schema, int taskCount, OutputPlugin.Control control)
	{
		PluginTask task = config.loadConfig(PluginTask.class);

		// retryable (idempotent) output:
		// return resume(task.dump(), schema, taskCount, control);

		// non-retryable (non-idempotent) output:

		logger.debug(task.toString());

		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		Calendar calendar = Calendar.getInstance(timeZone);
		SimpleDateFormat simpleDateFormat =
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z' zzz", Locale.KOREA);
		simpleDateFormat.setTimeZone(timeZone);

		logger.info("* setup null value");
		logger.info("  String value    = \"{}\"", task.getNullValue().get().getString());
		logger.info("  Long value      = {}", task.getNullValue().get().getLong());
		logger.info("  Timestamp value = {}", simpleDateFormat.format(task.getNullValue().get().getTimestamp()));
		logger.info("  Boolean value   = {}", task.getNullValue().get().getBoolean());
		logger.info("  Double value    = {}", task.getNullValue().get().getDouble());
		logger.info("  Json value      = {}", task.getNullValue().get().getJson());

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
