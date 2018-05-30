Embulk::JavaPlugin.register_output(
  "mongodb_nest", "org.embulk.output.mongodb_nest.MongodbNestOutputPlugin",
  File.expand_path('../../../../classpath', __FILE__))
