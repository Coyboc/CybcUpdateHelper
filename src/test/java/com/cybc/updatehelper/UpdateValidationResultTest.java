package com.cybc.updatehelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.cybc.updatehelper.exceptions.UpdateNullException;
import com.cybc.updatehelper.exceptions.UpdateValidationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.LinkedList;
import java.util.List;

@RunWith(JUnit4.class)
public class UpdateValidationResultTest {

    @Before         //premise
    public void testTestUpdate() {

        final int updateVersion = 5;
        Update u = createUpdate(updateVersion);
        assertNotNull(u);
        assertEquals(updateVersion, u.getUpdateVersion());
    }

    @Test(expected = UpdateNullException.class)
    public void failureOnNullUpdateCollection(){
        UpdateHelper.validateUpdates(null, 0);
    }

    @Test
    public void failureOnEmptyUpdates() {
        List<Update> updates = new LinkedList<>();
        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 0);
        assertNotNull(updateValidationResult);
        assertTrue(updateValidationResult.isEmpty());
    }

    @Test
    public void correctFinalVersion() {
        List<Update> updates = new LinkedList<>();
        updates.add(createUpdate(1));
        updates.add(createUpdate(2));
        updates.add(createUpdate(3));
        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 3);
        assertNotNull(updateValidationResult);
        assertTrue(updateValidationResult.isCorrect());
    }

    @Test(expected = UpdateValidationException.class)
    public void unexpectedFinalVersion() {
        List<Update> updates = new LinkedList<>();
        updates.add(createUpdate(1));
        updates.add(createUpdate(2));
        updates.add(createUpdate(3));
        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 10);
        assertNotNull(updateValidationResult);
        assertTrue(updateValidationResult.isUnexpectedFinalVersion());
        updateValidationResult.throwIfCorrupted();
    }

    @Test
    public void failureOnSwitchedOrder() {
        List<Update> updates = new LinkedList<>();
        updates.add(createUpdate(1));

        updates.add(createUpdate(3));//wrong!
        updates.add(createUpdate(2));//wrong!

        updates.add(createUpdate(4));

        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 4);
        assertNotNull(updateValidationResult);
        assertTrue(updateValidationResult.isWrongOrder());
    }

    @Test
    public void failureOnEqualVersions() {
        List<Update> updates = new LinkedList<>();
        updates.add(createUpdate(1));

        updates.add(createUpdate(2));//wrong!
        updates.add(createUpdate(2));//wrong!

        updates.add(createUpdate(4));

        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 4);
        assertNotNull(updateValidationResult);
        assertTrue(updateValidationResult.hasEqualVersions());
    }

    @Test(expected = UpdateValidationException.class)
    public void failureThrowOnEqualVersions() {
        List<Update> updates = new LinkedList<>();
        updates.add(createUpdate(1));

        updates.add(createUpdate(2));//wrong!
        updates.add(createUpdate(2));//wrong!

        updates.add(createUpdate(4));

        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 4);
        assertNotNull(updateValidationResult);
        updateValidationResult.throwIfCorrupted();
    }

    @Test(expected = UpdateValidationException.class)
    public void failureThrowOnSwitchedOrder() {
        List<Update> updates = new LinkedList<>();
        updates.add(createUpdate(1));

        updates.add(createUpdate(3));//wrong!
        updates.add(createUpdate(2));//wrong!

        updates.add(createUpdate(4));

        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 4);
        assertNotNull(updateValidationResult);
        updateValidationResult.throwIfCorrupted();
    }

    @Test(expected = UpdateNullException.class)
    public void failureThrowOnNullUpdate() {
        List<Update> updates = new LinkedList<>();
        updates.add(createUpdate(1));

        updates.add(createUpdate(2));
        updates.add(null);//wrong!

        updates.add(createUpdate(4));

        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 4);
        assertNotNull(updateValidationResult);
        updateValidationResult.throwIfCorrupted();
    }

    @Test
    public void correctOrdered() {
        List<Update> updates = new LinkedList<>();
        updates.add(createUpdate(1));
        updates.add(createUpdate(2));
        updates.add(createUpdate(3));
        updates.add(createUpdate(4));

        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 4);
        assertNotNull(updateValidationResult);
        assertTrue(updateValidationResult.isCorrect());
    }

    @Test
    public void correctOrderedSingle() {
        List<Update> updates = new LinkedList<>();
        updates.add(createUpdate(1));

        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 1);
        assertTrue(updateValidationResult.isCorrect());
    }

    @Test(expected = UpdateNullException.class)
    public void failureNullSingle() {
        List<Update> updates = new LinkedList<>();
        updates.add(null);

        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 0);
        assertTrue(updateValidationResult.isCorrect());
    }

    @Test(expected = UpdateNullException.class)
    public void failureNullMulti() {
        List<Update> updates = new LinkedList<>();
        updates.add(createUpdate(1));
        updates.add(createUpdate(2));
        updates.add(null);//wrong!
        updates.add(createUpdate(4));

        final UpdateHelper.UpdateValidationResult updateValidationResult = UpdateHelper.validateUpdates(updates, 4);
        assertTrue(updateValidationResult.isCorrect());
    }

    private Update createUpdate(final int version) {
        return new Update() {
            @Override
            public void execute(Object o) throws Exception {

            }

            @Override
            public int getUpdateVersion() {
                return version;
            }

        };
    }

}
