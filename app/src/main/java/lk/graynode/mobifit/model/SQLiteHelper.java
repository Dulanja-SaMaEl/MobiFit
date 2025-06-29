package lk.graynode.mobifit.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE products (\n" +
                "    id           TEXT PRIMARY KEY \n" +
                "                         NOT NULL,\n" +
                "    title        TEXT    NOT NULL,\n" +
                "   description      TEXT    NOT NULL,\n" +
                "   price      TEXT    NOT NULL,\n" +
                "   qty      TEXT    NOT NULL,\n" +
                "    url TEXT    NOT NULL\n" +
                ");\n");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS products");
        onCreate(db);
}
}
