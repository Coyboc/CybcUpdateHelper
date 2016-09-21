CybcUpdateHelper
==========

Simple Java library to make updates to a specific storage (like databases) easier. The library takes care that the updates are provided in a correct order.

E.g. it makes SqliteDatabase upgrades via a SqliteOpenHelper much easier. The User has only to prepare a ordered list of updates for the upgrade method.

Example for Android SQLiteDatabase
----------------------------------

First step
----------

Create your `Update` implementations which will perform your updates.

```java
import android.database.sqlite.SQLiteDatabase;

import com.cybc.updatehelper.Update;

public class MyUpdateVersion_1 implements Update<SQLiteDatabase> {

    @Override
    public void execute(SQLiteDatabase database) throws Exception {
        //execute our update/migration/changes to the database
        database.execSQL("UPDATE|CREATE|INSERT|ALTER ...");
    }

    @Override
    public int getUpdateVersion() {
        //Version 1
        return 1;
    }
}
```

Second step
-----------

Implement an `UpdateWorker` which provides needed information for the `UpdateHelper`. In this example the `SQLiteOpenHelper` is the `UpdateWorker`.

```java
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cybc.updatehelper.Update;
import com.cybc.updatehelper.UpdateWorker;

import java.util.Collection;

public class MyOpenHelper extends SQLiteOpenHelper implements UpdateWorker<Update<SQLiteDatabase>, SQLiteDatabase> {

    private static final String DB_NAME = "MyDatabase.db";

    private final UpdateHelper<Update<SQLiteDatabase>, SQLiteDatabase> updateHelper .... // see third step

    public MyOpenHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, factory, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {/* init your database */}

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
      //.... see third step
    }

    @Override
    public int getLatestUpdateVersion(SQLiteDatabase database) {
        //return the latest update version, must be equals the newVersion of the database
        return 5;
    }

    @Override
    public Collection<Update<SQLiteDatabase>> createUpdates() {
        //create your or return your updates here

        Set<Update<SQLiteDatabase>> updates = new LinkedHashSet<>();
        updates.add(new MyUpdateVersion_1());
        updates.add(new MyUpdateVersion_2());
        updates.add(new MyUpdateVersion_3());
        updates.add(new MyUpdateVersion_4());
        updates.add(new MyUpdateVersion_5());

        return updates;
    }

    @Override
    public void onPreUpdate(SQLiteDatabase database, Update<SQLiteDatabase> update) {
      //called before an updated gets executed
    }

    @Override
    public void onPostUpdate(SQLiteDatabase database, Update<SQLiteDatabase> update) {
      //called after an updated was successfully executed
    }

    @Override
    public void onUpgradingDone(SQLiteDatabase database) {
      //called when all updates were executed successfully
    }

    @Override
    public boolean isClosed(SQLiteDatabase database) {
        //keep sure that the database will no closed while updating
        return !database.isOpen();
    }
}
```

Third step
----------

Now use the `UpdateHelper` to trigger the update logic.

```java
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cybc.updatehelper.Update;
import com.cybc.updatehelper.UpdateWorker;

import java.util.Collection;

public class MyOpenHelper extends SQLiteOpenHelper implements UpdateWorker<Update<SQLiteDatabase>, SQLiteDatabase> {

    private static final String DB_NAME = "MyDatabase.db";

    //The UpdateHelper which executes the Updates for you
    private final UpdateHelper<Update<SQLiteDatabase>, SQLiteDatabase> updateHelper = new UpdateHelper<>(this);

    public MyOpenHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, factory, 5);
    }

    //...

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
       //triggers the creation of the updates and executes all updates step by step
       //keep sure that the given updates have the correct order
       updateHelper.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }

    //....
}
```

...Done.
-------

You can extend your list of updates for future database updates and so on.

Additional
----------

You can create your own `Update` interfaces, simply extend `Update`

```java
import android.database.sqlite.SQLiteDatabase;

import com.cybc.updatehelper.Update;

public interface MyOwnUpdateInterface extends Update<SQLiteDatabase> {

    void doSomething();

    void doAnotherThing();

}
```

After refactoring `MyOpenHelper`, you can do more stuff before or after an update was executed, just your interface implementation is used:

```java
public class MyOpenHelper extends SQLiteOpenHelper implements UpdateWorker<MyOwnUpdateInterface, SQLiteDatabase> {

    private static final String DB_NAME = "MyDatabase.db";

    private final UpdateHelper<MyOwnUpdateInterface, SQLiteDatabase> updateHelper = new UpdateHelper<>(this);

    public MyOpenHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, factory, 5);
    }

    //...

    @Override
    public Collection<MyOwnUpdateInterface> createUpdates() {
        Set<MyOwnUpdateInterface> updates = new LinkedHashSet<>();
        updates.add(new MyUpdateVersion_1());
        //...
        return updates;
    }

    @Override
    public void onPreUpdate(SQLiteDatabase database, MyOwnUpdateInterface update) {
        //call your method from your interface implementation
        update.doSomething();
    }

    @Override
    public void onPostUpdate(SQLiteDatabase database, MyOwnUpdateInterface update) {
        //call your method from your interface implementation
        update.doAnotherThing();
    }

    //...
}
```

....work in progress....