package com.sikulix.htmlparse;

import net.htmlparser.jericho.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class App {

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

	static boolean debug = false;

	static String start;
	static String sourceUrlString;
	final static String notodo = "NADA";
	static String todo = notodo;

	static String lpURL =
					"https://bugs.launchpad.net/sikuli/+bugs?field.searchtext=&orderby=-importance&field.status%3Alist=INPROGRESS&"
					+ "field.status%3Alist=FIXCOMMITTED&assignee_option=any&field.assignee=&field.bug_reporter=&field.bug_commenter=&"
					+ "field.subscriber=&field.structural_subscriber=&field.milestone%3Alist=##version##&field.tag=&field.tags_combinator=ANY&"
					+ "field.has_cve.used=&field.omit_dupes.used=&field.omit_dupes=on&field.affects_me.used=&field.has_patch.used=&"
					+ "field.has_branches.used=&field.has_no_branches.used=&field.has_blueprints.used=&field.has_no_blueprints.used=&"
					+ "search=Search&orderby=-id&##start##";

	static String lp110 = "59266";
	static String lp120 = "63602";

	static Map<String, String> imps = new HashMap<String, String>();

	static List<String> options = new ArrayList<String>();
	static boolean isOptionsValid = false;

	static {
		imps.put("U", "0");
		imps.put("W", "1");
		imps.put("L", "2");
		imps.put("M", "3");
		imps.put("H", "4");
		imps.put("C", "5");
	}

	private static String nextArg(String[] args) {
		if (!isOptionsValid) {
			if(args.length == 0) {
				return notodo;
			}
			options.addAll(Arrays.asList(args));
			isOptionsValid = true;
		}
		if (options.isEmpty()) {
			return null;
		}
		String next = options.get(0);
		options.remove(0);
		return next;
	}

	private static String nextArg() {
		return nextArg(new String[]{});
	}

	private static String nextArg(String preset) {
		String next = nextArg();
		if (next == null) {
			return preset;
		}
		return next;
	}

	public static void main(String[] args) throws Exception {
		todo = nextArg(args);
		if ("tess".equals(todo)) {
			start = "http://tesseract-ocr.googlecode.com/files/";
			sourceUrlString="http://code.google.com/p/tesseract-ocr/downloads/list";
			Source source=new Source(new URL(sourceUrlString));
			scanSegmentsTess(source.getAllElements(HTMLElementName.TD));
		}
		if (todo.startsWith("lp")) {
			start = "https://bugs.launchpad.net/sikuli/+bug/";
			Map<String, String> bugs = new HashMap<String, String>();
			String vers = lp110;
			String vfor = "1.1.0";
			if (null != nextArg()) {
				vers = lp120;
				vfor = "1.2.0";
			}
			String url = lpURL.replace("##version##", vers);
			int first = 0;
			int count = 1;
			String from = "start=0";
			while (count > 0) {
				Source source=new Source(new URL(url.replace("##start##", from)));
				count = scanSegmentsLP(source, first, bugs);
				if (count > 0) {
					first += count;
					from = String.format("memo=%d&start=%d", first, first);
				}
			}
			p("*** %s has entries: %d", vfor, first);
			Object[] keys = bugs.keySet().toArray();
			Arrays.sort(keys, Collections.reverseOrder());
			String key;
			String val;
			String bnum;
			for (Object k: keys) {
				key = (String) k;
				val = bugs.get(key);
				bnum = key.substring(2);
				p("(%s - %s) %s", bnum, val.substring(0,2), val.substring(2) );
				p("link: `%s - %s <%s>`_", bnum, val.substring(0,2), start + bnum);
			}
		}
		if ("dump".equals(todo)) {
			String url = nextArg();
			if (url == null) {
				todo = notodo;
			}
			displaySegments(new Source(new URL(url)));
		}
		if (todo == notodo) {
			p("Nothing to do");
			System.exit(1);
		}
		System.exit(1);
	}

	private static void displaySegments(Source source) {
		for (Segment segment : source.getAllElements()) {
			displaySegment(segment);
		}
	}

	private static void displaySegments(Source source, String elem) {
		for (Segment segment : source.getAllElements(elem)) {
			displaySegment(segment);
		}
	}

	private static void displaySegment(Segment segment) {
			p("-------------------------------------------------------------------------------");
			p("%s", segment.getDebugInfo());
			p("%s", segment.toString());
	}

	private static void scanSegmentsTess(List<? extends Segment> segments) {
		String href;
		String link;
		String cls;
		String lang;
		for (Segment segment : segments) {
			if (debug) {
				displaySegment(segment);
				p("-------------------------------------------------------------------------------");
			}
			cls = segment.getFirstElement().getAttributeValue("class");
			if (cls == null) {
				continue;
			}
			if (!cls.startsWith("vt col_1")) {
				continue;
			}
			link = segment.getTextExtractor().toString();
			if (!link.endsWith("3.02")) {
				continue;
			}
			if (!link.contains("language")) {
				continue;
			}
			link = link.split(" language")[0];
			href = segment.getAllElements(HTMLElementName.A).get(0).getAttributeValue("href");
			href = href.split("\\?")[1].split("&")[0].split("=")[1];
			String parts[] = href.split("\\.");
			lang = parts[parts.length - 3];
			System.out.println(String.format("%s = %s (%s)", lang, link, start + href));
		}
	}

	private static int scanSegmentsLP(Source source, int first, Map<String, String> bugs) {
		String href;
		String link;
		String lang;
		List<? extends Segment> segments = source.getAllElementsByClass("buglisting-row");
		int n = first;
		int count = 0;
		for (Segment segment : segments) {
			if (debug) {
				displaySegment(segment);
				p("-------------------------------------------------------------------------------");
			}
			List<Element> elems = segment.getAllElements();
//			displaySegment(segment);
			String bn = "";
			String bt = "";
			String st = "";
			String im = "";
			String cls;
			String key;
			for (Element e : elems) {
				cls = e.getAttributeValue("class");
				if ("bugnumber".equals(cls)) {
					bn = e.getTextExtractor().toString().substring(1);
					bn = "000" + bn;
					bn = bn.substring(bn.length()-7);
				} else if ("bugtitle".equals(cls)) {
					bt = e.getTextExtractor().toString();
				} else if (cls.startsWith("importance")) {
					im = e.getTextExtractor().toString().substring(0, 1);
				}else if (cls.startsWith("status")) {
					st = e.getTextExtractor().toString().substring(0, 1);
				}
				if (!bt.isEmpty()) {
					n++;
					count++;
					key = ("F".equals(st) ? "2" : "1") + imps.get(im) + bn;
					bugs.put(key, st + im + bt);
//					p("%3d %s (%s - %s%s) %s", n, key, bn, st, im, bt);
					bn = bt = "";
				}
			}
			if (debug) System.exit(1);
		}
		return count;
	}
}
