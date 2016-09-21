package com.cybc.updatehelper;

import static org.junit.Assert.assertTrue;

import com.cybc.updatehelper.exceptions.UpdateNullException;
import com.cybc.updatehelper.exceptions.UpdateOrderWrongException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.LinkedList;
import java.util.List;

@RunWith(JUnit4.class)
public class UpdateHelperOrderResultTest {

    @Test
    public void failureWrongOrdered() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));

        updateFactories.add(createUpdate(3));//wrong!
        updateFactories.add(createUpdate(2));//wrong!

        updateFactories.add(createUpdate(4));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
        assertTrue(orderResult.isWrong());
    }

    @Test
    public void failureEqualVersions() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));

        updateFactories.add(createUpdate(2));//wrong!
        updateFactories.add(createUpdate(2));//wrong!

        updateFactories.add(createUpdate(4));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
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
        orderResult.throwIfCorrupted();
    }

    @Test(expected = UpdateOrderWrongException.class)
    public void failureThrowOnWrongOrdered() {
        List<Update> updateFactories = new LinkedList<>();
        updateFactories.add(createUpdate(1));

        updateFactories.add(createUpdate(3));//wrong!
        updateFactories.add(createUpdate(2));//wrong!

        updateFactories.add(createUpdate(4));

        final UpdateHelper.OrderResult orderResult = UpdateHelper.createOrderResultOf(updateFactories);
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
