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
package ch.petikoch.examples.rxjava.backup_requests;

import com.google.common.base.Stopwatch;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class WordCountWithBackupRequests {

    private final static String THE_SENTENCE = "This is the magic sentence.";

    public static void main(String[] args) throws InterruptedException {

        int numberOfSentences = 5_000;

        System.out.println("-----------------------------------------------------------------------");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Observable.just(THE_SENTENCE).repeat(numberOfSentences)
                .toBlocking()
                .forEach(theSentence -> sysout("Result arrived: " + theSentence + " -> " + fastWordCount(theSentence) + " words"));
        System.out.println("Elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        System.out.println("Fast... just as warm-up.");

        System.out.println("-----------------------------------------------------------------------");
        stopwatch = Stopwatch.createStarted();
        Observable.just(THE_SENTENCE).repeat(numberOfSentences)
                .toBlocking()
                .forEach(theSentence -> sysout("Result arrived: " + theSentence + " -> " + fastWordCount(theSentence) + " words"));
        System.out.println("Elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        System.out.println("Fast, again. After warm-up our 'reference'.");

        /*
        System.out.println("-----------------------------------------------------------------------");
        Thread.sleep(5000);
        stopwatch = Stopwatch.createStarted();
        Observable.just(THE_SENTENCE).repeat(numberOfSentences)
                .toBlocking()
                .forEach(theSentence -> sysout("Result arrived: " + theSentence + " -> " + wordCountWithCachedThread(theSentence)
                        .toBlocking()
                        .first()
                        + " words"));
        System.out.println("Elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        System.out.println("Fast 2... Same as first, but executed on separate, cached thread. Just to see the overhead.");
        */

        System.out.println("-----------------------------------------------------------------------");
        Thread.sleep(5000);
        stopwatch = Stopwatch.createStarted();
        Observable.just(THE_SENTENCE).repeat(numberOfSentences)
                .toBlocking()
                .forEach(theSentence -> sysout("Result arrived: " + theSentence + " -> " + sometimesSlowWordCount(theSentence) + " words"));
        System.out.println("Elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        System.out.println("Sometimes slow... Now we have sometimes some latency. How to deal with this?");

        System.out.println("-----------------------------------------------------------------------");
        Thread.sleep(5000);
        stopwatch = Stopwatch.createStarted();
        Observable.just(THE_SENTENCE).repeat(numberOfSentences)
                .toBlocking()
                .forEach(theSentence -> sysout("Result arrived: " + theSentence + " -> " + sometimesSlowWordCountWithBackupRequests(theSentence) + " words"));
        System.out.println("Elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        System.out.println("Sometimes slow with backup requests... pretty fast, again. Backup requests work! Whew... ");
    }

    private static int fastWordCount(String sentence) {
        sysout("Counting " + sentence);
        return sentence.split("\\s+").length;
    }

    private static Observable<Integer> wordCountWithCachedThread(String sentence) {
        return Observable.fromCallable(() -> fastWordCount(sentence)).subscribeOn(Schedulers.io());
    }

    private static int sometimesSlowWordCount(String sentence) {
        if (ThreadLocalRandom.current().nextInt(100) == 1) {
            return slowWordCount(sentence);
        } else {
            return fastWordCount(sentence);
        }
    }

    private static int slowWordCount(String sentence) {
        sysout("Slooooooooooooooooooooooooooooowwwwwwwwwwwwww counting " + sentence);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            sysout("Oh man, damn, I was to slooooooooooooooooooooooooooooowwwwwwwwwwwwww and got interrupted...");
        }
        return sentence.split("\\s+").length;
    }

    private static int sometimesSlowWordCountWithBackupRequests(String sentence) {
        Observable<Integer> firstRequest = wordCountWithCachedThread(sentence);
        Observable<Integer> backupRequest = Observable.timer(10, TimeUnit.MICROSECONDS).flatMap(aLong -> wordCountWithCachedThread(sentence));
        return Observable.merge(firstRequest, backupRequest)
                .toBlocking()
                .first();

        // :-):
        // - simple
        //
        // :-(
        // - what if second is also slow
        // - what in case of other errors
        // - what if ...
    }

    private static void sysout(String text) {
        System.out.println(new Date() + " [" + Thread.currentThread().getName() + "] " + text);
    }
}
