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

public class MyUpdateVersion_2 implements Update<SQLiteDatabase> {

    @Override
    public void execute(SQLiteDatabase database) throws Exception {
        //execute our update/migration/changes to the database
        database.execSQL("UPDATE|CREATE|INSERT|ALTER ...");
    }

    @Override
    public int getUpdateVersion() {
        //Version 2
        return 2;
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
        super(context, DB_NAME, factory, 6);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {/* init your version 1 database */}

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
      //.... see third step
    }

    @Override
    public int getLatestUpdateVersion(SQLiteDatabase database) {
        //return the latest update version, must be equals the newVersion of the database
        return 6;
    }

    @Override
    public Collection<Update<SQLiteDatabase>> createUpdates() {
        //create your or return your updates here

        Set<Update<SQLiteDatabase>> updates = new LinkedHashSet<>();
        updates.add(new MyUpdateVersion_2());
        updates.add(new MyUpdateVersion_3());
        updates.add(new MyUpdateVersion_4());
        updates.add(new MyUpdateVersion_5());
        updates.add(new MyUpdateVersion_6());

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
        super(context, DB_NAME, factory, 6);
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

Now you can extend your list of updates for future database updates and so on.

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
        super(context, DB_NAME, factory, 6);
    }

    //...

    @Override
    public Collection<MyOwnUpdateInterface> createUpdates() {
        Set<MyOwnUpdateInterface> updates = new LinkedHashSet<>();
        updates.add(new MyUpdateVersion_2());
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

Testing
-------

I used for this example the library `robolectric` (http://robolectric.org/getting-started/) to have a suitable way to test Android SQLiteDatabases via JUnit tests.

Example for Android SQLiteDatabase
----------------------------------

First step
----------

Create your `UpdateTest`s which wraps your normal production updates and executes your tests.

```java
import android.database.sqlite.SQLiteDatabase;

import com.cybc.updatehelper.Update;
import com.cybc.updatehelper.testing.UpdateTest;
import com.cybc.updatehelper.testing.UpdateTestExecutor;

import com.example.updates.MyUpdateVersion_1;

public class MyTestUpdateVersion_1 implements UpdateTest<SQLiteDatabase> {

    @Override
    public Update<SQLiteDatabase> getUpdateToTest() {
        return new MyUpdateVersion_1();
    }

    @Override
    public void insertMockData(SQLiteDatabase storageToUpdate) {
        //insert your test data here
        database.execSQL("INSERT ...");
    }

    @Override
    public void testConsistency(SQLiteDatabase storageToUpdate) {
        //test your database changes here together with your mock data
        assertTrue(1 == 1);
    }
}

Second step
----------

Prepare a SQLiteOpenHelper which integrates your tests. Use here an `UpdateTestRunner` to trigger the updates and the following tests to them.
The example shows how you could create an empty database with your very first version schema
and re-using the class to trigger the updates with the prepared first version database.

```java
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyTestDatabaseHelper extends SQLiteOpenHelper implements UpdateTestRunner.StorageProvider<SQLiteDatabase> {

    private final UpdateTestRunner<SQLiteDatabase> updateTestRunner;

    public static SQLiteDatabase createFirstVersionDatabase(Context context, String name){
        MyTestDatabaseHelper helper = new MyTestDatabaseHelper(context, name, 1);
        return helper.getReadableDatabase();//calls onCreate
    }

    private MyTestDatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
        this.updateTestRunner = null;
    }

    public MyTestDatabaseHelper(Context context, String name) {
        super(context, name, null, 6 /*Your current target version for your database*/);
        this.updateTestRunner = new UpdateTestRunner<>(this, createUpdates());
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //init your very first database schema here
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // when opening a existing database, run your updates here
        updateTestRunner.runTestUpdates(db, oldVersion, newVersion);
    }

    private Collection<UpdateTest<SQLiteDatabase>> createUpdates() {
        //create your test updates here
        List<UpdateTest<SQLiteDatabase>> updates = new ArrayList<>();
        updates.add(new MyTestUpdateVersion_2());
        updates.add(new MyTestUpdateVersion_3());
        updates.add(new MyTestUpdateVersion_4());
        updates.add(new MyTestUpdateVersion_5());
        updates.add(new MyTestUpdateVersion_6());

        return updateTests;
    }

    @Override
    public boolean isStorageClosed(SQLiteDatabase sqLiteDatabase) {
        return !sqLiteDatabase.isOpen();
    }
}

```

Third step
-----------

Now you can start your tests like this:

```java
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DatabaseTest {

    @Test
    public void startDatabaseTests() {
        final Context context = Shadows.shadowOf(RuntimeEnvironment.application).getApplicationContext();

        //your database name for the tests
        final String databaseName = "Test_MyDatabase.db";

        //ensure your database file does not exists, to create a real new database
        File file = new File(databaseName);
        if (file.exists()) {
            assertTrue("Delete previous test file.", file.delete());
        }

        //prepare the first version database
        SQLiteDatabase firstVersionDatabase = MyTestDatabaseHelper.createFirstVersionDatabase(context, databaseName);

        //make several tests that ensures your first version database is correct initialized
        assertEquals(firstVersionDatabase.getVersion(), 1);

        firstVersionDatabase.close();//close the database to free the file for the update tests
        assertFalse(firstVersionDatabase.isOpen());

        //Now prepare your test updater here
        final MyTestDatabaseHelper helperTest = new MyTestDatabaseHelper(context, databaseName);

        //triggers onUpgrade and starts the actual update + tests
        final SQLiteDatabase databaseToTest = helperTest.getReadableDatabase();

        //make several tests with the updated database
        assertEquals(databaseToTest.getVersion(), 6);

        databaseToTest.close();
        assertFalse(databaseToTest.isOpen());
    }

}
```