package com.cybc.updatehelper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import com.cybc.updatehelper.exceptions.UpdateFailedException;
import com.cybc.updatehelper.exceptions.UpdateNullException;
import com.cybc.updatehelper.exceptions.UpdateValidationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(JUnit4.class)
public class UpdateHelperTest {

    private boolean enableLogging = false;

    /*
    This TestClass uses a simple integer list as the storage 'to update'.
     */

    @Test
    public void createUpdates() {
        final int updateSize = 5;
        List<Update<IntegerStorage>> updates = new ArrayList<>();
        for (int i = 0; i < updateSize; i++) {
            updates.add(createUpdate(i, false));
        }
        Update last = null;
        for (Update u : updates) {
            if (last != null) {
                assertEquals(last.getUpdateVersion() + 1, u.getUpdateVersion());
            } else {
                assertEquals(0, u.getUpdateVersion());
            }
            last = u;
        }
    }

    /**
     * Tests:
     * - persistent storage reference through callbacks,
     * - callbacks are called on every update and in correct order pre -> post (+pre is called as often as post),
     * - updates actually get executed and do change the storage,
     * - updates are executed in correct order,
     * - final storage object in #onUpgradeDone contains all updates
     */
    @Test
    public void testSuccessfulUpdate() {
        final int updateSize = 100;
        final IntegerStorage methodLocalStorage = new IntegerStorage();
        UpdateHelper<Update<IntegerStorage>, IntegerStorage> updateHelper = new UpdateHelper<>(new UpdateWorker<Update<IntegerStorage>, IntegerStorage>() {
            int onPreUpdateCalled = 0;
            int onPostUpdateCalled = 0;
            Update<IntegerStorage> lastUpdate;

            @Override
            public int getLatestUpdateVersion(IntegerStorage intStorage) {
                assertNotNull(intStorage);
                assertEquals(methodLocalStorage, intStorage);
                return updateSize - 1;
            }

            @Override
            public Collection<Update<IntegerStorage>> createUpdates() {
                List<Update<IntegerStorage>> updates = new ArrayList<>();
                for (int i = 0; i < updateSize; i++) {
                    updates.add(createUpdate(i, false));
                }
                Update last = null;
                for (Update u : updates) {
                    if (last != null) {
                        assertEquals(last.getUpdateVersion() + 1, u.getUpdateVersion());
                    } else {
                        assertEquals(0, u.getUpdateVersion());
                    }
                    last = u;
                }
                return updates;
            }

            @Override
            public void onPreUpdate(IntegerStorage storage, Update<IntegerStorage> update) {
                log("onPreUpdate");
                assertNotNull(storage);
                assertNotNull(update);
                assertEquals(methodLocalStorage, storage);

                assertEquals(onPreUpdateCalled, storage.size());
                assertEquals(onPreUpdateCalled, update.getUpdateVersion());

                onPreUpdateCalled++;
                if (lastUpdate != null) {
                    assertEquals(lastUpdate.getUpdateVersion() + 1, update.getUpdateVersion());
                }
                lastUpdate = update;
            }

            @Override
            public void onPostUpdate(IntegerStorage storage, Update<IntegerStorage> update) {
                log("onPostUpdate");
                assertNotNull(storage);
                assertNotNull(update);
                assertEquals(methodLocalStorage, storage);

                assertEquals(update.getUpdateVersion(), storage.size() - 1);

                onPostUpdateCalled++;
                assertEquals(onPreUpdateCalled, onPostUpdateCalled);
                assertEquals(lastUpdate, update);
            }

            @Override
            public void onUpgradingDone(IntegerStorage storage) {
                assertNotNull(storage);
                assertEquals(createUpdates().size(), storage.size());

                assertEquals(onPreUpdateCalled, onPostUpdateCalled);
                assertEquals(updateSize, storage.size());

                for (int i = 0; i < updateSize; i++) {
                    assertEquals(i, storage.get(i).intValue()); //ensure that every update was executed in correct order
                }
            }

            @Override
            public boolean isStorageClosed(IntegerStorage integers) {
                assertEquals(methodLocalStorage, integers);
                return integers.isClosed();
            }
        });
        updateHelper.onUpgrade(methodLocalStorage, -1, updateSize - 1);
    }

