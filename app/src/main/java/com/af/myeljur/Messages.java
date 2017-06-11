package com.af.myeljur;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by Peter on 19.02.2017.
 */

public class Messages {

    class  ShortMessage{
        public  int id;
        public  String shortText;
        public  ArrayList<String> senderOrReveivers;
        public  String subject;
        public  String date;
        public  boolean unread;
        public  boolean withFiles;
        public  boolean withResources;
        public  boolean inbox;
        public  ShortMessage(int id, String shortText, ArrayList<String> senderOrReveivers,String subject, String date, boolean unread, boolean withFiles, boolean withResources, boolean inbox ){
            this.id = id;
            this.shortText = shortText;
            this.senderOrReveivers = senderOrReveivers;
            this.subject = subject;
            this.date = date;
            this.unread = unread;
            this.withFiles = withFiles;
            this.withResources = withResources;
            this.inbox = inbox;
        }
    }

    public  interface ShortMessagesCallback{
        void returnMessages(ArrayList<ShortMessage> messages);
        void failedToReturnMessages();
    }

    void getShortMessages(final boolean inbox, boolean unreadonly, final ShortMessagesCallback callback){
        EljurApiRequest getMessages = new EljurApiRequest(EljurApiRequest.Method.GETMESSAGES);
        if(inbox){
            getMessages.addParameter("folder","inbox");
        }else {
            getMessages.addParameter("folder", "sent");
        }
        getMessages.addParameter("unreadonly",String.valueOf(unreadonly));
        EljurApi.execute(getMessages, new EljurApi.pCallback() {
            @Override
            public void onSuccess(String raw) {
                callback.returnMessages(processShortMessages(raw,inbox));
            }

            @Override
            public void onFail() {
                callback.failedToReturnMessages();
            }
        });
    }

    class MessagesComparator implements  Comparator<ShortMessage>{
        @Override
        public int compare(ShortMessage m1, ShortMessage m2) {
            if(m1.id < m2.id){
                return 1;
            }else if(m1.id > m2.id){
                return -1;
            }else {
                return 0;
            }
        }
    }

