package parashar.vijay.karate.demo.nakadi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.apache.commons.compress.utils.Lists;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.fahrschein.AuthorizationBuilder;
import org.zalando.fahrschein.EventAlreadyProcessedException;
import org.zalando.fahrschein.Listener;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.fahrschein.domain.Authorization;
import org.zalando.fahrschein.domain.Subscription;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class NakadiEventListener implements Listener<Map> {
    public static final Logger log = LoggerFactory.getLogger(NakadiEventListener.class);
    public static final int TWO_THOUSAND_MILLIS = 2000;

    private NakadiClient nakadiClient;
    private static NakadiEventListener instance;

    final private DataSource dataSource;
    private ObjectMapper objectMapper;


    private NakadiEventListener() throws IOException {

        this.dataSource = prepareDB();
        this.objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);


        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("nakadi.properties"));

        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        nakadiClient =
                NakadiClient.builder(URI.create(properties.getProperty("url")))
                        .withAccessTokenProvider(() -> TokenHelper.get("nakadi"))
                        .withObjectMapper(mapper)
                        .build();

        String[] eventTypes = properties.getProperty("events").split(",");


        new Thread(
                () -> {
                    try {
                        final Subscription subscription;
                        final String applicationName = "karate-demo";
                        subscription =
                                nakadiClient
                                        .subscription(applicationName, Sets.newHashSet(eventTypes))
                                        .withAuthorization(AuthorizationBuilder.authorization()
                                                .addAdmin("user", properties.getProperty("authorization.user"))
                                                .withReaders(Authorization.AuthorizationAttribute.ANYONE)
                                                .build())
                                        .readFromBegin()
                                        .subscribe();
                        nakadiClient.stream(subscription).listen(Map.class, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .start();
    }

    private DataSource prepareDB() {
        final EmbeddedPostgres embeddedPostgres;
        try {
            embeddedPostgres = EmbeddedPostgres.start();
            final DataSource database = embeddedPostgres.getPostgresDatabase();
            final Connection connection = database.getConnection();

            createDatabase(connection);
            return database;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void accept(List<Map> events) throws IOException, EventAlreadyProcessedException {
        events
                .stream()
                .forEach(
                        event -> {
                            final Map metadata = (Map) event.get("metadata");
                            final String eventType = (String) metadata.get("event_type");
                            final String eventId = (String) metadata.get("eid");

                            log.info("<<<<------RECEIVED------->>>> {}", eventType);
                            log.debug("Body {} ", event);

                            saveEventToDatabase(event, eventType, eventId);
                        });
    }

    private void saveEventToDatabase(Map event, String eventType, String eventId) {
        String sql = "insert into event (e_event_type,e_event,e_eid) values (?,?,?)";
        try {
            final PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, eventType);
            PGobject pGobject = new PGobject();
            pGobject.setType("jsonb");
            pGobject.setValue(objectMapper.writeValueAsString(event));
            preparedStatement.setObject(2, pGobject);
            preparedStatement.setString(3, eventId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static NakadiEventListener instance() {
        if (instance == null) {
            try {
                instance = new NakadiEventListener();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private void createDatabase(Connection connection) {
        try {
            final InputStream stream =
                    getClass().getClassLoader().getResourceAsStream("db/table.sql");

            log.debug("Steam {} ", stream);
            final String sqlScript = CharStreams.toString(new InputStreamReader(stream));
            final String[] sqls = sqlScript.split(";");
            final Statement statement = connection.createStatement();
            Arrays.stream(sqls)
                    .forEach(
                            sql -> {
                                try {
                                    log.info("Executing {}", sql);
                                    statement.execute(sql);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            });

            log.info("Creation of DB is Successful ...");

        } catch (IOException e) {
            log.error("Error in creating DB programmatically ", e);
            log.error(
                    Arrays.stream(e.getStackTrace())
                            .map(stackTraceElement -> stackTraceElement.toString())
                            .collect(Collectors.joining("\n")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        final NakadiEventListener nakadiEventListener = instance();
        try {
            Thread.sleep(20000);
            final Map nakadiEvent = nakadiEventListener.event("de.zalando.logistics.wolf.movements.process_status_changed",
                    "e_event->>'group_reference' = 'PG-MG-20190910-13-02-00000027' \n" +
                            " and e_event->>'process_type' = '/process-types/bgs_dropout' \n" +
                            " and e_event->>'status' = 'ACKNOWLEDGED'"
            );
            System.out.println(nakadiEvent.get("events"));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map event(String eventType, String whereClause) throws SQLException, IOException {
        String sql = "select e_event  from event where e_event_type = '" + eventType + "' and " + whereClause;
        final Statement statement = dataSource.getConnection().createStatement();
        final ResultSet resultSet = statement.executeQuery(sql);

        Map response = Maps.newHashMap();
        final List<Map> events = Lists.newArrayList();

        response.put("events", events);
        while (resultSet.next()) {
            final String eventStr = resultSet.getString("e_event");
            final Map event = objectMapper.readValue(eventStr, Map.class);
            events.add(event);
        }
        return response;
    }

}
