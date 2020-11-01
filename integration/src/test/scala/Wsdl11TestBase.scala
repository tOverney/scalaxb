import scalaxb.compiler.Config
import scalaxb.compiler.ConfigEntry.GeneratePackageDir
import scalaxb.compiler.ConfigEntry.Outdir
import scalaxb.compiler.ConfigEntry.PackageNames
import scalaxb.compiler.wsdl11.Driver
trait Wsdl11TestBase extends TestBase {
  def packageName: String
  override val config =
    Config.default.update(PackageNames(Map(None -> Some(packageName)))).
      update(Outdir(tmp)).
      update(GeneratePackageDir)
  override lazy val module = new Driver(config.useJavaTime) // with Verbose
}