    ArrayList<ShortMessage> processShortMessages(String raw, boolean inbox){
        JSONObject rawJson;
        ArrayList<ShortMessage> shortMessages = new ArrayList<>();
        try {
            rawJson = (JSONObject) new JSONParser().parse(raw);
        } catch (ParseException e) {
            return shortMessages;
        }
        rawJson = ((JSONObject) ((JSONObject) rawJson.get("response")).get("result"));
        int total = Integer.parseInt(rawJson.get("total").toString());
        int count = Integer.parseInt(rawJson.get("count").toString());
        Object[] messages = ((JSONArray) rawJson.get("messages")).toArray();
        for(Object m: messages){
            JSONObject mg = (JSONObject) m;
            int id = Integer.parseInt(mg.get("id").toString());
            String shortText = iHateBRsAndNewLines( NNHP.getStringFromJsonObject(mg, "short_text"));
            JSONObject jSender;
            ArrayList<String> senderOReceivers;
            if(inbox){
                senderOReceivers = new ArrayList<>(1);

                if(mg.get("user_from") == null)
                    continue;

                jSender = (JSONObject) mg.get("user_from");
                senderOReceivers.add(NNHP.getNameFromJson(jSender));
            }else {
                senderOReceivers = new ArrayList<>();

                if (mg.get("users_to") == null)
                    continue;

                Object[] receivers = ((JSONArray) mg.get("users_to")).toArray();

                for(Object r:receivers){
                    jSender = (JSONObject) r;
                    senderOReceivers.add(NNHP.getNameFromJson(jSender));
                }
            }

            String subject = NNHP.getStringFromJsonObject(mg, "subject");
            String rawDate = NNHP.getStringFromJsonObject(mg, "date");
            Date mDate;
            try {
                mDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rawDate);
            } catch (java.text.ParseException e) {
                mDate = new Date();
            }
            String date = new SimpleDateFormat("HH:mm/dd.MM.yy").format(mDate);
            boolean unread = NNHP.getBoolFromJsonObject(mg, "unread",false);
            boolean withFiles = NNHP.getBoolFromJsonObject(mg,"with_files",false);
            boolean withResources = NNHP.getBoolFromJsonObject(mg, "with_resources",false);
            ShortMessage sM = new ShortMessage(id, shortText,senderOReceivers,subject,date,unread,withFiles,withResources, inbox);
            shortMessages.add(sM);
        }
        Collections.sort(shortMessages, new MessagesComparator());
        return shortMessages;
    }

    String iHateBRsAndNewLines(String s){
        return s.replaceAll(Pattern.quote("<br />"),"").replaceAll("(?m)^[ \t]*\r?\n", "").replaceAll("\n", " ");
    }

    class File{
        public String name;
        public  String link;
        public  File(String name,String link){
            this.name = name;
            this.link = link;
        }
    }

    class Message{
        public int id;
        public String text;
        public String subject;
        public  String date;
        public ArrayList<File> files;
        public  String sender;
        public  ArrayList<String> receivers;
        public  Message(String text, String sender, ArrayList<String> receivers, String subject, String date, ArrayList<File> files){
            if(files!=null)
                this.files = files;
            this.text = text;
            this.sender = sender;
            this.receivers = receivers;
            this.subject = subject;
            this.date = date;
        }

    }

    public  interface  MessageCallback{
        void Success(Message m);
        void Fail();
    }

    void getMessage(int id, final MessageCallback messageCallback){
        EljurApiRequest getMessageInfo = new EljurApiRequest(EljurApiRequest.Method.GETMESSAGEINFO).addParameter("id", String.valueOf(id));
        EljurApi.execute(getMessageInfo, new EljurApi.pCallback() {
            @Override
            public void onSuccess(String raw) {
                messageCallback.Success(processMessage(raw));
            }

            @Override
            public void onFail() {
                messageCallback.Fail();
            }
        });
    }

    Message processMessage(String raw){
        JSONObject rawMessage;
        try {
            rawMessage = (JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(raw)).get("response")).get("result")).get("message");
        } catch (ParseException e) {
            return null;
        }

        String date = NNHP.getStringFromJsonObject(rawMessage,"date");
        String subject = NNHP.getStringFromJsonObject(rawMessage,"subject");
        String text = NNHP.getStringFromJsonObject(rawMessage, "text");
        String sender;
        if(rawMessage.get("user_from")!=null) {
            JSONObject jSender;
            jSender = (JSONObject) rawMessage.get("user_from");
            sender = NNHP.getNameFromJson(jSender);
        }else {
            sender = "Неизвестно";
        }
        ArrayList<String> receivers = new ArrayList<>();
        if(rawMessage.get("user_to")!=null){
        Object[] rawReceivers = ((JSONArray) rawMessage.get("user_to")).toArray();
            for(Object r:rawReceivers){
                JSONObject receiver = (JSONObject)r;
                String rr = NNHP.getNameFromJson(receiver);
                receivers.add(rr);
            }
        }
        ArrayList<File> files = new ArrayList<>();
        if(rawMessage.get("files")!=null){
            Object[] fs = ((JSONArray)rawMessage.get("files")).toArray();
            for (Object rawF :fs){
                JSONObject file = (JSONObject) rawF;
                String name = NNHP.getStringFromJsonObject(file, "filename");
                String link = NNHP.getStringFromJsonObject(file,"link");
                files.add(new File(name,link));
            }
        }
        return new Message(text,sender,receivers,subject,date,files);

    }

    public interface ReceiversListCallback{
        void Success(ReceiversContainer rc);
        void  Fail();
    }


    class  ReceiversContainer{
        ArrayList<String> groups;
        HashMap<String, ArrayList<Person>> childrenGroups;
        public ReceiversContainer(ArrayList<String> groups, HashMap<String, ArrayList<Person>> childrenGroups){
            this.groups = groups;
            this.childrenGroups = childrenGroups;
        }
    }

    class Person{
        public String id;
        public String name;
        public String info;
        public Person(String id,String name,String info){
            this.id = id;
            this.name = name;
            this.info = info;
        }
    }

    void getReceiversList(final ReceiversListCallback callback){
        EljurApiRequest getReceivers = new EljurApiRequest(EljurApiRequest.Method.GETMESSAGERECEIVERS);
        EljurApi.execute(getReceivers, new EljurApi.pCallback() {
            @Override
            public void onSuccess(String raw) {
                callback.Success(processReceiversList(raw));
            }

            @Override
            public void onFail() {
                callback.Fail();
            }
        });
    }

    class PersonComparator implements Comparator<Person>{
        @Override
        public int compare(Person p1, Person p2) {
            return p1.name.compareTo(p2.name);
        }
    }

    ReceiversContainer processReceiversList(String raw){
        Object[] groups;
        try {
            groups = ((JSONArray) ((JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(raw)).get("response")).get("result")).get("groups")).toArray();
        } catch (ParseException e) {
            return null;
        }

        ArrayList<String> groupTags = new ArrayList<>();
        HashMap<String,ArrayList<Person>> childrenGroups = new HashMap<>();
        for (Object g:groups){
            JSONObject group = (JSONObject)g;

            ArrayList<Person> personsInGroup = new ArrayList<>();
            String groupName = NNHP.getStringFromJsonObject(group,"name");

            if(group.get("subgroups")!=null){
                Object[] subgroups = ((JSONArray)group.get("subgroups")).toArray();
                for (Object sg:subgroups){
                    Object[] users = ((JSONArray)((JSONObject)sg).get("users")).toArray();
                    for (Object u:users){
                        JSONObject jU = (JSONObject)u;
                        String id = NNHP.getStringFromJsonObject(jU,"name");
                        String name = NNHP.getNameFromJson(jU);
                        String info = "";
                        if(jU.get("info")!=null){
                            info = jU.get("info").toString();
                        }

                        Person p = new Person(id,name,info);
                        personsInGroup.add(p);

                    }
                }
            }else {
                Object[] users = ((JSONArray)((JSONObject)g).get("users")).toArray();
                for (Object u:users){
                    JSONObject jU = (JSONObject)u;
                    String id = NNHP.getStringFromJsonObject(jU,"name");
                    String name = NNHP.getNameFromJson(jU);
                    String info = "";
                    if(jU.get("info")!=null){
                        info = jU.get("info").toString();
                    }
                    Person p = new Person(id,name,info);
                    personsInGroup.add(p);

                }
            }

            Collections.sort(personsInGroup,new PersonComparator());

            groupTags.add(groupName);
            childrenGroups.put(groupName,personsInGroup);
        }

        return  new ReceiversContainer(groupTags,childrenGroups);

    }

    interface SendMessageCallback{
        void Success();
        void Fail();
    }

    void sendMessage(ArrayList<String> receivers, String subject, String text, final SendMessageCallback callback){
        StringBuilder r = new StringBuilder();
        for(String s:receivers){
            r.append(s+",");
        }
        String aReceivers = r.substring(0,r.length()-1);

        EljurApiRequest sendMessage = new EljurApiRequest(EljurApiRequest.Method.SENDMESSAGE).addParameter("users_to",aReceivers).addParameter("subject", subject).addParameter("text",text);
        EljurApi.execute(sendMessage, new EljurApi.pCallback() {
            @Override
            public void onSuccess(String raw) {
                if(raw.contains("200")){
                    callback.Success();
                }else {
                    callback.Fail();
                }
            }

            @Override
            public void onFail() {
                    callback.Fail();
            }
        });
    }

}
