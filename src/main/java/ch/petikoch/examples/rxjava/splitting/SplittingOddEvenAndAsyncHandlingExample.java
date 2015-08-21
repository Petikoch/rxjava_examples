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
package ch.petikoch.examples.rxjava.splitting;

import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SplittingOddEvenAndAsyncHandlingExample {

    public static void main(String[] args) throws InterruptedException {

        Observable<Integer> numberSource = Observable.range(1, 10)
                .doOnNext(integer1 -> print("Generated: " + integer1));

        numberSource.groupBy(number -> isEven(number))
                .forEach(groupedObservable -> {
                    if (groupedObservable.getKey()) {
                        groupedObservable.subscribeOn(Schedulers.io()).forEach(number -> printSlow("Found slowly that " + number + " is even"));
                    } else {
                        groupedObservable.subscribeOn(Schedulers.io()).forEach(number -> printVerySlow("Found very slowly that " + number + " is odd"));
                    }
                });

        // to keep the main thread alive and avoid JVM shutdown
        TimeUnit.SECONDS.sleep(20);

        print("Finished");
    }

    private static boolean isEven(int number) {
        return number % 2 == 0;
    }

    private static void print(String text) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + text);
    }

    private static void printSlow(String text) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + text);
        try {
            Thread.sleep(500 + ThreadLocalRandom.current().nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printVerySlow(String text) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + text);
        try {
            Thread.sleep(2500 + ThreadLocalRandom.current().nextInt(3000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
