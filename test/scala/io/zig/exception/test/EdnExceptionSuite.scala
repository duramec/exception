package io.zig.exception.test

import io.zig.exception.EdnException
import io.zig.data.Convert
import us.bpsm.edn.{ Keyword, Symbol }
import org.scalatest.FunSuite
import java.io.FileReader

class ATestException(val value1: String, val value2: java.lang.Integer, e: Throwable) extends Exception(e)

class EdnExceptionSuite extends FunSuite {

  val filename = "some-file-that-does-not-exist.txt"
  def getException(): Exception = {
    try {
      val fr = new FileReader(filename)
      fr.read()
      fail
    } catch {
      case e: Exception ⇒ return e
    }
  }

  test ("parses exception") {
    val e = getException()
    val edn = EdnException.format(e)
    val obj = Convert.ednToObjects(edn).get(0).asInstanceOf[java.util.Map[Object, Object]]
    val s = obj.get(Keyword.newKeyword("message")).asInstanceOf[String]
    expectResult (true) { s.startsWith(filename) }
    val trace = obj.get(Keyword.newKeyword("trace")).asInstanceOf[java.util.List[java.util.List[Object]]]
    import scala.collection.JavaConversions._
    expectResult (true) { trace.size > 10 }
    trace map {
      line ⇒ expectResult (4) { line.size }
    }
  }

  test ("parses exception with inherited field members") {
    val e = getException()
    val wrapped = new ATestException("something", 123, e)
    val edn = EdnException.format(wrapped)
    val obj = Convert.ednToObjects(edn).get(0).asInstanceOf[java.util.Map[Object, Object]]
    val state = obj.get(Keyword.newKeyword("state")).asInstanceOf[java.util.Map[Object, Object]]
    expectResult ("something") { state.get(Symbol.newSymbol("value1")).asInstanceOf[java.lang.String] }
    expectResult (123) { state.get(Symbol.newSymbol("value2")).asInstanceOf[java.lang.Long] }
  }

}