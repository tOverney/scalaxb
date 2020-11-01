import org.specs2.mutable._
import java.io.File

import scalaxb.compiler.Module
import scalaxb.compiler.xsd.Driver
import org.specs2.matcher
import scalaxb.compiler.Config

trait TestBase extends Specification with CompilerMatcher with matcher.FileMatchers {
  val tmp = new File("tmp")
  def resource(name: String) = new File(s"integration/src/test/resources/$name")  

  if (tmp.exists) deleteAll(tmp)
  tmp.mkdirs() // you need this for copyFileFromResource

  val config: Config = Config.default
  lazy val module: Module = new Driver(config.useJavaTime) // with Verbose

  /** Compile the `generated` files, execute `replLines` in the REPL, check whether the result is `expectedResult`.*/
  def repl(generated: Seq[File])(replLines: String, expectedResult: String) =
    (Seq(replLines), generated) must evaluateTo(expectedResult, "./tmp")
}
