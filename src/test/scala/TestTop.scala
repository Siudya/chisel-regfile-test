import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import regfile.RegFile

object GenRTL extends App {
  (new chisel3.stage.ChiselStage).execute(args,
    Seq(
      ChiselGeneratorAnnotation(() => new RegFile(UInt(64.W), 16, 8, 4))
    )
  )
}