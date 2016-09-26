package com.cybc.updatehelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.cybc.updatehelper.exceptions.UpdateNullException;
import com.cybc.updatehelper.exceptions.UpdateOrderWrongException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.LinkedList;
import java.util.List;

@RunWith(JUnit4.class)
public class UpdateHelperOrderResultTest {

    @Before         //premise
    public void testTestUpdate() {

        final int updateVersion = 5;
        Update u = createUpdate(updateVersion);
        assertNotNull(u);
        assertEquals(updateVersion, u.getUpdateVersion());
    }

    @Test
    public void failureOnSwitchedOrder() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));

        updateFactories.add(createUpdate(3));//wrong!
        updateFactories.add(createUpdate(2));//wrong!

        updateFactories.add(createUpdate(4));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
        assertNotNull(orderResult);
        assertTrue(orderResult.isWrong());
    }

    @Test
    public void failureOnEqualVersions() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));

        updateFactories.add(createUpdate(2));//wrong!
        updateFactories.add(createUpdate(2));//wrong!

        updateFactories.add(createUpdate(4));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
        assertNotNull(orderResult);
        assertTrue(orderResult.hasEqualVersions());
    }

    @Test(expected = UpdateOrderWrongException.class)
    public void failureThrowOnEqualVersions() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));

        updateFactories.add(createUpdate(2));//wrong!
        updateFactories.add(createUpdate(2));//wrong!

        updateFactories.add(createUpdate(4));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
        assertNotNull(orderResult);
        orderResult.throwIfCorrupted();
    }

    @Test(expected = UpdateOrderWrongException.class)
    public void failureThrowOnSwitchedOrder() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));

        updateFactories.add(createUpdate(3));//wrong!
        updateFactories.add(createUpdate(2));//wrong!

        updateFactories.add(createUpdate(4));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
        assertNotNull(orderResult);
        orderResult.throwIfCorrupted();
    }

    @Test(expected = UpdateNullException.class)
    public void failureThrowOnNullUpdate() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));

        updateFactories.add(createUpdate(2));
        updateFactories.add(null);//wrong!

        updateFactories.add(createUpdate(4));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
        assertNotNull(orderResult);
        orderResult.throwIfCorrupted();
    }

    @Test
    public void correctOrdered() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));
        updateFactories.add(createUpdate(2));
        updateFactories.add(createUpdate(3));
        updateFactories.add(createUpdate(4));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
        assertNotNull(orderResult);
        assertTrue(orderResult.isCorrect());
    }

    @Test
    public void correctOrderedSingle() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
        assertTrue(orderResult.isCorrect());
    }

    @Test(expected = UpdateNullException.class)
    public void failureNullSingle() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(null);

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
        assertTrue(orderResult.isCorrect());
    }

    @Test(expected = UpdateNullException.class)
    public void failureNullMulti() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));
        updateFactories.add(createUpdate(2));
        updateFactories.add(null);//wrong!
        updateFactories.add(createUpdate(4));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
        assertTrue(orderResult.isCorrect());
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
