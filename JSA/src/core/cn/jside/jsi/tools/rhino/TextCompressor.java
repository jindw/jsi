package cn.jside.jsi.tools.rhino;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.ScriptRuntime;

public class TextCompressor{
	public static final String BASE_64X_CODE;

	public static final String BASE_64X_FUNCTION_FULL = "function C(A){return A<62?String.fromCharCode(A+=A<26?65:A<52?71:-4):A<63?'_':A<64?'$':C(A>>6)+C(A&63)}";

	public static final String BASE_64X_FUNCTION_64 = "function C(A){return A<62?String.fromCharCode(A+=A<26?65:A<52?71:-4):A<63?'_':'$'}";

	public static final String BASE_64X_FUNCTION_63 = "function C(A){return String.fromCharCode(A+=A<26?65:A<52?71:A<62?-4:33)}";

	public static final String BASE_64X_FUNCTION_62 = "function C(A){return String.fromCharCode(A+=A<26?65:A<52?71:-4)}";

	public static final String BASE_64X_FUNCTION_52 = "function C(A){return String.fromCharCode(A+=A<26?65:71)}";

	public static final String BASE_64X_FUNCTION_26 = "function C(A){return String.fromCharCode(A+65)}";

	// public static final String DECODING_FUNCTION_COMPATIBLE =
	// "eval(function(D,G,B,H,A,I,F){C();while(A>=0)I[C(B+A)]=G[A--];function
	// L(A){return I[A]||A}if(''.replace(/^/,String)){var
	// K=D.match(H).reverse(),E=D.split(H).reverse(),J=E.pop();if(D.indexOf(J)){E.push(J);J=''}do{F.push(J);F.push(L(K.pop()||''))}while(J=E.pop());return
	// F.join('')}return
	// D.replace(H,L)}(_code,_keylist,_start,/[\\w\\$]+/g,_index,{},[]))";
	public static final String DECODING_FUNCTION_COMPATIBLE = "eval(function(E,I,A,D,J,K,L,H){C();while(A>0)K[C(D--)]=I[--A];function N(A){return K[A]==L[A]?A:K[A]}if(''.replace(/^/,String)){var M=E.match(J),B=M[0],F=E.split(J),G=0;if(E.indexOf(F[0]))F=[''].concat(F);do{H[A++]=F[G++];H[A++]=N(B)}while(B=M[G]);H[A++]=F[G]||'';return H.join('')}return E.replace(J,N)}(_code,_keylist,_index,_maxindex,/[\\w\\$]+/g,{},{},[]))";

	public static final String DECODING_FUNCTION = "eval(function(B,D,A,G,E,F){C();while(A>0)E[C(G--)]=D[--A];return B.replace(/[\\w\\$]+/g,function(A){return E[A]==F[A]?A:E[A]})}(_code,_keylist,_index,_maxindex,{},{}))";

	// "eval(function(B,E,D,A,F,G){C();while(A>=0)F[C(D+A)]=E[A--];return
	// B.replace(/[\\w\\$]+/g,function(A){return
	// F[A]==G[A]?A:F[A]})}(_code,_keylist,_start,_index,{}))";

	// public static final int MAX_SIMPLE_INDEX = 26 * 2;

	// public static final Collection<String> RESERVED_TOKEN ;
	static {
		// HashSet<String> set = new HashSet<String>();
		// set.add("constructor");
		// set.add("toString");
		// RESERVED_TOKEN = Collections.unmodifiableSet(set);
	}
	static {
		StringBuilder buf = new StringBuilder();
		for (char c = 'A'; c <= 'Z'; c++) {
			buf.append(c);
		}
		for (char c = 'a'; c <= 'z'; c++) {
			buf.append(c);
		}
		buf.append("0123456789_$");
		BASE_64X_CODE = buf.toString();
	}

	public static String getBase64XCode(int index) {
		if (index >= 64) {
			StringBuilder buf = new StringBuilder();
			do {
				buf.append(BASE_64X_CODE.charAt(index & 63));
				index >>>= 6;
			} while (index > 0);
			buf.reverse();
			return buf.toString();
		} else {
			return BASE_64X_CODE.substring(index, index + 1).intern();
		}
	}

	public String compress(final String source, final boolean compatible) {
		// DebugTool.println(source);
		// 以后替换值范围必须是这个范围的子集
		Pattern pattern = Pattern.compile("[\\w\\$]+");
		Matcher result = pattern.matcher(source);
		int latestEnd = 0;
		ArrayList<String> oplist = new ArrayList<String>();
		ArrayList<String> keylist = new ArrayList<String>();
		while (result.find()) {
			int start = result.start();
			oplist.add(source.substring(latestEnd, start));
			int end = result.end();
			keylist.add(source.substring(start, end));
			latestEnd = end;
		}
		oplist.add(source.substring(latestEnd));
		LinkedHashMap<String, String> keyMap = new LinkedHashMap<String, String>();
		int begin = this.buildKeyMap(keylist, keyMap);
		String dest = this.buildFunction(oplist, keylist, begin, keyMap,
				compatible);
		// DebugTool.println(dest);
		return dest;
	}

