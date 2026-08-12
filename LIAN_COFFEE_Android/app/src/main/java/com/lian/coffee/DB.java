package com.lian.coffee;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;

public class DB extends SQLiteOpenHelper {
    public DB(Context c) { super(c, "lian.db", null, 1); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT, title TEXT, amount REAL, date TEXT)");
        db.execSQL("CREATE TABLE inventory(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, qty REAL, min_qty REAL, buy_price REAL, sell_price REAL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void addTransaction(String type, String title, double amount, String date) {
        ContentValues v = new ContentValues();
        v.put("type", type); v.put("title", title); v.put("amount", amount); v.put("date", date);
        getWritableDatabase().insert("transactions", null, v);
    }

    public void addProduct(String name, double qty, double minQty, double buy, double sell) {
        ContentValues v = new ContentValues();
        v.put("name", name); v.put("qty", qty); v.put("min_qty", minQty);
        v.put("buy_price", buy); v.put("sell_price", sell);
        getWritableDatabase().insert("inventory", null, v);
    }

    public double total(String type, String date) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type=? AND date=?",
            new String[]{type, date});
        double x = 0; if(c.moveToFirst()) x = c.getDouble(0); c.close(); return x;
    }

    public Cursor inventory() {
        return getReadableDatabase().rawQuery("SELECT id,name,qty,min_qty,buy_price,sell_price FROM inventory ORDER BY name", null);
    }

    public Cursor transactions() {
        return getReadableDatabase().rawQuery("SELECT type,title,amount,date FROM transactions ORDER BY id DESC", null);
    }
}
