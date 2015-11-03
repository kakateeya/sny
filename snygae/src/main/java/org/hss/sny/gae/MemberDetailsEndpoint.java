package org.hss.sny.gae;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        name = "memberDetailsApi",
        version = "v1",
        resource = "memberDetails",
        namespace = @ApiNamespace(
                ownerDomain = "gae.sny.hss.org",
                ownerName = "gae.sny.hss.org",
                packagePath = ""
        )
)
public class MemberDetailsEndpoint {

    private static final Logger logger = Logger.getLogger(MemberDetailsEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(MemberDetails.class);
    }

    /**
     * Returns the {@link MemberDetails} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code MemberDetails} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "memberDetails/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public MemberDetails get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting MemberDetails with ID: " + id);
        MemberDetails memberDetails = ofy().load().type(MemberDetails.class).id(id).now();
        if (memberDetails == null) {
            throw new NotFoundException("Could not find MemberDetails with ID: " + id);
        }
        return memberDetails;
    }

    /**
     * Returns the {@link MemberDetails} with the corresponding ID.
     *
     * @param email the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code MemberDetails} with the provided ID.
     */
    @ApiMethod(
            name = "check",
            path = "memberDetails/email/{email}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public List<MemberDetails> check(@Named("email") String email) throws NotFoundException {
        logger.info("Getting MemberDetails with email: " + email);
        List<MemberDetails> membersDetails = ofy().load().type(MemberDetails.class).filter("email", email).list();
        if (membersDetails != null && membersDetails.size() > 0) {
            logger.info("Found members " + membersDetails.size());
            List<MemberDetails> membersDetails2 = ofy().load().type(MemberDetails.class).filter("primaryId", membersDetails.get(0).getPrimaryId()).list();
            if (membersDetails2 != null && membersDetails2.size() > 0) {
                logger.info("Found members " + membersDetails2.size());
                membersDetails = membersDetails2;
            } else {
                logger.info("Could not filter by primary Id");
            }
        } else {
            logger.info("No member found");
        }
        return membersDetails;
        /*
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        com.google.appengine.api.datastore.Query.Filter filter =
                new com.google.appengine.api.datastore.Query.FilterPredicate("email",
                        com.google.appengine.api.datastore.Query.FilterOperator.EQUAL,
                        email);

        com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query("MemberDetails").setFilter(filter);
        PreparedQuery pq = datastore.prepare(q);
        Entity result = pq.asSingleEntity();
        if (result != null) {
            logger.info(result.toString());
        } else {
            logger.info("email not found " + email);
        }
        return null;
        */
    }

    /**
     * Inserts a new {@code MemberDetails}.
     */
    @ApiMethod(
            name = "insert",
            path = "memberDetails",
            httpMethod = ApiMethod.HttpMethod.POST)
    public MemberDetails insert(MemberDetails memberDetails) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that memberDetails.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(memberDetails).now();
        if (memberDetails.getPrimaryId() == 0) {
            memberDetails.setPrimaryId(memberDetails.getId());
            ofy().save().entity(memberDetails).now();
        }
        logger.info("Created MemberDetails.");

        return ofy().load().entity(memberDetails).now();
    }

    /**
     * Updates an existing {@code MemberDetails}.
     *
     * @param id            the ID of the entity to be updated
     * @param memberDetails the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code MemberDetails}
     */
    @ApiMethod(
            name = "update",
            path = "memberDetails/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public MemberDetails update(@Named("id") Long id, MemberDetails memberDetails) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(memberDetails).now();
        logger.info("Updated MemberDetails: " + memberDetails);
        return ofy().load().entity(memberDetails).now();
    }

    /**
     * Deletes the specified {@code MemberDetails}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code MemberDetails}
     */
    @ApiMethod(
            name = "remove",
            path = "memberDetails/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(MemberDetails.class).id(id).now();
        logger.info("Deleted MemberDetails with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "memberDetails",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<MemberDetails> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<MemberDetails> query = ofy().load().type(MemberDetails.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<MemberDetails> queryIterator = query.iterator();
        List<MemberDetails> memberDetailsList = new ArrayList<MemberDetails>(limit);
        while (queryIterator.hasNext()) {
            memberDetailsList.add(queryIterator.next());
        }
        return CollectionResponse.<MemberDetails>builder().setItems(memberDetailsList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(MemberDetails.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find MemberDetails with ID: " + id);
        }
    }
}