package org.embulk.output.mongodb_nest;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import org.bson.BSON;
import org.bson.BSONObject;
import org.bson.Document;
import org.embulk.config.TaskReport;
import org.embulk.spi.*;
import org.embulk.spi.time.Timestamp;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class PluginPageOutput implements TransactionalPageOutput
{
	private static final Logger logger = Exec.getLogger(MongodbNestOutputPlugin.class);

	private final MongodbNestOutputPlugin.PluginTask task;
	private final Schema                             schema;
	private final PageReader                         pageReader;

	private MongoClient               mongo;
	private MongoDatabase             db;
	private MongoCollection<Document> collection;


	PluginPageOutput(MongodbNestOutputPlugin.PluginTask task, Schema schema)
	{
		this.pageReader = new PageReader(schema);
		this.schema = schema;
		this.task = task;

		String connectionStr = "mongodb://";

		if (task.getUser() != null)
		{
			connectionStr += task.getUser();
			try
			{
				connectionStr += ":" + URLEncoder.encode(task.getPassword(), "UTF-8") + "@";
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}

		connectionStr += task.getHost() + ":" + task.getPort() + "/" + task.getDatabase();

		this.mongo = MongoClients.create(connectionStr);
		this.db = this.mongo.getDatabase(task.getDatabase());
		this.collection = this.db.getCollection(task.getCollection());
	}

	@Override
	public void add(Page page)
	{
		pageReader.setPage(page);
		List<WriteModel<Document>> replaceModel = new ArrayList<>();

		while (pageReader.nextRecord())
		{
			BasicDBObject doc = new BasicDBObject();

			for (int i = 0; i < schema.getColumnCount(); i++)
			{
				String t = schema.getColumnName(i);
				Class<?> type = schema.getColumnType(i).getJavaType();

				boolean isnull = pageReader.isNull(i);

				if(schema.getColumnType(i).getName().compareTo("json") == 0)
				{
					doc.putAll(isnull ? task.getNullValue().get().getJson() : (BSONObject) BasicDBObject.parse(pageReader.getJson(i).toJson()));
				}
				else if (type.equals(boolean.class))
				{
					doc.append(t, isnull ? task.getNullValue().get().getBoolean() : pageReader.getBoolean(i));
				}
				else if (type.equals(double.class))
				{
					doc.append(t, isnull ? task.getNullValue().get().getDouble() : pageReader.getDouble(i));
				}
				else if (type.equals(long.class))
				{
					doc.append(t, isnull ? task.getNullValue().get().getLong() : pageReader.getLong(i));
				}
				else if (type.equals(String.class))
				{
					doc.append(t, isnull ? task.getNullValue().get().getString() : pageReader.getString(i));
				}
				else if (type.equals(Timestamp.class))
				{
					doc.append(t, isnull ? task.getNullValue().get().getTimestamp() : new java.sql.Timestamp(pageReader.getTimestamp(i).toEpochMilli()));
				}

			}

			if (task.getChild().isPresent())
			{
				doc = transformDocument(doc);
			}

			replaceModel.add(new ReplaceOneModel<>(
					generateFilter(doc),
					new Document(doc),
					new ReplaceOptions().upsert(true)));

			if(replaceModel.size() % task.getBulkSize() == 0)
			{
				collection.bulkWrite(replaceModel);
				replaceModel.clear();
			}
		}

		if (replaceModel.size() > 0)
		{
			collection.bulkWrite(replaceModel);
		}
	}

	private Document generateFilter(BasicDBObject document)
	{
		Document filter = new Document();

		for(String k : task.getKey())
		{
			filter.append(k, document.get(k));
		}

		return filter;
	}

	private BasicDBObject transformDocument(BasicDBObject doc)
	{
		for (MongodbNestOutputPlugin.DefineChildDocument cd : task.getChild().get())
		{
			String name = cd.getName();
			String field = cd.getField();

			BasicDBObject subdoc = new BasicDBObject();
			Object exists = doc.get(name);

			if (exists != null)
			{
				subdoc.putAll((Map) exists);
			}

			subdoc.append(field, doc.remove(field));
			doc.append(name, subdoc);
		}

		return doc;
	}

	@Override
	public void finish()
	{

	}

	@Override
	public void close()
	{
		this.mongo.close();
	}

	@Override
	public void abort()
	{

	}

	@Override
	public TaskReport commit()
	{
		return null;
	}
}
