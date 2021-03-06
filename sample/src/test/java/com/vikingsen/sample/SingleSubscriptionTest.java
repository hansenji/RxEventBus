package com.vikingsen.sample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.reactivex.schedulers.Schedulers;
import pocketbus.BuildConfig;
import pocketbus.Bus;
import pocketbus.sample.BusRegistry;
import pocketbus.sample.SingleSubscription;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class SingleSubscriptionTest implements SingleSubscription.Callback {
    private int eventCount = 0;

    @Test
    public void test() throws Exception {
        Bus bus = new Bus.Builder().setCurrentScheduler(Schedulers.trampoline()).build();
        bus.setRegistry(new BusRegistry());
        Bus.setDefault(bus);
        SingleSubscription singleSubscription = new SingleSubscription(this);
        bus.post(1);
        singleSubscription.unregister();
        assertEquals(1, eventCount);
    }

    @Override
    public void assertEvent(Object event) {
        eventCount++;
    }
}