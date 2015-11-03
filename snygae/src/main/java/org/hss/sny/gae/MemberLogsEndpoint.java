package org.hss.sny.gae;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;
import com.googlecode.objectify.ObjectifyService;

import java.util.logging.Logger;

import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "memberLogsApi",
        version = "v1",
        resource = "memberLogs",
        namespace = @ApiNamespace(
                ownerDomain = "gae.sny.hss.org",
                ownerName = "gae.sny.hss.org",
                packagePath = ""
        )
)
public class MemberLogsEndpoint {

    private static final Logger logger = Logger.getLogger(MemberLogsEndpoint.class.getName());

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(MemberLogs.class);
    }

    /**
     * This method gets the <code>MemberLogs</code> object associated with the specified <code>id</code>.
     *
     * @param id The id of the object to be returned.
     * @return The <code>MemberLogs</code> associated with <code>id</code>.
     */
    @ApiMethod(name = "getMemberLogs")
    public MemberLogs getMemberLogs(@Named("id") Long id) throws NotFoundException {
        // TODO: Implement this function
        logger.info("Calling getMemberLogs method");
        MemberLogs memberLogs = ofy().load().type(MemberLogs.class).filter("memberId", id).first().now();
        return memberLogs;

    }

    /**
     * This inserts a new <code>MemberLogs</code> object.
     *
     * @param memberLogs The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertMemberLogs")
    public MemberLogs insertMemberLogs(MemberLogs memberLogs) {
        // TODO: Implement this function
        ofy().save().entity(memberLogs).now();
        logger.info("Inserted MemberLogs");
        return ofy().load().entity(memberLogs).now();
    }
    /**
     * This Updates a new <code>MemberLogs</code> object.
     *
     * @param memberLogs The object to be added.
     * @return The object to be updated.
     */
    @ApiMethod(name = "updateMemberLogs")
    public MemberLogs updateMemberLogs(@Named("id") Long id, MemberLogs memberLogs) throws NotFoundException {
        checkExists(id);
        ofy().save().entity(memberLogs).now();
        logger.info("Updated MemberLogs");
        return ofy().load().entity(memberLogs).now();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(MemberLogs.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find MemberLogs with ID: " + id);
        }
    }

}