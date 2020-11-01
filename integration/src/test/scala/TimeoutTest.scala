import java.io.File
import scalaxb.stockquote.server._
import scala.concurrent._, duration.Duration

class TimeoutTest extends Wsdl11TestBase with JaxwsTestBase {

  def serviceImpl:DocumentWrappedService = new DocumentWrappedService(serverSleepTime)
  def serviceAddress: String = "document-wrapped"

  step {
    startServer
  }

  val packageName = "stockquote"
  val wsdlFile = new File(s"integration/target/$serviceAddress.wsdl")
  lazy val generated = {
    writeStringToFile(retrieveWsdl, wsdlFile)
    module.process(wsdlFile, config)
  }

  def requestTimeout: Int = 1
  def connectionTimeout: Int = 1
  def serverSleepTime = Duration(3, "seconds") // specs2 has its own Duration
  def totalTestAwait: Int = 10

  "request timeout times out" in {
    (List(s"""import stockquote._
      import scala.concurrent._, duration._, ExecutionContext.Implicits._
      import java.util.concurrent.ExecutionException
      import java.util.NoSuchElementException""",
      s"""val service = (new DocumentWrappedServiceSoapBindings with scalaxb.Soap11ClientsAsync with scalaxb.DispatchHttpClientsAsync {
        override def requestTimeout = $requestTimeout.seconds
        override def connectionTimeout = $connectionTimeout.seconds
      }).service""",
      s"""val fresponse: Future[Throwable] = service.price(Some("GOOG")).failed""",
      s"""val response: Throwable = Await.result(fresponse, $totalTestAwait.seconds)""",
      s"""response match {
        case e: ExecutionException => ()
        case e: NoSuchElementException => sys.error("Request was supposed to timeout but it returned successfully")
        case e: Throwable => throw e
      }""",
      s"""true"""), generated) must evaluateTo(true,
      outdir = "./tmp", usecurrentcp = true)
  }

  step {
    stopServer
  }
}
