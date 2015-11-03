package org.hss.sny.gae;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "statTotalApi",
        version = "v1",
        resource = "statTotal",
        namespace = @ApiNamespace(
                ownerDomain = "gae.sny.hss.org",
                ownerName = "gae.sny.hss.org",
                packagePath = ""
        )
)
public class StatTotalEndpoint {

    private static final Logger logger = Logger.getLogger(StatTotalEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(StatTotal.class);
    }

    /**
     * Returns the {@link StatTotal} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code StatTotal} with the provided ID.
    @ApiMethod(
            name = "get",
            path = "statTotal/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public StatTotal get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting StatTotal with ID: " + id);
        StatTotal statTotal = ofy().load().type(StatTotal.class).id(id).now();
        if (statTotal == null) {
            throw new NotFoundException("Could not find StatTotal with ID: " + id);
        }
        return statTotal;
    }
     */

    /**
     * Inserts a new {@code StatTotal}.
    @ApiMethod(
            name = "insert",
            path = "statTotal",
            httpMethod = ApiMethod.HttpMethod.POST)
    public StatTotal insert(StatTotal statTotal) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that statTotal.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(statTotal).now();
        logger.info("Created StatTotal.");

        return ofy().load().entity(statTotal).now();
    }
     */

    /**
     * Updates an existing {@code StatTotal}.
     *
     * @param id        the ID of the entity to be updated
     * @param statTotal the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code StatTotal}
    @ApiMethod(
            name = "update",
            path = "statTotal/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public StatTotal update(@Named("id") Long id, StatTotal statTotal) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(statTotal).now();
        logger.info("Updated StatTotal: " + statTotal);
        return ofy().load().entity(statTotal).now();
    }
     */

    /**
     * Deletes the specified {@code StatTotal}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code StatTotal}
    @ApiMethod(
            name = "remove",
            path = "statTotal/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(StatTotal.class).id(id).now();
        logger.info("Deleted StatTotal with ID: " + id);
    }
     */

    private static final int NUM_AGE_GROUPS = 7;

    /**
     * List all entities.
     *
     * @param zip
     * @param city
     * @param state
     * @param country
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "statTotal",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<StatTotal> list(@Named("zip") String zip, @Named("city") String city,
                                              @Named("state") String state, @Named("country") String country) {
        int limit = DEFAULT_LIST_LIMIT;
        logger.info("Calling getStatistics method");
        Query<MemberDetails> query = ofy().load().type(MemberDetails.class).limit(limit);

        if (zip != null && zip.length() > 0) { query = query.filter("zipcode", zip); }
        if (city != null && city.length() > 0) { query = query.filter("city", city); }
        if (state != null && state.length() > 0) { query = query.filter("state", state); }
        if (country != null && country.length() > 0) { query = query.filter("country", country); }

        logger.info(zip + " " + city + " " + state + " " + country);

        int [] maxAges = new int[] {10, 20, 30, 40, 50, 60, 100};

        HashMap<Integer, StatTotal> map = new HashMap<>(NUM_AGE_GROUPS);
        Cursor cursor = null;

        while (true) {
            logger.info("looping..");
            if (cursor != null) {
                query = query.startAt(cursor);
            }
            QueryResultIterator<MemberDetails> queryIterator = query.iterator();

            if (!queryIterator.hasNext()) {
                logger.info("Done");
                break;
            }

            while (queryIterator.hasNext()) {
                MemberDetails md = queryIterator.next();
                logger.info(md.getFirstName());
                MemberLogs memberLogs = ofy().load().type(MemberLogs.class).filter("memberId", md.getId()).first().now();
                if (memberLogs != null) {
                    logger.info("total " + memberLogs.getTotal());
                }
                StatTotal entry;
                int i = 0;
                while (maxAges[i] < md.getAge()) i++;
                if (map.containsKey(maxAges[i])) {
                    logger.info("Found age group entry " + maxAges[i]);
                    entry = map.get(maxAges[i]);
                } else {
                    logger.info("creating new age group entry " + maxAges[i]);
                    entry = new StatTotal();
                    int minAge = 0;
                    if (i >= 1) {
                        minAge = maxAges[i-1];
                    }
                    entry.setMaxAge(minAge);
                    map.put(maxAges[i], entry);
                }
                if (md.isMale()) {
                    entry.setMales(entry.getMales() + 1);
                    if (memberLogs != null) {
                        entry.setMcount(entry.getMcount() + memberLogs.getTotal());
                    }
                } else {
                    entry.setFemales(entry.getFemales() + 1);
                    if (memberLogs != null) {
                        entry.setFcount(entry.getFcount() + memberLogs.getTotal());
                    }
                }
                logger.info("entry males " + entry.getMales() + " " + entry.getMcount() + " f " + entry.getFemales() + " " + entry.getFcount() );
            }
            cursor = queryIterator.getCursor();
        }
        return CollectionResponse.<StatTotal>builder().setItems(map.values()).build();
    }
}