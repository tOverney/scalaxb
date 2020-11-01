import java.io.File
import scalaxb.compiler.Config
import scalaxb.compiler.ConfigEntry._
import scalaxb.stockquote.server._

class Wsdl11RpcTest extends Wsdl11TestBase with JaxwsTestBase {

  def serviceImpl:RpcLiteralService = new RpcLiteralService()
  def serviceAddress: String = "rpc-literal"

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
      """val service = (new RpcLiteralServiceSoapBindings with scalaxb.Soap11ClientsAsync with scalaxb.DispatchHttpClientsAsync {}).service""",
      """val fresponse = service.price("GOOG")""",
      """val response = Await.result(fresponse, 5.seconds)""",
      """if (response != 42.0) sys.error(response.toString)""",
      
      """val fresponse2 = service.useHeader(Some("GOOG"))""",
      """val response2 = Await.result(fresponse2, 5.seconds)""",
      """if (response2 != UseHeaderOutput(Some(42))) sys.error(response2.toString)""",

      """true"""), generated) must evaluateTo(true,
      outdir = "./tmp", usecurrentcp = true)
  }

  step {
    stopServer
  }
}