	private String buildFunction(List opList, List keyList, int start,
			LinkedHashMap<String, String> keyMap, final boolean compatible) {
		Iterator<String> opit = opList.iterator();
		Iterator<String> keyit = keyList.iterator();
		StringBuilder buf = new StringBuilder();
		while (keyit.hasNext()) {
			buf.append(opit.next());
			String key = keyit.next();
			if (keyMap.containsKey(key)) {
				buf.append(keyMap.get(key));
			} else {
				buf.append(key);
			}
		}
		buf.append(opit.next());
		String code = buf.toString();
		buf = new StringBuilder("'");
		int index = -1;
		for (String key : keyMap.keySet()) {
			index++;
			if (index > 0) {
				buf.append("|");
			}
			buf.append(key);
		}
		int maxCodeIndex = index + start;
		buf.append("'.split('|')");
		String keys = buf.toString();
		String function = DECODING_FUNCTION;
		if (compatible) {
			function = DECODING_FUNCTION_COMPATIBLE;
		}

		function = function
				.replace(
						"C();",
						maxCodeIndex >= 64 ? BASE_64X_FUNCTION_FULL
								: maxCodeIndex >= 63 ? BASE_64X_FUNCTION_64
										: maxCodeIndex >= 62 ? BASE_64X_FUNCTION_63
												: maxCodeIndex >= 52 ? BASE_64X_FUNCTION_62
														: maxCodeIndex >= 26 ? BASE_64X_FUNCTION_52
																: BASE_64X_FUNCTION_26);
		String code1 = ScriptRuntime.escapeString(code, '\'');
		String code2 = ScriptRuntime.escapeString(code, '\"');
		if (code1.length() > code2.length()) {
			code = '\"' + code2 + '\"';
		} else {
			code = '\'' + code1 + '\'';
		}

		// function = function.replaceFirst("_index", index+"");
		// function = function.replaceFirst("_start", start+"");
		// function = function.replaceFirst("_keylist", keys);
		// function = function.replaceFirst("_code", code);
		String[] parts = function.split("_[\\w]+");
		buf = new StringBuilder();
		buf.append(parts[0]);
		buf.append(code);
		buf.append(parts[1]);
		buf.append(keys);
		buf.append(parts[2]);
		buf.append(index + 1 + "");
		buf.append(parts[3]);
		buf.append(maxCodeIndex + "");
		buf.append(parts[4]);
		// $keylist,$start,/[\\w\\$]+/,$index
		// }(_code,_keylist,_index,_maxindex,/[\w\$]+/g,{},{},[])

		return buf.toString();
	}

	protected int buildKeyMap(ArrayList<String> keylist,
			LinkedHashMap<String, String> result) {
		Map<String, KeyEntry> entryMap = new HashMap<String, KeyEntry>();
		// DebugTool.println(keylist);
		for (String key : keylist) {
			KeyEntry entry = entryMap.get(key);
			if (entry == null) {
				entryMap.put(key, new KeyEntry(key));
			} else {
				entry.count++;
			}
		}
		KeyEntry[] entrys = entryMap.values().toArray(new KeyEntry[0]);
		if (entrys.length > 64) {
			Arrays.sort(entrys);
		}
		int simpleKeyCount = Math.min(entrys.length, 64);
		Arrays.sort(entrys, 0, simpleKeyCount, new Comparator<KeyEntry>() {
			public int compare(KeyEntry entry, KeyEntry other) {
				String key1 = entry.key;
				String key2 = other.key;
				if (key1.length() == 1 && key2.length() == 1) {
					return BASE_64X_CODE.indexOf(key1)
							- BASE_64X_CODE.indexOf(key2);
				} else {
					return key1.length() - key2.length();
				}
			}

		});
		int offset = 0;
		while (offset < simpleKeyCount) {
			KeyEntry entry = entrys[offset];
			if(BASE_64X_CODE.charAt(offset) ==entry.key.charAt(0)){
				//DebugTool.println(entry.key+":"+offset);
				entry.reserved = true;
				offset++;
			}else{
				break;
			}
		}

		//DebugTool.println("----");
		final int begin = offset;
		Map<String, KeyEntry> reservedSet = new HashMap<String, KeyEntry>();
		List<KeyEntry> entryList = new ArrayList<KeyEntry>();
		while (offset < entrys.length) {
			KeyEntry entry = entrys[offset++];

			//DebugTool.println(entry.key+":"+(offset-1));
			if (entry.count == 1) {
				entry.reserved = true;
				reservedSet.put(entry.key, entry);
				continue;
			}else{
				entryList.add(entry);
			}
		}
		offset = begin;
		for (int i = 0; i < entryList.size(); i++) {
			KeyEntry entry = entryList.get(i);
			// 开始替换
			entry.replace = getBase64XCode(offset++);
			if (reservedSet.containsKey(entry.replace)) {
				KeyEntry unreservedEntry = reservedSet.get(entry.replace);
				reservedSet.remove(entry.replace);
				unreservedEntry.reserved = false;
				entryList.add(unreservedEntry);
			}
			result.put(entry.key, entry.replace);
		}
		return begin;
	}

	static class KeyEntry implements Comparable {
		boolean reserved;

		String key;

		String replace;

		int count;

		public KeyEntry(String key) {
			this.key = key;
			this.count = 1;
		}

		public int compareTo(Object o) {
			KeyEntry other = (KeyEntry) o;
			int result = this.count - other.count;
			return result > 0 ? -1 : result == 0 ? 0 : 1;
		}

		@Override
		public String toString() {
			return "key=" + key + ";replace=" + replace + ";count="
					+ this.count;
		}

	}

}
