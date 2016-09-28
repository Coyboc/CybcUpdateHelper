package com.cybc.updatehelper;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import com.cybc.updatehelper.testing.UpdateTest;
import com.cybc.updatehelper.testing.UpdateTestRunner;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class UpdateTestRunnerTest {

    /**
     * tests that:
     * - the storage isn't closed while processing updates
     * - the storage is same instance in callbacks as in creation
     * - the storage isn't null
     * - the storage get's closed at the end of the tests
     * - every updateTest provides the applicable 'real' update and that the execute on the update is called
     * - every updateTest inserts mock data and make >directly< after that an consistency check
     * - every updateTest is called in correct order
     * - every updateTest was called until the end of the test run
     *
     */
    @Test
    public void testSuccessfullUpdateTest_TestRun() {
        final int testCount = 100;
        final int startVersion = 0;

        //creating update tests
        List<UpdateTest<IntegerStorage>> updateTests = new ArrayList<>();
        for (int i = startVersion + 1; i <= testCount; i++) {
            updateTests.add(createTestUpdate(i));
        }

        //creating the "storage"
        final IntegerStorage storage = new IntegerStorage();

        UpdateTestRunner<IntegerStorage> testRunner = new UpdateTestRunner<>(new UpdateTestRunner.StorageProvider<IntegerStorage>() {
            @Override
            public void setVersionBy(Update<IntegerStorage> lastUpdate, IntegerStorage integers) {
                //TODO IMPLE ME
            }

            @Override
            public boolean isStorageClosed(IntegerStorage integers) {
                assertNotNull(integers);
                assertFalse(integers.isClosed());
                assertEquals(storage, integers);
                return false;
            }

            @Override
            public void closeStorage(IntegerStorage integers) {
                assertNotNull(integers);
                assertEquals(storage, integers);
                integers.setClosed(true);
            }
        }, updateTests);

        assertFalse(storage.isClosed());
        testRunner.runTestUpdates(storage, startVersion, testCount); //first version = 0;
        assertTrue(storage.isClosed());
        assertEquals(testCount, storage.size());

        for (int i = startVersion + 1; i <= testCount; i++) {
            assertEquals(i, storage.get(i - 1).intValue()); //correct order of every update called
        }

    }

    private UpdateTest<IntegerStorage> createTestUpdate(final int version) {
        final int testOffset = 10000;

        return new UpdateTest<IntegerStorage>() {
            @Override
            public void insertMockData(IntegerStorage integers) {
                integers.add(getMockData());

            }

            private int getMockData() {
                return testOffset + version;
            }

            @Override
            public void testConsistency(IntegerStorage integers) {
                assertTrue(integers.contains(version)); //-> check that Update.execute was called
                assertTrue(integers.contains(getMockData())); //-> insert and testConsistency called
                assertEquals(getMockData(), integers.get(integers.size() - 1).intValue()); //-> check that insertMockData was called right before
                integers.remove(integers.size() - 1); //-> finally remove it again
            }

            @Override
            public Update<IntegerStorage> getUpdateToTest() {
                return new Update<IntegerStorage>() {
                    @Override
                    public void execute(IntegerStorage integers) throws Exception {
                        assertNotNull(integers);
                        integers.add(version);
                    }

                    @Override
                    public int getUpdateVersion() {
                        return version;
                    }
                };
            }
        };
    }

}
