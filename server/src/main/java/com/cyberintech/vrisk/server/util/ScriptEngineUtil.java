package com.cyberintech.vrisk.server.util;

import com.oracle.truffle.js.runtime.JSContextOptions;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Scripting Engine Utils
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-08-11
 */
public class ScriptEngineUtil {

	/**
	 * Obtain JavaScript Engine
	 *
	 * @return
	 */
	public static GraalJSScriptEngine getJavaScriptEngine() {
		// ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");
		GraalJSScriptEngine engine = GraalJSScriptEngine.create(
			Engine.newBuilder().option("engine.WarnInterpreterOnly", "false").build(),
			Context.newBuilder("js").allowIO(false).option(JSContextOptions.ECMASCRIPT_VERSION_NAME, "2022")
		);

		return engine;
	}

	/**
	 * Obtain JavaScript Engine
	 *
	 * @return
	 */
	public static void executeJavaScript(GraalJSScriptEngine engine, String executableContent) {
		try {
			engine.eval(executableContent);
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

}
