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
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadedByDefault {

    private static final Clock clock = new Clock(Time.EIGTH_AM);

    public static void main(String[] args) {
        Observable<String> customers = Observable.just("Fred", "John", "Maria", "Ben", "Ken", "Greg", "Peter", "Henry").repeat();
        Observable<Long> arrivals = Observable.interval(0, 1, TimeUnit.SECONDS);

        Observable<String> customerArriveStream =
                Observable.zip(
                        customers,
                        arrivals,
                        (customer, eventNumber) -> customer)
                        .doOnNext(customer -> sysout(customer + " arrived"));

        customerArriveStream.observeOn(Schedulers.io()).subscribe(s -> {
            sysout("-----> Serving " + s);
            waitSeconds(3);
        });

        waitSeconds(60);
    }

    private static void waitSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void sysout(String text) {
        System.out.println(clock.getTime() + " [" + Thread.currentThread().getName() + "] " + text);
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
