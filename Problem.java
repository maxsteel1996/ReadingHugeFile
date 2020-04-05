import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Problem {
	final static int LINE_LIMIT = 50;

	final static int THREAD_LIMIT = 10;
	final static int TOP = 5;

	public static void main(String[] args) throws IOException {
		URL u = new URL("resource_url");
		Scanner sc = new Scanner(u.openStream(), "UTF-8");
		Map<String, Integer> overAllWordCount = new ConcurrentHashMap<String, Integer>();
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_LIMIT);
		int partCount = 1;
		List<String> lines = new ArrayList<String>();
		while (sc.hasNext()) {
			String line = sc.next();
			if (line != "") {
				lines.add(line);
			}
			if (lines.size() >= LINE_LIMIT) {
				Map<String, Integer> wordsCountOfCurrentPart = new ConcurrentHashMap<String, Integer>();
				executor.submit(new ReadThread(partCount, lines, wordsCountOfCurrentPart, overAllWordCount));
				lines = new ArrayList<String>();
				partCount++;
			}
		}
		executor.shutdown();
		try {
			executor.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("*********** final ***********");
		for (Entry<String, Integer> entry : overAllWordCount.entrySet()) {
			System.out.println(entry);
		}
		System.out.println("**********************");
		List<Entry<String, Integer>> greatest = findGreatest(overAllWordCount, TOP);
		System.out.println("Top " + TOP + " entries:");
		for (Entry<String, Integer> entry : greatest) {
			System.out.println(entry);
		}
		findGreatest(overAllWordCount, TOP);
	}

	private static <K, V extends Comparable<? super V>> List<Entry<K, V>> findGreatest(Map<K, V> map, int n) {
		Comparator<? super Entry<K, V>> comparator = new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e0, Entry<K, V> e1) {
				V v0 = e0.getValue();
				V v1 = e1.getValue();
				return v0.compareTo(v1);
			}
		};
		PriorityQueue<Entry<K, V>> highest = new PriorityQueue<Entry<K, V>>(n, comparator);
		for (Entry<K, V> entry : map.entrySet()) {
			highest.offer(entry);
			while (highest.size() > n) {
				highest.poll();
			}
		}

		List<Entry<K, V>> result = new ArrayList<Map.Entry<K, V>>();
		while (highest.size() > 0) {
			result.add(highest.poll());
		}
		return result;
	}
}

class ReadThread extends Thread {

	public ReadThread(int i, List<String> lines, Map<String, Integer> wordsCountOfCurrentPart,
			Map<String, Integer> overAllWordCount) {
		this.i = i;
		this.lines = lines;
		this.wordsCountOfCurrentPart = wordsCountOfCurrentPart;
		this.overAllWordCount = overAllWordCount;
	}

	private int i;
	private List<String> lines;
	private Map<String, Integer> wordsCountOfCurrentPart;
	private Map<String, Integer> overAllWordCount;

	@Override
	public void run() {
		for (String line : lines) {
			String string[] = line.toLowerCase().split("\\P{L}+");
			// Adding all words generated in previous step into words
			for (String s : string) {
				if (s.equals(""))
					continue;
				overAllWordCount.put(s, overAllWordCount.getOrDefault(s, 0) + 1);
				wordsCountOfCurrentPart.put(s, wordsCountOfCurrentPart.getOrDefault(s, 0) + 1);
			}
		}
		synchronized (this) {
			System.out.println("*********** PART " + i + " ***********");
			for (Entry<String, Integer> entry : wordsCountOfCurrentPart.entrySet()) {
				System.out.println(entry);
			}

		}

	}
}
