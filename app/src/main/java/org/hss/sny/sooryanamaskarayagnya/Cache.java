package org.hss.sny.sooryanamaskarayagnya;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.apache.http.HttpStatus;
import org.hss.sny.gae.memberDetailsApi.model.MemberDetails;
import org.hss.sny.gae.memberLogsApi.model.MemberLogs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by sk on 12/22/14.
 */
public class Cache {
    private static Cache mInstance;

    public static Cache getInstance(Context context) {
        if (mInstance == null) {
            synchronized (Cache.class) {
                mInstance = new Cache(context.getApplicationContext());
            }
        }
        return  mInstance;
    }

    private Context mContext;
    private HashMap<Long, Member> mMemberMap;
    private HashMap<Long, CountLog> mLogMap;
    private HashMap<String, Member> mUidMemberMap;

    private Cache(Context context) {
        mContext = context;
        mMemberMap = new HashMap<>();
        mLogMap = new HashMap<>();
        mUidMemberMap = new HashMap<>();
    }

    public List<Member> loadMembers() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set<String> memberIds = sharedPreferences.getStringSet(Constants.SHARED_PREF_MEMBERS, null);
        ArrayList<Member> members = new ArrayList<>();
        if (memberIds != null) {
            for (String memberId: memberIds) {
                Member member = getMember(memberId);
                if (member != null) {
                    members.add(member);
                    getLog(member.id);
                }
            }
        }
        return members;
    }

    public Member getMember (String memberUid) {

        if (mUidMemberMap.containsKey(memberUid)) return mUidMemberMap.get(memberUid);

        Member member = null;
        try {
            FileInputStream fis = mContext.openFileInput(memberUid);
            member = new Gson().fromJson(new JsonReader(new InputStreamReader(fis)), Member.class);
            mMemberMap.put(member.id, member);
            mUidMemberMap.put(memberUid, member);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return member;
    }

    public void putMember(Member member) {
        mMemberMap.put(member.id, member);
        mUidMemberMap.put(member.uid, member);
    }

    public void putMember(MemberDetails memberDetails) {
        Member member = ServerApi.getMember(memberDetails);
        putMember(member);
    }

    public int getTotal(long memberId) {
        CountLog clog = getLog(memberId, false);
        if (clog == null) {return 0;}
        return clog.total;
    }


    public static class CountLog {
        Long id;
        Long memberId;
        int total;
        TreeMap<Long, Integer> logs;

        public CountLog(long mId) {
            id = new Long(0);
            memberId = mId;
            total = 0;
            logs = new TreeMap<>(Collections.reverseOrder());
        }

        public CountLog(Member member) {
            id = new Long(0);
            memberId = member.id;
            total = 0;
            logs = new TreeMap<>(Collections.reverseOrder());
        }

        public CountLog(MemberLogs mlogs) {
            id = mlogs.getId();
            memberId = mlogs.getMemberId();
            total = mlogs.getTotal();
            logs = new TreeMap<>(Collections.reverseOrder());
            logs = new Gson().fromJson(mlogs.getLogs(), logs.getClass());
        }

        public MemberLogs toMemberLogs() {
            MemberLogs mlogs = new MemberLogs();
            if (id > 0) {
                mlogs.setId(id);
            }
            mlogs.setMemberId(memberId);
            mlogs.setTotal(total);
            mlogs.setLogs(new Gson().toJson(logs, logs.getClass()));
            return mlogs;
        }
    }
    public CountLog getLog (long memberId) {
        return getLog(memberId, true);
    }

    public CountLog getLog (long memberId, boolean load) {
        if (mLogMap.containsKey(memberId)) return mLogMap.get(memberId);
        else if (!load) {
            return null;
        }

        String logsFile = Long.toString(memberId) + Constants.LOG_FILE_NAME;
        CountLog logs = null;
        try {
            logs = readLogsFromFile(logsFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (logs == null) {
            try {
                MemberLogs memberLogs = ServerApi.getMemberLogsApi().getMemberLogs(memberId).execute();
                if (memberLogs != null) {
                    logs = new CountLog(memberLogs);
                } else {
                    logs = new CountLog(memberId);
                }
                //write and then read from file to work around GSON issue
                writeLogsToFile(logsFile, logs);
                logs = readLogsFromFile(logsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (logs != null) {
            mLogMap.put(memberId, logs);
        }
        return logs;
    }

    public CountLog putLog (CountLog log) {
        mLogMap.put(log.memberId, log);
        MemberLogs memberLogs = log.toMemberLogs();
        try {
            Long logId = log.id;
            if (logId == null || logId == 0) {
                MemberLogs xlogs = ServerApi.getMemberLogsApi().insertMemberLogs(memberLogs).execute();
                if (xlogs != null) {
                    memberLogs.setId(xlogs.getId());
                    log.id = xlogs.getId();
                }
            } else {
                try {
                    ServerApi.getMemberLogsApi().updateMemberLogs(logId, memberLogs).execute();
                } catch (GoogleJsonResponseException gjre) {
                    if (gjre.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                        //log removed on server?
                        Log.e("Cache", "Log seems to have been removed. Resetting to 0");
                        mLogMap.remove(log.memberId);
                        mContext.getFileStreamPath(getLogFileName(log.memberId)).delete();
                        log = null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (log != null) {
            writeLogsToFile(getLogFileName(log.memberId), log);
        }
        return log;
    }

    public CountLog resetLogs(long memberId) {
        try {
            CountLog oldLogs = getLog(memberId, false);
            CountLog newLogs = new CountLog(memberId);
            if (oldLogs != null) {
                newLogs.id = oldLogs.id;
            }
            return putLog(newLogs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getLogFileName(long memberId) {
        return Long.toString(memberId) + Constants.LOG_FILE_NAME;
    }

    private void writeLogsToFile(String logsFile, CountLog clog) {
        try {
            Gson gson = new Gson();
            FileOutputStream fos = mContext.openFileOutput(logsFile, Context.MODE_PRIVATE);
            fos.write(gson.toJson(clog, CountLog.class).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CountLog readLogsFromFile(String logsFile) throws FileNotFoundException, IOException {
        Gson gson = new Gson();
        FileInputStream fis = mContext.openFileInput(logsFile);
        String json = readFile(mContext.getFileStreamPath(logsFile).getAbsolutePath());
        CountLog clog = gson.fromJson(json, CountLog.class);
        return clog;
    }

    String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

}
