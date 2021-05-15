package com.example.gianmarco.bluethings.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {



    private static int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "BlueThings.db";

    private static  String Table_Contacts = "CONTATTI";
    private static  String Table_Chat = "CHAT";
    private static  String Table_Chat_off = "CHATOFF";
    private static  String Table_Pictures = "PICTURES";
    private static  String Table_User = "USER";


    private static  String contacts_COL1 = "ID";
    private static  String contacts_COL2 = "DeviceName";
    private static  String contacts_COL3 = "DeviceAddress";
    private static  String contacts_COL4 = "NickName";
    private static  String contacts_COL5 = "Avatar";

    private static  String chat_COL1 = "ID";
    private static  String chat_COL2 = "Position";
    private static  String chat_COL3 = "Text";

    private static  String chat_off_COL1 = "ID";
    private static  String chat_off_COL2 = "Position";
    private static  String chat_off_COL3 = "Text";

    private static  String pictures_COL1 = "ID";
    private static  String pictures_COL2 = "Picture";

    private static  String user_COL1 = "NickName";
    private static  String user_COL2 = "Avatar";


    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null,7);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name , factory, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE " + Table_Contacts + " ("+
                contacts_COL1 + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                                "REFERENCES " + Table_Chat_off + "(" + chat_off_COL1 + ") "+
                                "REFERENCES " + Table_Chat + "(" + chat_COL1 + ") "+
                                "ON DELETE CASCADE, " +
                contacts_COL2 + " TEXT NOT NULL UNIQUE, " +
                contacts_COL3 + " TEXT NOT NULL UNIQUE, " +
                contacts_COL4 + " TEXT NOT NULL UNIQUE, " +
                contacts_COL5 + " BLOB DEFAULT NULL)";

        db.execSQL(createTable);



        String createTableTwo = "CREATE TABLE " + Table_Chat + " ("+
                chat_COL1 + " INTEGER, " +
                chat_COL2 + " INTEGER, " +
                chat_COL3 + " TEXT, " +
                "PRIMARY KEY(" + chat_COL1 + "," + chat_COL2 + "))";

        db.execSQL(createTableTwo);



        String createTableThree = "CREATE TABLE " + Table_Chat_off + " ("+
                chat_off_COL1 + " INTEGER, " +
                chat_off_COL2 + " INTEGER, " +
                chat_off_COL3 + " TEXT, " +
                "PRIMARY KEY(" + chat_COL1 + "," + chat_COL2 + "))";

        db.execSQL(createTableThree);



        String createTableFour = "CREATE TABLE " + Table_Pictures + " ("+
                pictures_COL1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                pictures_COL2 + " BLOB)";

        db.execSQL(createTableFour);



        String createTableFive = "CREATE TABLE " + Table_User + " ("+
                user_COL1 + " TEXT PRIMARY KEY, " +
                user_COL2 + " BLOB DEFAULT NULL)";

        db.execSQL(createTableFive);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL( "DROP TABLE IF EXISTS " + Table_Chat );
        db.execSQL( "DROP TABLE IF EXISTS " + Table_Chat_off );
        db.execSQL( "DROP TABLE IF EXISTS " + Table_Contacts );
        db.execSQL( "DROP TABLE IF EXISTS " + Table_Pictures );
        db.execSQL( "DROP TABLE IF EXISTS " + Table_User );
        onCreate(db);

    }



    //--------------------------DAO_CONTACTS--------------------------------------------------------


    public boolean insertContact(String deviceName,String deviceAddress, String nickName, byte[] avatar){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentvalues = new ContentValues();
        contentvalues.put(contacts_COL2,deviceName);
        contentvalues.put(contacts_COL3,deviceAddress);
        contentvalues.put(contacts_COL4,nickName);
        contentvalues.put(contacts_COL5,avatar);

        long result = db.insert(Table_Contacts, null, contentvalues);

        if (result == -1){
            return false;
        }
        else{
            return true;
        }
    }


    public void updateContact(String deviceName, String nickName, byte[] avatar){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentvalues = new ContentValues();
        contentvalues.put(contacts_COL4,nickName);
        contentvalues.put(contacts_COL5,avatar);

        String where_clause = contacts_COL2 + " = '" + deviceName + "'";

        long result = db.update(Table_Contacts, contentvalues, where_clause,null);
    }


    public void updateAvatar(String nickName, byte[] new_avatar){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(contacts_COL5,new_avatar);
        String where_clause = contacts_COL4 + " = " + nickName;
        db.update(Table_Contacts, values, where_clause,null);

    }


    public Cursor getContactInfos(String nickName){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + Table_Contacts + " WHERE "
                + contacts_COL4 + " = '" + nickName + "'";
        Cursor cursor = db.rawQuery(query,null);

        return cursor;
    }


    public Cursor getContactInfosById(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + Table_Contacts + " WHERE "
                + contacts_COL1 + " = " + id + "";
        Cursor cursor = db.rawQuery(query,null);

        return cursor;
    }


    public Cursor viewContactList(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + Table_Contacts;
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }


    public Cursor selectContact(String nickName){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + Table_Contacts + " WHERE " + contacts_COL4 + " = '" +
                nickName + "'";
        Cursor cursor = db.rawQuery(query,null);
        return cursor;
    }


    public void deleteContact(String nickName){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "DELETE  FROM " + Table_Contacts + " WHERE " + contacts_COL4 + " = '"
                + nickName + "'" ;
        db.execSQL(query);

    }


    public void updateContact(String oldNick, String newNick){

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + Table_Contacts +
                " SET " + contacts_COL4 + " = '" + newNick +
                "' WHERE " + contacts_COL4 + " = '" + oldNick + "'";
        db.execSQL(query);

    }



    //--------------------------DAO_CHAT------------------------------------------------------------


    public Cursor getChat(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT "+ chat_COL2 +", "+ chat_COL3 + " FROM " + Table_Chat + " WHERE "
                + chat_COL1 + " = " + id;
        Cursor cursor = db.rawQuery(query,null);

        return cursor;
    }

    public Cursor getChatIdByDeviceName(String deviceName){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT "+ contacts_COL1+ " FROM " + Table_Contacts + " WHERE " + contacts_COL2 + " = '" +
                deviceName + "'";
        Cursor cursor = db.rawQuery(query,null);

        return cursor;
    }

    public boolean insertChatRaw(int id, int position, String text){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentvalues = new ContentValues();
        contentvalues.put(chat_COL1,id);
        contentvalues.put(chat_COL2,position);
        contentvalues.put(chat_COL3,text);

        long result = db.insert(Table_Chat, null, contentvalues);

        if (result == -1){
            return false;
        }
        else{
            return true;
        }

    }


    //---------------  DAO_CHAT_OFF ----------------------------------------------------------------


    public Cursor getOfflineChat(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT "+ chat_off_COL2 +", "+ chat_off_COL3 + " FROM " + Table_Chat_off
                + " WHERE " + chat_off_COL1 + " = " + id;
        Cursor cursor = db.rawQuery(query,null);

        return cursor;
    }

    public boolean insertOfflineChatRaw(int id,int position, String text){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentvalues = new ContentValues();
        contentvalues.put(chat_off_COL1,id);
        contentvalues.put(chat_off_COL2,position);
        contentvalues.put(chat_off_COL3,text);

        long result = db.insert(Table_Chat_off, null, contentvalues);

        if (result == -1){
            return false;
        }
        else{
            return true;
        }

    }

    public void resetOfflineChat(){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "DELETE FROM " + Table_Chat_off;
        db.execSQL(query);


    }


    //--------------DAO_PICTURES--------------------------------------------------------------------

    public Cursor getPictures(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + Table_Pictures;
        Cursor cursor = db.rawQuery(query,null);
        return cursor;
    }

    public void insertPicture(byte[] picture){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentvalues = new ContentValues();
        contentvalues.put(pictures_COL2,picture);

        db.insert(Table_Pictures, null, contentvalues);
    }

    public Cursor getPicture(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT "+ pictures_COL2 + " FROM " + Table_Pictures + " WHERE "
                + pictures_COL1 + " = " + id;
        Cursor cursor = db.rawQuery(query,null);

        return cursor;
    }


    //------------------DAO_USER--------------------------------------------------------------------

    public Cursor getUserInfos(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + Table_User;
        Cursor cursor = db.rawQuery(query,null);

        return cursor;
    }

    public void insertUser(String nick){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentvalues = new ContentValues();
        contentvalues.put(user_COL1,nick);

        long result = db.insert(Table_User, null, contentvalues);
    }

    public void updateUserNick(String newNick){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentvalues = new ContentValues();
        contentvalues.put(user_COL1,newNick);


        long result = db.update(Table_User, contentvalues, null,null);
    }

    public void updateUserAvatar(byte[] new_avatar){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(user_COL2,new_avatar);
        db.update(Table_User, values, null,null);

    }

}
