/*
 * Copyright (c) 2014 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.asterisque

import org.specs2.Specification

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// OptionsSpec
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
 */
class OptionsSpec extends Specification { def is = s2"""
Options should:
construct without error. $e0
retrieve set value. $e1
take over initial values. $e2
take default value if value not exist. $e3
parse string as key-defined value. $e4
"""
	def e0 = {
		new Options()
		success
	}

	def e1 = {
		class A
		object B extends A
		val ikey = new Options.Key[Integer]("int", classOf[Integer])
		val bkey = new Options.Key[java.lang.Boolean]("boolean", classOf[java.lang.Boolean])
		val skey = new Options.Key[String]("string", classOf[String])
		val xkey = new Options.Key[A]("extends", classOf[A])
		val nkey = new Options.Key[Object]("none", classOf[Object])
		val o = new Options()
		o.set(ikey, 1234)
		o.set(bkey, false)
		o.set(xkey, B)
		o.set(skey, "foo")
		(o.get(ikey).get().intValue() === 1234) and
			(o.get(bkey).get().booleanValue() must beFalse) and
			(o.get(skey).get() === "foo") and
			(o.get(xkey).get() === B) and
			(o.get(nkey).isEmpty must beTrue)
	}

	def e2 = {
		val ikey = new Options.Key[Integer]("int", classOf[Integer])
		val o1 = new Options()
		o1.set(ikey, 1234)
		val o2 = new Options(o1)
		o1.set(ikey, 0)
		(o2.get(ikey).get().intValue() === 1234) and (o1.get(ikey).get().intValue() === 0)
	}

	def e3 = {
		val ikey = new Options.Key[Integer]("int", classOf[Integer])
		val o = new Options()
		(o.get(ikey).isEmpty must beTrue) and (o.getOrElse(ikey, () => Integer.valueOf(1234)).intValue() === 1234)
	}

	def e4 = {
		val ikey = new Options.IntKey("int")
		val skey = new Options.StringKey("int")
		val o = new Options()
		o.set(skey, "12345")
		(o.get(ikey).get().toInt === 12345) and (o.get(skey).get() === "12345")
	}
}
