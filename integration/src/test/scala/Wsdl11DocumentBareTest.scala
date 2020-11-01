import java.io.File
import scalaxb.compiler.Config
import scalaxb.compiler.ConfigEntry._
import scalaxb.stockquote.server._

class Wsdl11DocumentBareTest extends Wsdl11TestBase with JaxwsTestBase {

  def serviceImpl:DocumentLiteralBareService = new DocumentLiteralBareService()
  def serviceAddress: String = "document-bare"

  step {
    startServer
  }

  val packageName = "stockquote"
  val wsdlFile = new File(s"integration/target/$serviceAddress.wsdl")
  lazy val generated = {
    writeStringToFile(retrieveWsdl, wsdlFile)
    module.process(wsdlFile, config)
  }

  "document-bare service works" in {
    (List("""import stockquote._
      import scala.concurrent._, duration._, ExecutionContext.Implicits._""",
      """val service = (new DocumentLiteralBareServiceSoapBindings with scalaxb.Soap11ClientsAsync with scalaxb.DispatchHttpClientsAsync {}).service""",
      """val fresponse = service.price(Some("GOOG"))""",
      """val response = Await.result(fresponse, 5.seconds)""",
      """if (response != Some(42.0)) sys.error(response.toString)""",
      
      """val fresponse2 = service.useHeader(Some("GOOG"))""",
      """val response2 = Await.result(fresponse2, 5.seconds)""",

      """true"""), generated) must evaluateTo(true,
      outdir = "./tmp", usecurrentcp = true)
  }

  step {
    stopServer
  }
}
