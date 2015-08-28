/**
 * Copyright 2015 Peti Koch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.petikoch.examples.rxjava.threading;

import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class InfiniteLaneMcDrive {

    private static final Clock clock = new Clock(Time.EIGTH_AM);

    public static void main(String[] args) throws InterruptedException {

        Observable<String> customers = Observable.just("Fred", "John", "Maria", "Ben", "Ken", "Greg", "Peter", "Henry").repeat();
        Observable<Long> arrivals = Observable.interval(0, 1, TimeUnit.SECONDS);

        Observable<CustomerWithArrivalTime> customerArriveStream =
                Observable.zip(
                        customers,
                        arrivals,
                        (customer, eventNumber) -> new CustomerWithArrivalTime(customer + eventNumber, clock.getTime()))
                        .doOnNext(customer -> sysout(customer.name + " arrived"));

        AtomicInteger numerOfConcurrentHandledCustomers = new AtomicInteger(0);

        Observable<CustomerWithArrivalTime> orderFinishedStream = customerArriveStream.subscribeOn(Schedulers.io()).flatMap(
                customerWithArrivalTime -> {

                    sysout(numerOfConcurrentHandledCustomers.incrementAndGet() + " concurrent customer(s)");

                    Single<String> mac = Single.<String>create(singleSubscriber -> {
                        sysout("Starting with mac for " + customerWithArrivalTime);
                        simulateWork(12);
                        sysout("Mac ready for " + customerWithArrivalTime);
                        singleSubscriber.onSuccess("mac");
                    }).subscribeOn(Schedulers.io());

                    Single<String> fries = Single.<String>create(singleSubscriber -> {
                        sysout("Starting with fries for " + customerWithArrivalTime);
                        simulateWork(8);
                        sysout("Fries ready for " + customerWithArrivalTime);
                        singleSubscriber.onSuccess("fries");
                    }).subscribeOn(Schedulers.io());

                    Single<String> coke = Single.<String>create(singleSubscriber -> {
                        sysout("Starting with coke for " + customerWithArrivalTime);
                        simulateWork(2);
                        sysout("Coke ready for " + customerWithArrivalTime);
                        singleSubscriber.onSuccess("coke");
                    }).subscribeOn(Schedulers.io());

                    Single<CustomerWithArrivalTime> finishedOrder = Single.zip(mac, fries, coke, (s, s2, s3) -> {
                        sysout(numerOfConcurrentHandledCustomers.decrementAndGet() + " concurrent customer(s)");
                        return customerWithArrivalTime;
                    });
                    return finishedOrder.toObservable();
                },
                Integer.MAX_VALUE // = "infinite" lanes
        );

        orderFinishedStream.subscribe(customerWithArrivalTime -> {
            int timeDifference = clock.getTime().difference(customerWithArrivalTime.arrivalTime);
            sysout(customerWithArrivalTime.name + " order finished after " + timeDifference + " minutes");
            sysout(customerWithArrivalTime.name + " says good bye and leaves.");
        });

        Thread.sleep(60000);
    }

    private static void sysout(String text) {
        System.out.println(clock.getTime() + " [" + Thread.currentThread().getName() + "] " + text);
    }

    private static void simulateWork(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class CustomerWithArrivalTime {
        String name;
        Time arrivalTime;

        CustomerWithArrivalTime(String name, Time arrivalTime) {
            this.name = name;
            this.arrivalTime = arrivalTime;
        }

        @Override
        public String toString() {
            return name + " (arrived at " + arrivalTime + ")";
        }
    }

    static class Clock {

        private final AtomicInteger internalTimeInMinutes = new AtomicInteger();

        Clock(Time initialTime) {
            internalTimeInMinutes.set(initialTime.internalTimeInMinutes);
            Observable.interval(1, TimeUnit.SECONDS).subscribe(aLong -> {
                internalTimeInMinutes.incrementAndGet();
                System.out.println(getTime());
            });
        }

        public boolean isBefore(Time time) {
            return internalTimeInMinutes.get() < time.internalTimeInMinutes;
        }

        public Time getTime() {
            return new Time(internalTimeInMinutes.get());
        }

    }

    static class Time {

        static final Time EIGTH_AM = new Time(8 * 60);
        static final Time EIGTH_TWO_AM = new Time(8 * 60 + 2);
        static final Time NINE_AM = new Time(9 * 60);
        static final Time FIVE_PM = new Time(17 * 60);

        private final int internalTimeInMinutes;

        Time(int internalTimeInMinutes) {
            this.internalTimeInMinutes = internalTimeInMinutes;
        }

        private static String leftPadded2DigitsString(int number, int maxNumber) {
            if (number < 10) {
                return "0" + number;
            } else if (number >= 10 && number <= maxNumber) {
                return Integer.toString(number);
            } else {
                throw new IllegalStateException("Unhandled: " + number);
            }
        }

        int difference(Time laterOther) {
            int result = laterOther.internalTimeInMinutes - internalTimeInMinutes;
            if (result < 0) {
                result = result * -1;
            }
            return result;
        }

        @Override
        public String toString() {
            return leftPadded2DigitsString(internalTimeInMinutes / 60, 23) + ":" + leftPadded2DigitsString(internalTimeInMinutes % 60, 59);
        }
    }
}
