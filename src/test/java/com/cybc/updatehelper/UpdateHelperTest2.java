package com.cybc.updatehelper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(JUnit4.class)
public class UpdateHelperTest2 {

    /*
    This Test uses a simple integer list as the storage 'to update'.
     */

    @Test
    public void createUpdates() {
        final int updateSize = 5;
        List<Update<IntegerStorage>> updates = new ArrayList<>();
        for (int i = 0; i < updateSize; i++) {
            updates.add(createUpdate(i));
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

    @Test
    public void testSuccessfulUpdate() {
        final int updateSize = 5;
        final IntegerStorage storage = new IntegerStorage();
        UpdateHelper<Update<IntegerStorage>, IntegerStorage> updateHelper = new UpdateHelper<>(new UpdateWorker<Update<IntegerStorage>, IntegerStorage>() {
            int onPreUpdateCalled = 0;
            int onPostUpdateCalled = 0;
            Update<IntegerStorage> lastUpdate;

            @Override
            public int getLatestUpdateVersion(IntegerStorage integers) {
                assertNotNull(integers);
                assertEquals(storage, integers);
                return updateSize;
            }

            @Override
            public Collection<Update<IntegerStorage>> createUpdates() {
                List<Update<IntegerStorage>> updates = new ArrayList<>();
                for (int i = 0; i < updateSize; i++) {
                    updates.add(createUpdate(i));
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
            public void onPreUpdate(IntegerStorage integers, Update<IntegerStorage> update) {
                log("onPreUpdate");
                assertNotNull(integers);
                assertNotNull(update);

                assertEquals(onPreUpdateCalled, integers.size());
                assertEquals(onPreUpdateCalled, update.getUpdateVersion());

                onPreUpdateCalled++;
                lastUpdate = update;
            }

            @Override
            public void onPostUpdate(IntegerStorage integers, Update<IntegerStorage> update) {
                log("onPostUpdate");
                assertNotNull(integers);
                assertNotNull(update);
                assertEquals(update.getUpdateVersion(), integers.size() - 1);

                onPostUpdateCalled++;
                assertEquals(onPreUpdateCalled, onPostUpdateCalled);
                assertEquals(lastUpdate, update);
            }

            @Override
            public void onUpgradingDone(IntegerStorage integers) {
                assertNotNull(integers);
                assertEquals(createUpdates().size(), integers.size());

                assertEquals(onPreUpdateCalled, onPostUpdateCalled);
            }

            @Override
            public boolean isStorageClosed(IntegerStorage integers) {
                assertEquals(storage, integers);
                return false;
            }
        });
        updateHelper.onUpgrade(storage, -1, updateSize);
    }

    private Update<IntegerStorage> createUpdate(final int version) {
        return new Update<IntegerStorage>() {
            @Override
            public int getUpdateVersion() {
                return version;
            }

            @Override
            public void execute(IntegerStorage array) throws Exception {
                array.add(version);
            }
        };
    }

    private class IntegerStorage extends ArrayList<Integer> {
        //much wow, storage in ram -> fast
    }

    private void log(String msg) {
        System.out.println(msg);
    }

}
