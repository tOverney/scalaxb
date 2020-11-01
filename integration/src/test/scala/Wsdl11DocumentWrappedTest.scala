import java.io.File
import scalaxb.stockquote.server._
import scala.concurrent._, duration.Duration

class Wsdl11DocumentWrappedTest extends Wsdl11TestBase with JaxwsTestBase {

  // specs2 has its own Duration
  def serviceImpl:DocumentWrappedService = new DocumentWrappedService(Duration(0, "seconds"))
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

  "document-wrapped service works" in {
    (List("""import stockquote._
      import scala.concurrent._, duration._, ExecutionContext.Implicits._""",
      """val service = (new DocumentWrappedServiceSoapBindings with scalaxb.Soap11ClientsAsync with scalaxb.DispatchHttpClientsAsync {}).service""",
      """val fresponse = service.price(Some("GOOG"))""",
      """val response = Await.result(fresponse, 5.seconds)""",
      """if (response != 42.0) sys.error(response.toString) else ()""",      
      """true"""), generated) must evaluateTo(true,
      outdir = "./tmp", usecurrentcp = true)
  }

  step {
    stopServer
  }
}
