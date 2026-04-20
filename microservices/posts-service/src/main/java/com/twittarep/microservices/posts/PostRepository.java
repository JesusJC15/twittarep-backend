package com.twittarep.microservices.posts;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class PostRepository {

    private final MongoCollection<PostDocument> collection;

    public PostRepository() {
        this(System.getenv("MONGODB_URI"), System.getenv().getOrDefault("POSTS_DATABASE_NAME", "twittarep_posts_service"));
    }

    protected PostRepository(boolean initialize) {
        this.collection = null;
    }

    PostRepository(String mongoUri, String databaseName) {
        var pojoCodecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );
        MongoClientSettings settings = MongoClientSettings.builder()
            .codecRegistry(pojoCodecRegistry)
            .applyConnectionString(new ConnectionString(mongoUri))
            .build();
        MongoClient client = MongoClients.create(settings);
        MongoDatabase database = client.getDatabase(databaseName);
        this.collection = database.getCollection("posts", PostDocument.class);
        this.collection.createIndex(Indexes.descending("createdAt"));
    }

    public PostDocument save(PostDocument document) {
        ensureCollection();
        if (document.getId() == null) {
            document.setId(UUID.randomUUID().toString());
        }
        collection.insertOne(document);
        return document;
    }

    public List<PostDocument> findPage(int page, int size) {
        ensureCollection();
        List<PostDocument> documents = new ArrayList<>();
        collection.find()
            .sort(Sorts.descending("createdAt"))
            .skip(page * size)
            .limit(size)
            .into(documents);
        return documents;
    }

    public long count() {
        ensureCollection();
        return collection.countDocuments();
    }

    private void ensureCollection() {
        if (collection == null) {
            throw new IllegalStateException("Collection not initialized");
        }
    }
}
