import os.Path
import mill._
import scalalib._

object ivys {
  val sv = "2.12.13"
  val chisel3 = ivy"edu.berkeley.cs::chisel3:3.5.3"
  val chisel3Plugin = ivy"edu.berkeley.cs:::chisel3-plugin:3.5.3"
  val chiseltest = ivy"edu.berkeley.cs::chiseltest:0.5.3"
  val scalatest = ivy"org.scalatest::scalatest:3.2.2"
  val macroParadise = ivy"org.scalamacros:::paradise:2.1.1"
  val sourcecode = ivy"com.lihaoyi::sourcecode:0.2.7"
}

trait MyCommonModule extends ScalaModule with SbtModule{
  override def scalaVersion = ivys.sv
  override def compileIvyDeps = Agg(ivys.macroParadise)
  override def scalacPluginIvyDeps = Agg(ivys.macroParadise, ivys.chisel3Plugin)
  override def scalacOptions = Seq("-Xsource:2.11", "-language:reflectiveCalls")
  override def ivyDeps = Agg(ivys.chisel3, ivys.sourcecode)
}

object RegFile extends MyCommonModule{
  override def moduleDeps = super.moduleDeps
  override def millSourcePath = os.pwd

  object test extends Tests with TestModule.ScalaTest{
    override def ivyDeps = super.ivyDeps() ++ Agg(ivys.scalatest, ivys.chiseltest)
  }
}