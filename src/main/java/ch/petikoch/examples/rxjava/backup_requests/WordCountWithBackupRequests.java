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

		int numberOfSentences = 10_000;

		System.out.println("-----------------------------------------------------------------------");
		System.out.println("Fast... ");
		Stopwatch stopwatch = Stopwatch.createStarted();
		Observable.just(THE_SENTENCE).repeat(numberOfSentences)
				.toBlocking()
				.forEach(theSentence -> sysout("Result arrived: " + theSentence + " -> " + fastWordCount(theSentence) + " words"));
		System.out.println("Elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");

		System.out.println("-----------------------------------------------------------------------");
		System.out.println("Sometimes slow...");
		Thread.sleep(5000);
		stopwatch = Stopwatch.createStarted();
		Observable.just(THE_SENTENCE).repeat(numberOfSentences)
				.toBlocking()
				.forEach(theSentence -> sysout("Result arrived: " + theSentence + " -> " + sometimesSlowWordCount(theSentence) + " words"));
		System.out.println("Elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");

		System.out.println("-----------------------------------------------------------------------");
		System.out.println("Sometimes slow with backup requests...");
		Thread.sleep(5000);
		stopwatch = Stopwatch.createStarted();
		Observable.just(THE_SENTENCE).repeat(numberOfSentences)
				.toBlocking()
				.forEach(theSentence -> sysout("Result arrived: " + theSentence + " -> " + sometimesSlowWordCountWithBackupRequests(theSentence) + " words"));
		System.out.println("Elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
	}

	private static int fastWordCount(String sentence) {
		sysout("Counting " + sentence);
		return sentence.split("\\s+").length;
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
		return Observable.fromCallable(() -> sometimesSlowWordCount(sentence)).subscribeOn(Schedulers.io())
				.timeout(10,
						TimeUnit.MICROSECONDS,
						Observable.fromCallable(() -> sometimesSlowWordCount(sentence)).subscribeOn(Schedulers.io()))
				.toBlocking()
				.first();

		// :-):
		// - simple
		//
		// :-(
		// - what if first returns just after second launched -> that's a pitty
		// - what if second is also slow
		// - what in case of other errors
		// - what if ...
	}

	private static void sysout(String text) {
		System.out.println(new Date() + " [" + Thread.currentThread().getName() + "] " + text);
	}
}
