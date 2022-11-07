package regfile
import chisel3._
import chisel3.util._

sealed class ReadPort[T <: Data](gen:T, set:Int) extends Bundle {
  val addrWidth = log2Up(set)
  val addr = Input(UInt(addrWidth.W))
  val en = Input(Bool())
  val data = Output(gen)
}

sealed class WritePort[T <: Data](gen:T, set:Int) extends Bundle {
  val addrWidth = log2Up(set)
  val addr = Input(UInt(addrWidth.W))
  val en = Input(Bool())
  val data = Input(gen)
}

class RegFile[T <: Data](gen:T, set:Int, readPort:Int, writePort:Int) extends Module {
  val io = IO(new Bundle{
    val r = Vec(readPort, new ReadPort(gen, set))
    val w = Vec(writePort, new WritePort(gen, set))
  })
  //TODO: Fill your codes here.
  io.r.foreach(_.data := 0.U) //This line should be remove when filling your codes!
}
