package com.conveyal.taui.persistence;

import com.conveyal.taui.AnalystConfig;
import com.conveyal.taui.models.Bundle;
import com.conveyal.taui.models.Model;
import com.conveyal.taui.models.Modification;
import com.conveyal.taui.models.Scenario;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Manages a single connection to MongoDB for the entire TAUI server process.
 */
public class Persistence {
    private static final Logger LOG = LoggerFactory.getLogger(Persistence.class);

    // TODO deprecated but needed for MongoJack
    private static MongoClient mongo;
    private static DB db;

    public static MongoMap<Modification> modifications;
    public static MongoMap<Scenario> scenarios;
    public static MongoMap<Bundle> bundles;

    public static void initialize () {
        LOG.info("Connecting to MongoDB");
        // allow configurable db connection params

        if (AnalystConfig.databaseUri != null) {
            MongoClientOptions.Builder builder = MongoClientOptions.builder()
                    .sslEnabled(true);
            mongo = new MongoClient(new MongoClientURI(AnalystConfig.databaseUri, builder));
            LOG.info("Connecting to remote MongoDB instance");
        }
        else {
            LOG.info("Connecting to local MongoDB instance");
            mongo = new MongoClient();
        }

        db = mongo.getDB(AnalystConfig.databaseName);

        modifications = getTable("modifications", Modification.class);
        scenarios = getTable("scenarios", Scenario.class);
        bundles = getTable("bundles", Bundle.class);
    }

    /** connect to a table using MongoJack */
    private static <V extends Model> MongoMap<V> getTable (String name, Class clazz) {
        DBCollection collection = db.getCollection(name);
        JacksonDBCollection<V, String> coll = JacksonDBCollection.wrap(collection, clazz, String.class);
        return new MongoMap<>(coll);
    }
}
