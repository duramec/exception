package io.zig.exception;

import static us.bpsm.edn.Keyword.newKeyword;
import static us.bpsm.edn.Symbol.newSymbol;
import us.bpsm.edn.Symbol;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.io.IOException;
import java.lang.StackTraceElement;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import io.zig.data.Convert;

public class EdnException {
	// todo: add special cases for +, -, and .
	static final Pattern exclusionPattern = Pattern
			.compile("[^a-zA-Z0-9\\._%$\\?=\\*&]+");

	/*
	 * Be extra paranoid here. If a weird case happens where we cannot
	 * deserialize field members through reflection, we want to get as much
	 * Exception information as possible.
	 */
	public static final String parse(Exception e) {
		Map<Object, Object> obj = createFull(e);
		String edn = "";
		try {
			edn = Convert.objectToEdn(obj);
		} catch (IOException andTryWithSimpler) {
			try {
				obj = createWithTrace(e);
				edn = Convert.objectToEdn(obj);
			} catch (Exception andTryWithSimplest) {
				try {
					obj = createSimple(e);
					edn = Convert.objectToEdn(obj);
				} catch (Exception andHopeAndPray) {
				}
			}
		}
		return edn;
	}

	static final Map<Object, Object> createFull(Exception e) {
		Map<Object, Object> obj = createSimple(e);
		placeState(obj, e);
		placeStackTrace(obj, e);
		return Collections.unmodifiableMap(obj);
	}

	static final Map<Object, Object> createSimple(Exception e) {
		Map<Object, Object> obj = new HashMap<Object, Object>();
		Symbol name = newSymbol(normalize(e.getClass().getName()));
		obj.put(newKeyword("type"), name);
		obj.put(newKeyword("message"), e.getMessage());
		return obj;
	}

	static final Map<Object, Object> createWithTrace(Exception e) {
		Map<Object, Object> obj = createSimple(e);
		placeStackTrace(obj, e);
		return Collections.unmodifiableMap(obj);
	}

	static final void placeStackTrace(Map<Object, Object> obj, Exception e) {
		List<Object> stack = new ArrayList<Object>();
		for (StackTraceElement element : e.getStackTrace()) {
			placeStackTraceElement(stack, element);
		}
		if (stack.size() > 0) {
			obj.put(newKeyword("trace"), stack);
		}
	}

	static final void placeStackTraceElement(List<Object> stack,
			StackTraceElement element) {
		ArrayList<Object> entry = new ArrayList<Object>();
		entry.add(newSymbol(normalize(element.getClassName())));
		entry.add(newSymbol(normalize(element.getMethodName())));
		entry.add(newSymbol(normalize(element.getFileName())));
		entry.add(element.getLineNumber());
		stack.add(entry);
	}

	static final void placeState(Map<Object, Object> object, Exception e) {
		Map<Object, Object> state = new HashMap<Object, Object>();
		Class<?> klass = e.getClass();
		try {
			for (Field field : klass.getDeclaredFields()) {
				placeStateField(state, field, e);
			}
		} catch (Exception andClearExistingState) {
			state.clear();
		}
		if (state.size() > 0) {
			object.put(newKeyword("state"), state);
		}
	}

	static final <T extends Exception> void placeStateField(
			Map<Object, Object> state, Field field, T e)
			throws IllegalArgumentException, IllegalAccessException {
		String name = field.getName();
		switch (name) {
		case "serialVersionUID":
			break;
		default:
			field.setAccessible(true);
			state.put(newSymbol(normalize(name)), field.get(e));
			break;
		}
	}
	
	static final String normalize(String s) {
		return exclusionPattern.matcher(s).replaceAll("_");
	}
}