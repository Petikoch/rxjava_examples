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

public class SyncSplittingOddEvenExample {

    public static void main(String[] args) throws InterruptedException {

        Observable<Integer> numberSource = Observable.range(1, 5)
                .doOnNext(integer1 -> print("Generated: " + integer1));

        numberSource.groupBy(number -> isEven(number))
                .forEach(groupedObservable -> {
                    if (groupedObservable.getKey()) {
                        groupedObservable.forEach(number -> print(number + " is even"));
                    } else {
                        groupedObservable.forEach(number -> print(number + " is odd"));
                    }
                });

        print("Finished");
    }

    private static boolean isEven(int number) {
        return number % 2 == 0;
    }

    private static void print(String text) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + text);
    }
}
