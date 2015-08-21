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
package ch.petikoch.examples.rxjava.parallel;

import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

import java.util.concurrent.ThreadLocalRandom;

public class ParallelExample {

    // https://github.com/ReactiveX/RxJava/issues/1673
    public static void main(String[] args) throws InterruptedException {

        Observable<Integer> numberSource = Observable.range(1, 5)
                .doOnNext(integer1 -> print("Generated: " + integer1));

        Observable<Integer> mergedResults = numberSource.flatMap(
                aNumber -> createSlowlyBlockingPrintObservable(aNumber).subscribeOn(Schedulers.io())
        );

        mergedResults.observeOn(Schedulers.io()).subscribe(aNumber -> print("Finished: " + aNumber));

        // to keep the main thread alive and avoid JVM shutdown
        Thread.sleep(10000);
    }

    private static Observable<Integer> createSlowlyBlockingPrintObservable(Integer item) {
        return Single.<Integer>create(subscriber -> {
            printSlow("Printing " + item + " slow.............. ");
            print("..............ly: " + item + " printed.");
            if (!subscriber.isUnsubscribed()) {
                subscriber.onSuccess(item);
            }
        }).toObservable();
    }

    private static void printSlow(String text) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + text);
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(2000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void print(String text) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + text);
    }
}
