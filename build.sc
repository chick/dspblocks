// import Mill dependency
import mill._
import mill.define.Sources
import mill.modules.Util
import mill.scalalib.TestModule.ScalaTest
import scalalib._
// support BSP
import mill.bsp._
// input build.sc from each repositories.
import $file.`api-config-chipsalliance`.`build-rules`.mill.build
import $file.`berkeley-hardfloat`.build
import $file.`rocket-chip`.common

// Global Scala Version
val sv = "2.12.13"
val chisel3Ivy = ivy"edu.berkeley.cs::chisel3:3.4.3"
val chisel3PluginIvy = ivy"edu.berkeley.cs:::chisel3-plugin:3.4.3"
val iotestersIvy = ivy"edu.berkeley.cs::chisel-iotesters:1.5.3"
val macroParadiseIvy = ivy"org.scalamacros:::paradise:2.1.1"
val dsptoolsIvy = ivy"edu.berkeley.cs::dsptools:1.4.3"

// RocketChip dependency
object myconfig extends `api-config-chipsalliance`.`build-rules`.mill.build.config with PublishModule {
  override def millSourcePath = os.pwd / "api-config-chipsalliance" / "design" / "craft"
  override def scalaVersion = sv
  override def pomSettings = myrocketchip.pomSettings()
  override def publishVersion = myrocketchip.publishVersion()
}

// RocketChip dependency
object myhardfloat extends `berkeley-hardfloat`.build.hardfloat {
  override def millSourcePath = os.pwd / "berkeley-hardfloat"
  override def scalaVersion = sv
  override def chisel3IvyDeps = Agg(chisel3Ivy)
}

// Build from source
object myrocketchip extends `rocket-chip`.common.CommonRocketChip {
  override def millSourcePath = os.pwd / "rocket-chip"
  override def scalaVersion = sv
  override def chisel3IvyDeps = Agg(chisel3Ivy)
  // TODO: dirty here, need to PR RocketChip
  override def scalacPluginIvyDeps = Agg(macroParadiseIvy, chisel3PluginIvy)
  def hardfloatModule: PublishModule = myhardfloat
  def configModule: PublishModule = myconfig
}

object dspblocks extends SbtModule { m =>
  override def millSourcePath = os.pwd
  override def scalaVersion = sv
  override def scalacOptions = Seq("-Xsource:2.11")
  override def moduleDeps = super.moduleDeps ++ Seq(myrocketchip)
  override def ivyDeps = Agg(dsptoolsIvy)
  object test extends Tests with ScalaTest {
    override def ivyDeps = m.ivyDeps() ++ Agg(iotestersIvy)
  }
}
