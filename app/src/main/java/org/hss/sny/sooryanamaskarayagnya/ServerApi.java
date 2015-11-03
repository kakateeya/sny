package org.hss.sny.sooryanamaskarayagnya;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import org.hss.sny.gae.memberDetailsApi.MemberDetailsApi;
import org.hss.sny.gae.memberDetailsApi.model.MemberDetails;
import org.hss.sny.gae.memberLogsApi.MemberLogsApi;
import org.hss.sny.gae.statTotalApi.StatTotalApi;
import org.hss.sny.gae.topperApi.TopperApi;

import java.io.IOException;

/**
 * Created by sk on 12/15/14.
 */
public class ServerApi {
    private static final boolean LOCAL_SERVER = false;
    private static final boolean TESTING_ON_EMULATOR = false;
    private static final String LOCAL_SERVER_URL = "http://192.168.0.102:8080/_ah/api/";
    // TODO replace this with the actual app ID from google console
    private static final String APP_ENGINE_SERVER_URL = "https://test-app-id.appspot.com/_ah/api/";

    private static MemberDetailsApi memberDetailsApi;
    private static MemberLogsApi memberLogsApi;
    private static StatTotalApi statTotalApi;
    private static TopperApi topperApi;

    private static String getServerUrl() {
        if (LOCAL_SERVER) {
            if (TESTING_ON_EMULATOR) {
                return "http://10.0.2.2:8080/_ah/api/";
            }
            return LOCAL_SERVER_URL;
        }
        return APP_ENGINE_SERVER_URL;
    }

    public static MemberDetailsApi getMemberDetailsApi() {
        if (memberDetailsApi == null) {
            MemberDetailsApi.Builder builder = new MemberDetailsApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    .setRootUrl(getServerUrl())
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            if (LOCAL_SERVER) {
                                // - turn off compression when running against local devappserver
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        }
                    });
            memberDetailsApi = builder.build();
        }
        return memberDetailsApi;
    }

    public static MemberLogsApi getMemberLogsApi() {
        if (memberLogsApi == null) {
            MemberLogsApi.Builder builder = new MemberLogsApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    .setRootUrl(getServerUrl())
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            if (LOCAL_SERVER) {
                                // - turn off compression when running against local devappserver
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        }
                    });
            memberLogsApi = builder.build();
        }
        return memberLogsApi;
    }

    public static StatTotalApi getStatsApi() {
        if (statTotalApi == null) {
            StatTotalApi.Builder builder = new StatTotalApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    .setRootUrl(getServerUrl())
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            if (LOCAL_SERVER) {
                                // - turn off compression when running against local devappserver
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        }
                    });
            statTotalApi = builder.build();
        }
        return statTotalApi;
    }

    public static TopperApi getTopperApi() {
        if (topperApi == null) {
            TopperApi.Builder builder = new TopperApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    .setRootUrl(getServerUrl())
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            if (LOCAL_SERVER) {
                                // - turn off compression when running against local devappserver
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        }
                    });
            topperApi = builder.build();
        }
        return topperApi;
    }

    public static Member getMember(MemberDetails memberDetails) {
        Member member = new Member();
        member.id = memberDetails.getId();
        member.uid = memberDetails.getUid();
        member.primaryId = memberDetails.getPrimaryId();
        member.email = memberDetails.getEmail();
        member.firstName = memberDetails.getFirstName();
        member.lastName = memberDetails.getLastName();
        member.age = memberDetails.getAge();
        member.isMale = memberDetails.getMale();
        member.city = memberDetails.getCity();
        member.state = memberDetails.getState();
        member.countryIdx = memberDetails.getCountryIdx();
        member.country = memberDetails.getCountry();
        member.zipcode = memberDetails.getZipcode();
        return member;
    }

    public static MemberDetails getMemberDetails (Member member) {
        MemberDetails md = new MemberDetails();
        if (member.id > 0) {
            md.setId(member.id);
        }
        md.setUid(member.uid);
        md.setPrimaryId(member.primaryId);
        md.setEmail(member.email);
        md.setFirstName(member.firstName);
        md.setLastName(member.lastName);
        md.setAge(member.age);
        md.setMale(member.isMale);
        md.setCity(member.city);
        md.setState(member.state);
        md.setCountryIdx(member.countryIdx);
        md.setCountry(member.country);
        md.setZipcode(member.zipcode);
        return md;
    }

}
