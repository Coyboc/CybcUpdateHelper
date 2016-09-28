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

I used for this example `testCompile 'org.robolectric:robolectric:3.1.2'` to have a suitable way to test Android SqliteDatabase via JUnit tests.

Example for Android SQLiteDatabase
----------------------------------

First step
----------

Create your `UpdateTest`s which wraps your normal production updates.

```java
import android.database.sqlite.SQLiteDatabase;

import com.cybc.updatehelper.Update;
import com.cybc.updatehelper.testing.UpdateTest;
import com.cybc.updatehelper.testing.UpdateTestExecutor;

import com.example.updates.MyUpdateVersion_1;

public class MyTestUpdateVersion_1 implements UpdateTest<SQLiteDatabase> {

    private Update<SQLiteDatabase> update = new MyUpdateVersion_1();

    @Override
    public UpdateTestExecutor<SQLiteDatabase> createTestExecutor() {
        return ... //see second step
    }

    @Override
    public void execute(SQLiteDatabase database) throws Exception {
        update.execute(database);
    }

    @Override
    public int getUpdateVersion() {
        return update.getUpdateVersion();
    }
}
```

Second step
-----------

Create an `UpdateTestExecutor` which can insert mock data into your database and test for you.

```java
import static org.junit.Assert.assertTrue;

//...

import com.example.updates.MyUpdateVersion_1;

public class MyTestUpdateVersion_1 implements UpdateTest<SQLiteDatabase> {

    //...

    @Override
    public UpdateTestExecutor<SQLiteDatabase> createTestExecutor() {
        //return an update test executor for the actual test
        return new UpdateTestExecutor<SQLiteDatabase>() {
            @Override
            public void insertMockData(SQLiteDatabase database) {
                //insert your test data here
                database.execSQL("INSERT ...");
            }

            @Override
            public void testConsistency(SQLiteDatabase database) {
                //test your database changes here together with your mock data
                assertTrue(1 == 1);
            }
        };
    }
    //...
}
```

Third step
----------

Prepare a database with your very first version of your database schema.
The resulting SQLiteDatabase File will be re-used to trigger your updates.

```java
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyFirstVersionDatabase extends SQLiteOpenHelper {

    public MyFirstVersionDatabase(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //create your fist version schema here
        database.execSQL("CREATE|INSERT|....");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //empty, this helper is not intended to make upgrades
    }
}
```

Fourth step
-----------

Create an `UpdateWorker` which performs like the production `UpdateWorker`,
but instead of creating production updates, it creates `UpdateTest`s which wraps your actual production updates.

```java
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cybc.updatehelper.UpdateWorker;
import com.cybc.updatehelper.testing.UpdateTest;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class MySqliteOpenHelperTest extends SQLiteOpenHelper implements UpdateWorker<UpdateTest<SQLiteDatabase>, SQLiteDatabase>{

    private final UpdateTester<UpdateTest<SQLiteDatabase>, SQLiteDatabase> updateTester = ... // see fifth step

    public MySqliteOpenHelperTest(Context context, String name) {
        super(context, name, null, 6);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //empty, this database is not intended to create a database schema
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        ... //see fifth step
    }

    @Override
    public int getLatestUpdateVersion(SQLiteDatabase database) {
        return 6;
    }

    @Override
    public Collection<UpdateTest<SQLiteDatabase>> createUpdates() {
        //create your UpdateTests here
        Set<UpdateTest<SQLiteDatabase>> updates = new LinkedHashSet<>();
        updates.add(new MyTestUpdateVersion_2());
        updates.add(new MyTestUpdateVersion_3());
        updates.add(new MyTestUpdateVersion_4());
        updates.add(new MyTestUpdateVersion_5());
        updates.add(new MyTestUpdateVersion_6());

        return updates;
    }

    @Override
    public void onPreUpdate(SQLiteDatabase database, UpdateTest<SQLiteDatabase> update) {
      //executed before a test update
    }

    @Override
    public void onPostUpdate(SQLiteDatabase database, UpdateTest<SQLiteDatabase> update) {
      //executed after a test update
    }

    @Override
    public void onUpgradingDone(SQLiteDatabase database) {
      //executed after all test updates
    }

    @Override
    public boolean isClosed(SQLiteDatabase database) {
        return !database.isOpen();
    }
}
```

Fifth step
-----------

Now create an `UpdateTester` which executes your updates, inserts your mock-data and starts the tests.

```java
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cybc.updatehelper.UpdateWorker;
import com.cybc.updatehelper.testing.UpdateTest;
import com.cybc.updatehelper.testing.UpdateTestRunner;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class MySqliteOpenHelperTest extends SQLiteOpenHelper implements UpdateWorker<UpdateTest<SQLiteDatabase>, SQLiteDatabase> {

    // the update tester which executes the updates, inserts the mock-data and tests the resulting database
    private final UpdateTester<UpdateTest<SQLiteDatabase>, SQLiteDatabase> updateTester = new UpdateTester<>(this);

    public MySqliteOpenHelperTest(Context context, String name) {
        super(context, name, null, 6);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //like in production, trigger the updates here
        updateTester.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }

  //...
}
```

Sixth step
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

        //prepare the first version database
        final MyFirstVersionDatabase firstVersionOpenHelperTest = new MyFirstVersionDatabase(context, databaseName);

        //trigger the onCreate method for the database, initializes the first version schema
        SQLiteDatabase firstVersionDatabase = firstVersionOpenHelperTest.getReadableDatabase();

        //make several tests that ensures your first version database is correct initialized
        assertEquals(firstVersionDatabase.getVersion(), 1);

        firstVersionDatabase.close();//close the database to free the file for the update tests
        assertFalse(firstVersionDatabase.isOpen());

        //Now prepare your test updater here
        final MySqliteOpenHelperTest helperTest = new MySqliteOpenHelperTest(context, databaseName);

        //triggers onUpgrade and starts the actual update + tests
        final SQLiteDatabase databaseToTest = helperTest.getReadableDatabase();

        //make several tests with the updated database
        assertEquals(databaseToTest.getVersion(), 6);

        databaseToTest.close();
        assertFalse(databaseToTest.isOpen());
    }

}
```

....work in progress....