    @Test(expected = UpdateNullException.class)
    public void testNullUpdates() {
        List<Update<IntegerStorage>> updates = new ArrayList<>();
        updates.add(createUpdate(1, false));
        updates.add(null);
        TestUpdateWorker worker = new TestUpdateWorker(2, updates);
        new UpdateHelper<>(worker).onUpgrade(new IntegerStorage(), 0, 2);
    }

    @Test(expected = UpdateFailedException.class)
    public void testWrongLatestVersionUpdates() {
        List<Update<IntegerStorage>> updates = new ArrayList<>();
        updates.add(createUpdate(1, false));
        TestUpdateWorker worker = new TestUpdateWorker(546567, updates);
        new UpdateHelper<>(worker).onUpgrade(new IntegerStorage(), 0, 2);
    }

    @Test(expected = UpdateFailedException.class)
    public void testWrongUpdateCount() {
        List<Update<IntegerStorage>> updates = new ArrayList<>();
        updates.add(createUpdate(1, false));
        updates.add(createUpdate(2, false));
        updates.add(createUpdate(3, false));

        int lastUpdateVersion = 3;

        TestUpdateWorker worker = new TestUpdateWorker(lastUpdateVersion, updates); //but 10 is latest version
        new UpdateHelper<>(worker).onUpgrade(new IntegerStorage(), 0, 10);
    }

    @Test(expected = UpdateValidationException.class)
    public void testWrongUpdateOrder() {
        List<Update<IntegerStorage>> updates = new ArrayList<>();
        updates.add(createUpdate(1, false));
        updates.add(createUpdate(3, false));
        updates.add(createUpdate(2, false));
        TestUpdateWorker worker = new TestUpdateWorker(3, updates);
        new UpdateHelper<>(worker).onUpgrade(new IntegerStorage(), 0, 3);
    }

    @Test(expected = UpdateValidationException.class)
    public void testEqualUpdateVersion() {
        List<Update<IntegerStorage>> updates = new ArrayList<>();
        updates.add(createUpdate(1, false));
        updates.add(createUpdate(1, false));
        updates.add(createUpdate(1, false));
        TestUpdateWorker worker = new TestUpdateWorker(1, updates);
        new UpdateHelper<>(worker).onUpgrade(new IntegerStorage(), 0, 1);
    }

    @Test(expected = UpdateFailedException.class)
    public void testStorageClosedAfterUpdate() {
        List<Update<IntegerStorage>> updates = new ArrayList<>();
        updates.add(createUpdate(1, false));
        updates.add(createUpdate(2, false));
        updates.add(createUpdate(3, false));
        updates.add(createUpdate(4, true));
        updates.add(createUpdate(5, false));
        TestUpdateWorker worker = new TestUpdateWorker(2, updates);
        new UpdateHelper<>(worker).onUpgrade(new IntegerStorage(), 0, 5);
    }

    private Update<IntegerStorage> createUpdate(final int version, final boolean closeStorageAfterUpdate) {
        return new Update<IntegerStorage>() {
            @Override
            public int getUpdateVersion() {
                return version;
            }

            @Override
            public void execute(IntegerStorage array) throws Exception {
                array.add(version);
                if (closeStorageAfterUpdate) {
                    array.setClosed(true);
                }
            }
        };
    }

    private class TestUpdateWorker implements UpdateWorker<Update<IntegerStorage>, IntegerStorage> {

        private final int                                latestVersion;
        private final Collection<Update<IntegerStorage>> updates;

        private TestUpdateWorker(final int latestVersion, Collection<Update<IntegerStorage>> updates) {
            this.latestVersion = latestVersion;
            this.updates = updates;
        }

        @Override
        public int getLatestUpdateVersion(IntegerStorage integers) {
            return latestVersion;
        }

        @Override
        public Collection<Update<IntegerStorage>> createUpdates() {
            return updates;
        }

        @Override
        public void onPreUpdate(IntegerStorage integers, Update<IntegerStorage> update) {}

        @Override
        public void onPostUpdate(IntegerStorage integers, Update<IntegerStorage> update) {}

        @Override
        public void onUpgradingDone(IntegerStorage integers) {}

        @Override
        public boolean isStorageClosed(IntegerStorage integers) {
            return integers.isClosed();
        }
    }

    private void log(String msg) {
        if (enableLogging) {
            System.out.println(msg);
        }
    }

}
